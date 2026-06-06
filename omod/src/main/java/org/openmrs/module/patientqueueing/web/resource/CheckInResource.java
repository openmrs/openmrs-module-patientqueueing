/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.web.resource;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.PatientQueueingConfig;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.api.ProviderAssignmentService;
import org.openmrs.module.patientqueueing.web.customdto.CheckInResult;
import org.openmrs.module.patientqueueing.web.customdto.QueueEntry;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * REST resource for generic patient check-in with configurable visit type. Supports provider
 * auto-assignment, queue position calculation, and estimated wait time.
 */
@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/checkin", supportedClass = CheckInResult.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class CheckInResource extends DelegatingCrudResource<CheckInResult> {
	
	@Override
	public CheckInResult newDelegate() {
		return new CheckInResult();
	}
	
	@Override
	public CheckInResult save(CheckInResult delegate) {
		// CREATE is handled via create() method with SimpleObject
		throw new ResourceDoesNotSupportOperationException("SAVE not supported");
	}
	
	@Override
	public CheckInResult getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException("GET by UUID not supported");
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("SEARCH not supported");
	}
	
	@Override
	public CheckInResult create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		PatientService patientService = Context.getPatientService();
		LocationService locationService = Context.getLocationService();
		ProviderService providerService = Context.getProviderService();
		VisitService visitService = Context.getVisitService();
		AdministrationService adminService = Context.getAdministrationService();
		PatientQueueingService queueingService = Context.getService(PatientQueueingService.class);
		ProviderAssignmentService providerAssignmentService = Context.getService(ProviderAssignmentService.class);
		
		// Extract required properties with backward compatibility
		String patientUuid = getRequiredProperty(propertiesToCreate, "patient");
		
		// Support both "locationTo" (new) and "location" (backward compatibility)
		String locationToUuid = propertiesToCreate.get("locationTo");
		if (locationToUuid == null) {
			locationToUuid = propertiesToCreate.get("location");
		}
		if (locationToUuid == null) {
			throw new IllegalArgumentException("locationTo (or location) is required");
		}
		
		// Extract optional properties
		String visitTypeUuid = propertiesToCreate.get("visitType");
		String queueRoomUuid = propertiesToCreate.get("queueRoom");
		String providerUuid = propertiesToCreate.get("provider");
		Integer priority = getIntegerProperty(propertiesToCreate, "priority");
		String comment = propertiesToCreate.get("comment");
		Boolean autoAssignProvider = getBooleanProperty(propertiesToCreate, "autoAssignProvider", null);
		
		// Retrieve and validate entities
		Patient patient = patientService.getPatientByUuid(patientUuid);
		validateNotNull(patient, "Patient not found for UUID: " + patientUuid);
		
		Location locationTo = locationService.getLocationByUuid(locationToUuid);
		validateNotNull(locationTo, "Location not found for UUID: " + locationToUuid);
		
		Location queueRoom = null;
		if (StringUtils.isNotBlank(queueRoomUuid)) {
			queueRoom = locationService.getLocationByUuid(queueRoomUuid);
		}
		
		Provider provider = null;
		if (StringUtils.isNotBlank(providerUuid)) {
			provider = providerService.getProviderByUuid(providerUuid);
		}
		
		// Determine visit type - use provided or default from configuration
		VisitType visitType = getVisitType(visitService, adminService, visitTypeUuid);
		validateNotNull(visitType, "VisitType not found - configure patientqueueing.defaultVisitTypeUuid global property");
		
		// Create or reuse visit with null check for error handling
		Visit visit = ensureVisitForPatient(visitService, patient, locationTo, visitType);
		if (visit == null) {
			throw new APIException("Failed to create visit for patient - please check visit configuration");
		}
		
		// Create patient queue entry
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setLocationTo(locationTo);
		patientQueue.setQueueRoom(queueRoom);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setDateCreated(new Date());
		patientQueue.setPriority(priority);
		patientQueue.setComment(comment);
		
		// Generate visit number (ticket number)
		String visitNumber = queueingService.generateVisitNumber(locationTo, patient);
		patientQueue.setVisitNumber(visitNumber);
		
		// Auto-assign provider if requested and no provider specified
		if (provider == null && shouldAutoAssignProvider(adminService, autoAssignProvider)) {
			provider = providerAssignmentService.assignProvider(locationTo);
		}
		
		if (provider != null) {
			patientQueue.setProvider(provider);
		}
		
		// Save the queue entry
		patientQueue = queueingService.savePatientQue(patientQueue);
		
		// Calculate queue position
		int queuePosition = calculateQueuePosition(queueingService, locationTo, queueRoom);
		
		// Calculate estimated wait time
		int estimatedWaitMinutes = calculateEstimatedWait(queuePosition, adminService);
		
		// Build and return result
		CheckInResult result = new CheckInResult();
		result.setPatient(patient);
		result.setVisit(visit);
		result.setQueueEntry(new QueueEntry(patientQueue));
		result.setTicketNumber(patientQueue.getVisitNumber());
		result.setQueuePosition(queuePosition);
		result.setEstimatedWaitMinutes(estimatedWaitMinutes);
		result.setSuccess(true);
		
		return result;
	}
	
	/**
	 * Ensures the patient has an active visit. Reuses existing active visit or creates a new one.
	 * 
	 * @param visitService The visit service
	 * @param patient The patient
	 * @param location The location
	 * @param visitType The visit type
	 * @return The visit (existing or newly created)
	 */
	private Visit ensureVisitForPatient(VisitService visitService, Patient patient, Location location, VisitType visitType) {
		try {
			// Check for active visits
			List<Visit> activeVisits = visitService.getActiveVisitsByPatient(patient);
			if (activeVisits != null && !activeVisits.isEmpty()) {
				// Reuse first active visit
				return activeVisits.get(0);
			}
			
			// Create new visit starting at beginning of today
			Visit visit = new Visit();
			visit.setPatient(patient);
			visit.setVisitType(visitType);
			visit.setLocation(location);
			
			// Set start time to beginning of day
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			visit.setStartDatetime(calendar.getTime());
			
			Visit saved = visitService.saveVisit(visit);
			return saved;
		}
		catch (Exception e) {
			throw new APIException("Failed to create visit for patient: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Gets the visit type to use for the check-in. Uses provided UUID or default from
	 * configuration.
	 * 
	 * @param visitService The visit service
	 * @param adminService The administration service
	 * @param visitTypeUuid The provided visit type UUID (may be null)
	 * @return The visit type
	 */
	private VisitType getVisitType(VisitService visitService, AdministrationService adminService, String visitTypeUuid) {
		VisitType visitType = null;
		
		// Use provided visit type if available
		if (StringUtils.isNotBlank(visitTypeUuid)) {
			visitType = visitService.getVisitTypeByUuid(visitTypeUuid);
		}
		
		// Fall back to default from configuration
		if (visitType == null) {
			String defaultVisitTypeUuid = adminService.getGlobalProperty(PatientQueueingConfig.GP_DEFAULT_VISIT_TYPE);
			if (StringUtils.isNotBlank(defaultVisitTypeUuid)) {
				visitType = visitService.getVisitTypeByUuid(defaultVisitTypeUuid);
			}
		}
		
		return visitType;
	}
	
	/**
	 * Determines if provider should be auto-assigned based on configuration and request.
	 * 
	 * @param adminService The administration service
	 * @param autoAssignProvider Override value from request (null = use default)
	 * @return true if provider should be auto-assigned
	 */
	private Boolean shouldAutoAssignProvider(AdministrationService adminService, Boolean autoAssignProvider) {
		// Use override if provided
		if (autoAssignProvider != null) {
			return autoAssignProvider;
		}
		
		// Use default from configuration
		String defaultValue = adminService.getGlobalProperty(PatientQueueingConfig.GP_AUTO_ASSIGN_PROVIDER,
		    String.valueOf(PatientQueueingConfig.DEFAULT_AUTO_ASSIGN_PROVIDER));
		return Boolean.parseBoolean(defaultValue);
	}
	
	/**
	 * Calculates the patient's position in the queue for the given location and room.
	 * 
	 * @param queueingService The queueing service
	 * @param location The location
	 * @param queueRoom The queue room (may be null)
	 * @return The queue position (1-indexed)
	 */
	private int calculateQueuePosition(PatientQueueingService queueingService, Location location, Location queueRoom) {
		Date today = new Date();
		Date startOfDay = org.openmrs.util.OpenmrsUtil.firstSecondOfDay(today);
		Date endOfDay = org.openmrs.util.OpenmrsUtil.getLastMomentOfDay(today);
		
		List<PatientQueue> pendingQueues = queueingService.getPatientQueueList(null, startOfDay, endOfDay, location, null,
		    null, PatientQueue.Status.PENDING, queueRoom);
		
		// Position is count of people ahead + 1 for current patient
		return pendingQueues.size() + 1;
	}
	
	/**
	 * Calculates estimated wait time based on queue position.
	 * 
	 * @param queuePosition The patient's position in queue
	 * @param adminService The administration service
	 * @return Estimated wait time in minutes
	 */
	private int calculateEstimatedWait(int queuePosition, AdministrationService adminService) {
		// Get configured minutes per person ahead
		String minutesPerPersonStr = adminService.getGlobalProperty(
		    PatientQueueingConfig.GP_ESTIMATED_WAIT_MINUTES_PER_PERSON,
		    String.valueOf(PatientQueueingConfig.DEFAULT_ESTIMATED_WAIT_MINUTES_PER_PERSON));
		
		try {
			int minutesPerPerson = Integer.parseInt(minutesPerPersonStr);
			// Wait time = (people ahead) * minutes per person
			return (queuePosition - 1) * minutesPerPerson;
		}
		catch (NumberFormatException e) {
			return PatientQueueingConfig.DEFAULT_ESTIMATED_WAIT_MINUTES_PER_PERSON * (queuePosition - 1);
		}
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("UPDATE not supported");
	}
	
	@Override
	protected void delete(CheckInResult delegate, String s, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("DELETE not supported");
	}
	
	@Override
	public void purge(CheckInResult delegate, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("PURGE not supported");
	}
	
	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("patient");
			description.addProperty("visit");
			description.addProperty("queueEntry");
			description.addProperty("ticketNumber");
			description.addProperty("queuePosition");
			description.addProperty("estimatedWaitMinutes");
			description.addProperty("success");
			description.addProperty("errorMessage");
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("patient", Representation.FULL);
			description.addProperty("visit", Representation.FULL);
			description.addProperty("queueEntry", Representation.FULL);
			description.addProperty("ticketNumber");
			description.addProperty("queuePosition");
			description.addProperty("estimatedWaitMinutes");
			description.addProperty("success");
			description.addProperty("errorMessage");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient");
		description.addProperty("locationTo");
		description.addProperty("visitType");
		description.addProperty("queueRoom");
		description.addProperty("provider");
		description.addProperty("priority");
		description.addProperty("comment");
		description.addProperty("autoAssignProvider");
		return description;
	}
	
	/**
	 * Helper method to retrieve a required property.
	 */
	private String getRequiredProperty(SimpleObject properties, String key) {
		String value = properties.get(key);
		if (value == null) {
			throw new IllegalArgumentException(key + " is required");
		}
		return value;
	}
	
	/**
	 * Helper method to get an integer property.
	 */
	private Integer getIntegerProperty(SimpleObject properties, String key) {
		Object value = properties.get(key);
		if (value == null) {
			return null;
		}
		try {
			return Integer.parseInt(value.toString());
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(key + " must be a valid integer");
		}
	}
	
	/**
	 * Helper method to get a boolean property.
	 */
	private Boolean getBooleanProperty(SimpleObject properties, String key, Boolean defaultValue) {
		Object value = properties.get(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value.toString());
	}
	
	/**
	 * Helper method to validate that an object is not null.
	 */
	private void validateNotNull(Object obj, String errorMessage) {
		if (obj == null) {
			throw new IllegalArgumentException(errorMessage);
		}
	}
}
