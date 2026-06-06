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
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.web.customdto.SelfCheckIn;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.PatientQueueingConfig;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * UgandaEMR-compatible endpoint for self check-in supporting both Patients and NonPatients. Follows
 * UgandaEMR pattern with SelfCheckIn DTO (extends CheckInPatient) and DelegatingCrudResource
 * extension. Resource path: /ws/rest/v1/patientqueueing/selfcheckin
 * <p>
 * Check-in logic: 1. If patient identification (identifierTypeUuid + identifierValue OR configured
 * attribute types) is provided, try to find a matching patient first 2. If not found but phone
 * number provided, check if phone matches any patient using GP_PHONE_NUMBER_ATTRIBUTE_TYPE 3. If
 * patient found → create PatientQueue + Visit 4. If no patient found → create NonPatientQueue
 * (using displayName, phoneNumber, etc.)
 * <p>
 * Uses global properties to configure: - Person attribute types for phone number lookup
 * (GP_PHONE_NUMBER_ATTRIBUTE_TYPE) - Patient identifier types for identifier lookup
 * (GP_SELF_CHECK_IN_IDENTIFIER_TYPE_UUIDS) - Person attribute types for lookup
 * (GP_SELF_CHECK_IN_PERSON_ATTRIBUTE_TYPE_UUIDS)
 */
@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/selfcheckin", supportedClass = SelfCheckIn.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class SelfCheckInPatientResource extends DelegatingCrudResource<SelfCheckIn> {
	
	private static final Logger log = LoggerFactory.getLogger(SelfCheckInPatientResource.class);
	
	private static final String CHECK_IN_TYPE_PATIENT = "patient";
	
	private static final String CHECK_IN_TYPE_NON_PATIENT = "nonPatient";
	
	@Override
	public SelfCheckIn newDelegate() {
		return new SelfCheckIn();
	}
	
	@Override
	public SelfCheckIn save(SelfCheckIn delegate) {
		throw new ResourceDoesNotSupportOperationException("Use create() instead");
	}
	
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		// Cache services for this request
		PatientQueueingService queueingService = Context.getService(PatientQueueingService.class);
		AdministrationService administrationService = Context.getAdministrationService();
		
		// Cache global properties (eliminates repeated DB lookups)
		String phoneNumberAttributeTypeUuid = trimToNull(administrationService
		        .getGlobalProperty(PatientQueueingConfig.GP_PHONE_NUMBER_ATTRIBUTE_TYPE));
		String personAttributeTypeUuidsStr = trimToNull(administrationService
		        .getGlobalProperty(PatientQueueingConfig.GP_SELF_CHECK_IN_PERSON_ATTRIBUTE_TYPE_UUIDS));
		String identifierTypeUuidsStr = trimToNull(administrationService
		        .getGlobalProperty(PatientQueueingConfig.GP_SELF_CHECK_IN_IDENTIFIER_TYPE_UUIDS));
		
		// Get input parameters
		String identifierTypeUuid = trimToNull(propertiesToCreate.get("identifierTypeUuid"));
		String identifierValue = trimToNull(propertiesToCreate.get("identifierValue"));
		String phoneNumber = trimToNull(propertiesToCreate.get("phoneNumber"));
		String displayName = trimToNull(propertiesToCreate.get("displayName"));
		String queueTypeUuid = trimToNull(propertiesToCreate.get("queueTypeUuid"));
		
		// Get location parameters
		String currentLocationUuid = getRequiredProperty(propertiesToCreate, "currentLocation");
		String locationToUuid = getRequiredProperty(propertiesToCreate, "locationTo");
		String queueRoomUuid = getRequiredProperty(propertiesToCreate, "queueRoom");
		Integer priority = parsePriority(propertiesToCreate.get("priority"));
		
		// Resolve locations
		Location currentLocation = Context.getLocationService().getLocationByUuid(currentLocationUuid);
		Location locationTo = Context.getLocationService().getLocationByUuid(locationToUuid);
		Location queueRoom = Context.getLocationService().getLocationByUuid(queueRoomUuid);
		
		if (currentLocation == null) {
			throw new IllegalArgumentException("Current Location not found for UUID: " + currentLocationUuid);
		}
		if (locationTo == null) {
			throw new IllegalArgumentException("LocationTo not found for UUID: " + locationToUuid);
		}
		if (queueRoom == null) {
			throw new IllegalArgumentException("Queue Room not found for UUID: " + queueRoomUuid);
		}
		
		// Try to find patient using provided identification first
		Patient patient = null;
		if (identifierTypeUuid != null && identifierValue != null) {
			patient = resolvePatientByIdentifier(identifierTypeUuid, identifierValue, personAttributeTypeUuidsStr,
			    identifierTypeUuidsStr);
		}
		
		// If no patient found via direct identification but phone number provided, check via phone attribute
		if (patient == null && phoneNumber != null) {
			patient = findPatientByPhoneNumber(phoneNumber, phoneNumberAttributeTypeUuid);
		}
		
		// If patient found, create PatientQueue
		if (patient != null) {
			return createPatientCheckIn(queueingService, patient, currentLocation, locationTo, queueRoom, priority);
		}
		
		// No patient found - validate and create NonPatientQueue
		if (StringUtils.isBlank(displayName)) {
			throw new IllegalArgumentException("displayName is required for non-patient check-in");
		}
		return createNonPatientCheckIn(queueingService, displayName, phoneNumber, queueTypeUuid, currentLocation,
		    locationTo, queueRoom, priority);
	}
	
	/**
	 * Ensure patient has an active visit. Reuses existing active visit or creates a new one. This
	 * aligns with CheckInResource policy for consistency.
	 * 
	 * @param patient the patient
	 * @param location the location for the visit
	 * @return the visit (existing or newly created), or null if visit creation is disabled
	 */
	private Visit ensurePatientVisit(Patient patient, Location location) {
		try {
			AdministrationService administrationService = Context.getAdministrationService();
			String visitTypeUuid = trimToNull(administrationService
			        .getGlobalProperty(PatientQueueingConfig.GP_SELF_CHECK_IN_VISIT_TYPE_UUID));
			
			// If no visit type is configured, skip visit creation
			if (visitTypeUuid == null) {
				log.info("No visit type configured for self check-in, skipping visit creation");
				return null;
			}
			
			VisitService visitService = Context.getVisitService();
			
			// Check for active visits - reuse if exists
			List<Visit> activeVisits = visitService.getActiveVisitsByPatient(patient);
			if (activeVisits != null && !activeVisits.isEmpty()) {
				log.info("Patient " + patient.getPatientId() + " has " + activeVisits.size()
				        + " active visit(s). Reusing existing visit " + activeVisits.get(0).getUuid());
				return activeVisits.get(0);
			}
			
			// Create a new visit using the configured visit type
			VisitType visitType = Context.getVisitService().getVisitTypeByUuid(visitTypeUuid);
			if (visitType == null) {
				log.error("Configured visit type not found: " + visitTypeUuid);
				throw new IllegalArgumentException("Visit type configuration error. Please contact reception.");
			}
			
			Visit visit = new Visit();
			visit.setPatient(patient);
			visit.setVisitType(visitType);
			// Set visit start to beginning of day to avoid conflicts with encounter timestamps
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			visit.setStartDatetime(calendar.getTime());
			visit.setLocation(location);
			
			Visit savedVisit = visitService.saveVisit(visit);
			log.info("Started new visit " + savedVisit.getUuid() + " of type " + visitType.getName() + " for patient "
			        + patient.getPatientId());
			return savedVisit;
		}
		catch (IllegalArgumentException e) {
			// Re-throw validation exceptions
			throw e;
		}
		catch (Exception e) {
			log.error("Error ensuring patient visit: " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Find patient by phone number using the configured phone number attribute type.
	 */
	private Patient findPatientByPhoneNumber(String phoneNumber, String phoneNumberAttributeTypeUuid) {
		try {
			if (StringUtils.isBlank(phoneNumberAttributeTypeUuid)) {
				log.info("No phone number attribute type configured, skipping phone-based patient lookup");
				return null;
			}
			
			PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
			    phoneNumberAttributeTypeUuid);
			if (attributeType == null) {
				log.warn("Phone number attribute type not found for UUID: " + phoneNumberAttributeTypeUuid);
				return null;
			}
			
			PatientQueueingService queueingService = Context.getService(PatientQueueingService.class);
			return queueingService.getPatientByPersonAttributeValue(attributeType.getPersonAttributeTypeId(), phoneNumber);
		}
		catch (Exception e) {
			log.error("Error finding patient by phone number: " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Create a patient check-in entry with PatientQueue and Visit.
	 */
	private SimpleObject createPatientCheckIn(PatientQueueingService service, Patient patient, Location currentLocation,
	        Location locationTo, Location queueRoom, Integer priority) {
		// Create patient queue
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setLocationFrom(currentLocation);
		patientQueue.setLocationTo(locationTo);
		patientQueue.setQueueRoom(queueRoom);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setDateCreated(new Date());
		if (priority != null) {
			patientQueue.setPriority(priority);
		}
		
		// Generate visit number (ticket number)
		String visitNumber = service.generateVisitNumber(locationTo, patient);
		patientQueue.setVisitNumber(visitNumber);
		
		// Start a new visit for the patient (or reuse existing active visit)
		// NOTE: Visit and queue are saved in separate transactions. If savePatientQue fails
		// after ensurePatientVisit succeeds, the visit will be orphaned. A proper fix would
		// require transactional wrapping (e.g., @Transactional on service layer).
		Visit visit = ensurePatientVisit(patient, locationTo);
		
		// Save the queue entry
		patientQueue = service.savePatientQue(patientQueue);
		
		// Build response
		SimpleObject response = new SimpleObject();
		response.add("checkInType", CHECK_IN_TYPE_PATIENT);
		response.add("uuid", patientQueue.getUuid());
		response.add("patientQueue", buildPatientQueueObject(patientQueue));
		response.add("patient", buildPatientObject(patient));
		response.add("visit", visit != null ? buildVisitObject(visit) : null);
		response.add("ticketNumber", visitNumber);
		response.add("visitNumber", visitNumber);
		
		if (hasTypesDefined()) {
			response.add(RestConstants.PROPERTY_FOR_TYPE, "SelfCheckIn");
		}
		
		return response;
	}
	
	/**
	 * Create a non-patient check-in entry with NonPatientQueue.
	 */
	private SimpleObject createNonPatientCheckIn(PatientQueueingService service, String displayName, String phoneNumber,
	        String queueTypeUuid, Location currentLocation, Location locationTo, Location queueRoom, Integer priority) {
		// Resolve queue type
		Concept queueType = null;
		if (queueTypeUuid != null) {
			queueType = Context.getConceptService().getConceptByUuid(queueTypeUuid);
		}
		
		// Create non-patient queue entry
		NonPatientQueue nonPatientQueue = service.createNonPatientQueueEntry(displayName, phoneNumber, queueType,
		    currentLocation, locationTo, queueRoom, priority, null);
		
		// Build response
		SimpleObject response = new SimpleObject();
		response.add("checkInType", CHECK_IN_TYPE_NON_PATIENT);
		response.add("uuid", nonPatientQueue.getUuid());
		response.add("displayName", nonPatientQueue.getDisplayName());
		response.add("phoneNumber", nonPatientQueue.getPhoneNumber());
		response.add("ticketNumber", nonPatientQueue.getTicketNumber());
		response.add("status", nonPatientQueue.getStatus());
		response.add("nonPatientQueue", buildNonPatientQueueObject(nonPatientQueue));
		
		if (hasTypesDefined()) {
			response.add(RestConstants.PROPERTY_FOR_TYPE, "SelfCheckIn");
		}
		
		return response;
	}
	
	/**
	 * Build a SimpleObject representation of a NonPatientQueue.
	 */
	private SimpleObject buildNonPatientQueueObject(NonPatientQueue queue) {
		SimpleObject queueObj = new SimpleObject();
		queueObj.add("uuid", queue.getUuid());
		queueObj.add("displayName", queue.getDisplayName());
		queueObj.add("phoneNumber", queue.getPhoneNumber());
		queueObj.add("ticketNumber", queue.getTicketNumber());
		queueObj.add("status", queue.getStatus());
		queueObj.add("dateCreated", queue.getDateCreated());
		return queueObj;
	}
	
	/**
	 * Build a SimpleObject representation of a PatientQueue
	 */
	private SimpleObject buildPatientQueueObject(PatientQueue queue) {
		SimpleObject queueObj = new SimpleObject();
		queueObj.add("uuid", queue.getUuid());
		queueObj.add("visitNumber", queue.getVisitNumber());
		queueObj.add("status", queue.getStatus());
		queueObj.add("dateCreated", queue.getDateCreated());
		return queueObj;
	}
	
	/**
	 * Build a SimpleObject representation of a Patient
	 */
	private SimpleObject buildPatientObject(Patient patient) {
		SimpleObject patientObj = new SimpleObject();
		patientObj.add("uuid", patient.getUuid());
		// Safely get person name - handle potential null
		if (patient.getPersonName() != null) {
			patientObj.add("display", patient.getPersonName().getFullName());
		} else {
			patientObj.add("display", patient.getPatientIdentifier() != null ? patient.getPatientIdentifier()
			        .getIdentifier() : patient.getUuid());
		}
		// Add patient identifier if available
		if (patient.getPatientIdentifier() != null) {
			patientObj.add("patientId", patient.getPatientIdentifier().getIdentifier());
		}
		return patientObj;
	}
	
	/**
	 * Build a SimpleObject representation of a Visit
	 */
	private SimpleObject buildVisitObject(Visit visit) {
		SimpleObject visitObj = new SimpleObject();
		visitObj.add("uuid", visit.getUuid());
		visitObj.add("startDatetime", visit.getStartDatetime());
		visitObj.add("stopDatetime", visit.getStopDatetime());
		if (visit.getVisitType() != null) {
			visitObj.add("visitType", visit.getVisitType().getName());
		}
		return visitObj;
	}
	
	/**
	 * Resolve patient by identifier type and value. Checks if the provided identifier type UUID is
	 * configured as either a person attribute type or patient identifier type, then searches
	 * accordingly.
	 * 
	 * @param identifierTypeUuid the UUID of the identifier type to search by
	 * @param identifierValue the value to search for
	 * @param personAttributeTypeUuidsStr CSV of configured person attribute type UUIDs
	 * @param identifierTypeUuidsStr CSV of configured identifier type UUIDs
	 * @return the matching Patient, or null if not found
	 */
	private Patient resolvePatientByIdentifier(String identifierTypeUuid, String identifierValue,
	        String personAttributeTypeUuidsStr, String identifierTypeUuidsStr) {
		// Check if self check-in is properly configured
		if (personAttributeTypeUuidsStr == null && identifierTypeUuidsStr == null) {
			log.error("Self check-in is not configured. Please contact your system administrator to configure person attribute types and identifier types.");
			throw new IllegalArgumentException(
			        "Self check-in is not configured. Please contact your system administrator to set up the required configurations.");
		}
		
		// Check if it's a configured attribute type (person attribute like phone number)
		if (parseUuidSet(personAttributeTypeUuidsStr).contains(identifierTypeUuid.trim())) {
			log.info("Looking up patient by person attribute type: " + identifierTypeUuid);
			Patient patient = getPatientByPersonAttributes(identifierValue, personAttributeTypeUuidsStr);
			if (patient != null) {
				return patient;
			}
		}
		
		// Check if it's a configured identifier type (patient identifier like National ID)
		if (parseUuidSet(identifierTypeUuidsStr).contains(identifierTypeUuid.trim())) {
			log.info("Looking up patient by identifier type: " + identifierTypeUuid);
			Patient patient = getPatientByIdentifier(identifierValue, identifierTypeUuid);
			if (patient != null) {
				return patient;
			}
		}
		
		log.warn("Identifier type UUID not found in configured types: " + identifierTypeUuid);
		return null;
	}
	
	/**
	 * Find patient by person attribute value using configured attribute types (supports multiple).
	 * Uses efficient database query instead of loading all patients into memory.
	 */
	private Patient getPatientByPersonAttributes(String attributeValue, String attributeTypeUuidsStr) {
		try {
			PatientQueueingService queueingService = Context.getService(PatientQueueingService.class);
			String[] uuids = attributeTypeUuidsStr.split(",");
			
			for (String uuid : uuids) {
				uuid = uuid.trim();
				if (StringUtils.isBlank(uuid)) {
					continue;
				}
				
				PersonAttributeType attributeType = Context.getPersonService().getPersonAttributeTypeByUuid(uuid);
				if (attributeType == null) {
					log.warn("PersonAttributeType not found for UUID: " + uuid);
					continue;
				}
				
				// Use efficient database query instead of loading all patients
				Patient patient = queueingService.getPatientByPersonAttributeValue(attributeType.getPersonAttributeTypeId(),
				    attributeValue);
				
				if (patient != null) {
					return patient;
				}
			}
			
			return null;
		}
		catch (Exception e) {
			log.error("Error finding patient by person attributes", e);
			return null;
		}
	}
	
	/**
	 * Find patient by specific identifier type
	 */
	private Patient getPatientByIdentifier(String identifierValue, String identifierTypeUuid) {
		try {
			org.openmrs.PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(
			    identifierTypeUuid);
			if (identifierType == null) {
				log.warn("Patient identifier type not found for UUID: " + identifierTypeUuid);
				return null;
			}
			
			List<org.openmrs.PatientIdentifier> identifiers = Context.getPatientService().getPatientIdentifiers(
			    identifierValue, Arrays.asList(identifierType), null, null, null);
			
			if (identifiers == null || identifiers.isEmpty()) {
				return null;
			}
			
			if (identifiers.size() > 1) {
				log.warn("Multiple patients found for identifier: " + identifierValue + " in type " + identifierTypeUuid);
			}
			
			return identifiers.get(0).getPatient();
		}
		catch (Exception e) {
			log.error("Error finding patient by identifier type: " + identifierTypeUuid, e);
			return null;
		}
	}
	
	private Integer parsePriority(Object priorityValue) {
		if (priorityValue == null) {
			return null;
		}
		
		try {
			return Integer.parseInt(priorityValue.toString());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private String getRequiredProperty(SimpleObject properties, String key) {
		String value = properties.get(key);
		if (value == null) {
			throw new IllegalArgumentException(key + " cannot be null");
		}
		return value;
	}
	
	private String trimToNull(Object value) {
		if (value == null) {
			return null;
		}
		
		String s = value.toString().trim();
		return s.isEmpty() ? null : s;
	}
	
	private static final java.util.Set<String> EMPTY_UUID_SET = java.util.Collections.emptySet();
	
	/**
	 * Parse a CSV string of UUIDs into a Set for efficient lookups. Empty or blank strings return
	 * an empty set.
	 */
	private java.util.Set<String> parseUuidSet(String csv) {
		if (StringUtils.isBlank(csv)) {
			return EMPTY_UUID_SET;
		}
		java.util.Set<String> uuids = new java.util.HashSet<String>();
		for (String uuid : csv.split(",")) {
			String trimmed = uuid.trim();
			if (StringUtils.isNotBlank(trimmed)) {
				uuids.add(trimmed);
			}
		}
		return uuids;
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public SelfCheckIn getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public NeedsPaging<SelfCheckIn> doGetAll(RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("checkInType");
		description.addProperty("uuid");
		description.addProperty("ticketNumber");
		description.addProperty("patientQueue");
		description.addProperty("nonPatientQueue");
		description.addProperty("patient");
		description.addProperty("displayName");
		description.addProperty("phoneNumber");
		description.addProperty("visit");
		description.addSelfLink();
		
		if (rep instanceof FullRepresentation) {
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		}
		
		return description;
	}
	
	@Override
	protected void delete(SelfCheckIn delegate, String s, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public void purge(SelfCheckIn delegate, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("identifierTypeUuid");
		description.addProperty("identifierValue");
		description.addProperty("phoneNumber");
		description.addProperty("displayName");
		description.addProperty("queueTypeUuid");
		description.addProperty("currentLocation");
		description.addProperty("locationTo");
		description.addProperty("queueRoom");
		description.addProperty("priority");
		return description;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException("Operation not supported");
	}
}
