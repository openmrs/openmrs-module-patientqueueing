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

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.web.customdto.ProviderQueueEntry;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Provider queue management endpoint. Provides UgandaEMR-compatible provider dashboard
 * functionality with unified patient and non-patient queue support. Paths: - GET
 * /ws/rest/v1/providerqueuebylocation?uuid={locationUuid} - GET
 * /ws/rest/v1/providerqueuebylocation/:uuid - POST /ws/rest/v1/providerqueuebylocation (with uuid
 * in body) Supports operations: - Update queue status and priority - Forward patients between
 * locations - Unified patient and non-patient queue management
 */
@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/providerqueuebylocation", supportedClass = ProviderQueueEntry.class, supportedOpenmrsVersions = { "1.8 - 9.0.*" })
public class ProviderQueueResource extends DelegatingCrudResource<ProviderQueueEntry> {
	
	private PatientQueueingService patientQueueingService() {
		return Context.getService(PatientQueueingService.class);
	}
	
	private LocationService locationService() {
		return Context.getLocationService();
	}
	
	@Override
	public ProviderQueueEntry newDelegate() {
		return new ProviderQueueEntry();
	}
	
	@Override
	@Authorized("Task: patientqueueing.manageQueue")
	public ProviderQueueEntry save(ProviderQueueEntry delegate) {
		if (delegate.getUuid() == null || delegate.getUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("uuid is required");
		}
		
		Boolean isPatient = delegate.isPatient();
		if (isPatient == null && delegate.getSourceType() != null) {
			isPatient = "PATIENT_QUEUE".equalsIgnoreCase(delegate.getSourceType());
		}
		
		if ("FORWARD".equalsIgnoreCase(delegate.getAction())) {
			if (Boolean.TRUE.equals(isPatient)) {
				return forwardPatientQueue(delegate);
			} else {
				return forwardNonPatientQueue(delegate);
			}
		}
		
		if (Boolean.TRUE.equals(isPatient)) {
			return updatePatientQueue(delegate);
		} else {
			return updateNonPatientQueue(delegate);
		}
	}
	
	@Override
	@Authorized("Task: patientqueueing.viewQueue")
	public ProviderQueueEntry getByUniqueId(String uniqueId) {
		PatientQueue patientQueue = patientQueueingService().getPatientQueueByUuid(uniqueId);
		if (patientQueue != null) {
			return toDto(patientQueue);
		}
		
		NonPatientQueue nonPatientQueue = patientQueueingService().getNonPatientQueueByUuid(uniqueId);
		if (nonPatientQueue != null) {
			return toDto(nonPatientQueue);
		}
		
		return null;
	}
	
	@Override
	protected void delete(ProviderQueueEntry entry, String s, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Delete not supported");
	}
	
	@Override
	public void purge(ProviderQueueEntry entry, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("Purge not supported");
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("queueNumber");
			description.addProperty("displayName");
			description.addProperty("identifiedBy");
			description.addProperty("currentLocation");
			description.addProperty("serviceLocation");
			description.addProperty("status");
			description.addProperty("priority");
			description.addProperty("dateCreated");
			description.addProperty("patient");
			description.addProperty("sourceType");
			description.addProperty("locationToUuid");
			description.addProperty("queueRoomUuid");
			description.addProperty("comment");
			description.addProperty("action");
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		throw new ResourceDoesNotSupportOperationException("Create is not supported here");
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("patient");
		description.addProperty("sourceType");
		description.addProperty("status");
		description.addProperty("priority");
		description.addProperty("locationToUuid");
		description.addProperty("queueRoomUuid");
		description.addProperty("comment");
		description.addProperty("action");
		return description;
	}
	
	/**
	 * Get provider's queue for a location. Returns unified list of both patient and non-patient
	 * queues sorted by status, priority, and creation time.
	 */
	@Override
	public PageableResult doGetAll(RequestContext context) {
		String uuid = context.getParameter("uuid");
		if (uuid == null || uuid.trim().isEmpty()) {
			throw new IllegalArgumentException("uuid is required");
		}
		
		Location queueRoom = locationService().getLocationByUuid(uuid);
		if (queueRoom == null) {
			throw new IllegalArgumentException("Location not found for uuid=" + uuid);
		}
		
		Date target = new Date();
		Date from = OpenmrsUtil.firstSecondOfDay(target);
		Date to = OpenmrsUtil.getLastMomentOfDay(target);
		
		List<ProviderQueueEntry> rows = new ArrayList<ProviderQueueEntry>();
		
		List<PatientQueue> patientQueues = patientQueueingService().getPatientQueueListFifo(null, from, to, null, null,
		    null, null, queueRoom);
		
		if (patientQueues != null) {
			for (PatientQueue pq : patientQueues) {
				rows.add(toDto(pq));
			}
		}
		
		List<NonPatientQueue> nonPatientQueues = patientQueueingService().getNonPatientQueues(null, null, null, queueRoom,
		// Fixed: queueRoom is 4th param (was incorrectly 3rd), locationTo is 3rd param (null here)
		    from, to);
		
		if (nonPatientQueues != null) {
			for (NonPatientQueue npq : nonPatientQueues) {
				rows.add(toDto(npq));
			}
		}
		
		Collections.sort(rows, new Comparator<ProviderQueueEntry>() {
			
			@Override
			public int compare(ProviderQueueEntry a, ProviderQueueEntry b) {
				int sa = statusRank(a.getStatus());
				int sb = statusRank(b.getStatus());
				
				int statusCompare = Integer.compare(sa, sb);
				if (statusCompare != 0) {
					return statusCompare;
				}
				
				Integer pa = a.getPriority() != null ? a.getPriority() : 0;
				Integer pb = b.getPriority() != null ? b.getPriority() : 0;
				
				int priorityCompare = Integer.compare(pb, pa);
				if (priorityCompare != 0) {
					return priorityCompare;
				}
				
				Date da = a.getDateCreated();
				Date db = b.getDateCreated();
				
				if (da == null && db == null)
					return 0;
				if (da == null)
					return 1;
				if (db == null)
					return -1;
				
				return da.compareTo(db);
			}
		});
		
		return new AlreadyPaged<ProviderQueueEntry>(context, rows, false);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		return doGetAll(context);
	}
	
	private ProviderQueueEntry toDto(PatientQueue pq) {
		ProviderQueueEntry dto = new ProviderQueueEntry(pq);
		// Override status with normalized version for API
		dto.setStatus(mapPatientStatus(pq.getStatus()));
		// Override identifiedBy with more detailed version
		dto.setIdentifiedBy(resolvePatientIdentifierLabel(pq));
		return dto;
	}
	
	private ProviderQueueEntry toDto(NonPatientQueue npq) {
		ProviderQueueEntry dto = new ProviderQueueEntry(npq);
		// Override status with normalized version for API
		dto.setStatus(mapNonPatientStatus(npq.getStatus()));
		return dto;
	}
	
	private String resolvePatientIdentifierLabel(PatientQueue pq) {
		if (pq.getPatient() != null && pq.getPatient().getActiveIdentifiers() != null
		        && !pq.getPatient().getActiveIdentifiers().isEmpty()
		        && pq.getPatient().getActiveIdentifiers().iterator().next().getIdentifierType() != null) {
			return pq.getPatient().getActiveIdentifiers().iterator().next().getIdentifierType().getName();
		}
		return "Patient Record";
	}
	
	private String mapPatientStatus(PatientQueue.Status status) {
		if (status == null)
			return "WAITING";
		if (status == PatientQueue.Status.PENDING)
			return "WAITING";
		if (status == PatientQueue.Status.PICKED)
			return "CALLED";
		return status.name();
	}
	
	private String mapNonPatientStatus(NonPatientQueue.NonPatientQueueStatus status) {
		if (status == null)
			return "WAITING";
		if (status == NonPatientQueue.NonPatientQueueStatus.WAITING)
			return "WAITING";
		if (status == NonPatientQueue.NonPatientQueueStatus.CALLED)
			return "CALLED";
		if (status == NonPatientQueue.NonPatientQueueStatus.SERVING)
			return "IN_SERVICE";
		return status.name();
	}
	
	private PatientQueue.Status toPatientQueueStatus(String status) {
		String s = status != null ? status.trim().toUpperCase() : "WAITING";
		
		if ("WAITING".equals(s))
			return PatientQueue.Status.PENDING;
		if ("CALLED".equals(s))
			return PatientQueue.Status.PICKED;
		if ("IN_SERVICE".equals(s))
			return PatientQueue.Status.PICKED;
		
		try {
			return PatientQueue.Status.valueOf(s);
		}
		catch (Exception e) {
			return PatientQueue.Status.PENDING;
		}
	}
	
	private NonPatientQueue.NonPatientQueueStatus toNonPatientQueueStatus(String status) {
		String s = status != null ? status.trim().toUpperCase() : "WAITING";
		
		if ("IN_SERVICE".equals(s)) {
			return NonPatientQueue.NonPatientQueueStatus.SERVING;
		}
		
		try {
			return NonPatientQueue.NonPatientQueueStatus.valueOf(s);
		}
		catch (Exception e) {
			return NonPatientQueue.NonPatientQueueStatus.WAITING;
		}
	}
	
	private int statusRank(String status) {
		String s = status != null ? status.toUpperCase() : "";
		
		if ("IN_SERVICE".equals(s))
			return 0;
		if ("CALLED".equals(s))
			return 1;
		if ("WAITING".equals(s))
			return 2;
		if ("SKIPPED".equals(s))
			return 3;
		if ("COMPLETED".equals(s))
			return 4;
		
		return 5;
	}
	
	private ProviderQueueEntry updatePatientQueue(ProviderQueueEntry dto) {
		PatientQueue pq = patientQueueingService().getPatientQueueByUuid(dto.getUuid());
		if (pq == null) {
			throw new IllegalArgumentException("PatientQueue not found for uuid=" + dto.getUuid());
		}
		
		if (dto.getStatus() != null) {
			pq.setStatus(toPatientQueueStatus(dto.getStatus()));
			if (PatientQueue.Status.PICKED.equals(pq.getStatus())) {
				pq.setDatePicked(new Date());
			}
			if (PatientQueue.Status.COMPLETED.equals(pq.getStatus())) {
				pq.setDateCompleted(new Date());
			}
		}
		
		if (dto.getPriority() != null) {
			pq.setPriority(dto.getPriority());
		}
		
		if (dto.getLocationToUuid() != null && !dto.getLocationToUuid().trim().isEmpty()) {
			Location locationTo = locationService().getLocationByUuid(dto.getLocationToUuid());
			if (locationTo == null) {
				throw new IllegalArgumentException("locationTo not found for uuid=" + dto.getLocationToUuid());
			}
			pq.setLocationTo(locationTo);
		}
		
		if (dto.getQueueRoomUuid() != null && !dto.getQueueRoomUuid().trim().isEmpty()) {
			Location queueRoom = locationService().getLocationByUuid(dto.getQueueRoomUuid());
			if (queueRoom == null) {
				throw new IllegalArgumentException("queueRoom not found for uuid=" + dto.getQueueRoomUuid());
			}
			pq.setQueueRoom(queueRoom);
		}
		
		if (dto.getComment() != null) {
			pq.setComment(dto.getComment());
		}
		
		patientQueueingService().savePatientQue(pq);
		return toDto(pq);
	}
	
	private ProviderQueueEntry updateNonPatientQueue(ProviderQueueEntry dto) {
		NonPatientQueue npq = patientQueueingService().getNonPatientQueueByUuid(dto.getUuid());
		if (npq == null) {
			throw new IllegalArgumentException("NonPatientQueue not found for uuid=" + dto.getUuid());
		}
		
		if (dto.getStatus() != null) {
			npq.setStatus(toNonPatientQueueStatus(dto.getStatus()));
			if (NonPatientQueue.NonPatientQueueStatus.CALLED.equals(npq.getStatus())) {
				npq.setCalledAt(new Date());
			}
			if (NonPatientQueue.NonPatientQueueStatus.COMPLETED.equals(npq.getStatus())) {
				npq.setEndedAt(new Date());
			}
		}
		
		if (dto.getPriority() != null) {
			npq.setPriority(dto.getPriority());
		}
		
		if (dto.getLocationToUuid() != null && !dto.getLocationToUuid().trim().isEmpty()) {
			Location locationTo = locationService().getLocationByUuid(dto.getLocationToUuid());
			if (locationTo == null) {
				throw new IllegalArgumentException("locationTo not found for uuid=" + dto.getLocationToUuid());
			}
			npq.setLocationTo(locationTo);
		}
		
		if (dto.getQueueRoomUuid() != null && !dto.getQueueRoomUuid().trim().isEmpty()) {
			Location queueRoom = locationService().getLocationByUuid(dto.getQueueRoomUuid());
			if (queueRoom == null) {
				throw new IllegalArgumentException("queueRoom not found for uuid=" + dto.getQueueRoomUuid());
			}
			npq.setQueueRoom(queueRoom);
		}
		
		if (dto.getComment() != null) {
			npq.setComment(dto.getComment());
		}
		
		patientQueueingService().saveNonPatientQueue(npq);
		return toDto(npq);
	}
	
	private ProviderQueueEntry forwardPatientQueue(ProviderQueueEntry dto) {
		PatientQueue current = patientQueueingService().getPatientQueueByUuid(dto.getUuid());
		if (current == null) {
			throw new IllegalArgumentException("PatientQueue not found for uuid=" + dto.getUuid());
		}
		
		Location nextLocation = requireLocation(dto.getLocationToUuid(), "locationToUuid");
		Location nextQueueRoom = requireLocation(dto.getQueueRoomUuid(), "queueRoomUuid");
		
		current.setStatus(PatientQueue.Status.COMPLETED);
		current.setDateCompleted(new Date());
		if (dto.getComment() != null) {
			current.setComment(dto.getComment());
		}
		patientQueueingService().savePatientQue(current);
		
		PatientQueue next = new PatientQueue();
		next.setPatient(current.getPatient());
		next.setProvider(current.getProvider());
		next.setLocationFrom(current.getLocationTo() != null ? current.getLocationTo() : current.getQueueRoom());
		next.setLocationTo(nextLocation);
		next.setEncounter(current.getEncounter());
		next.setStatus(PatientQueue.Status.PENDING);
		next.setVisitNumber(current.getVisitNumber());
		next.setPriority(current.getPriority());
		next.setPriorityComment(current.getPriorityComment());
		next.setComment(dto.getComment() != null ? dto.getComment() : current.getComment());
		next.setQueueRoom(nextQueueRoom);
		next.setDatePicked(null);
		next.setDateCompleted(null);
		
		patientQueueingService().savePatientQue(next);
		return toDto(next);
	}
	
	private ProviderQueueEntry forwardNonPatientQueue(ProviderQueueEntry dto) {
		NonPatientQueue current = patientQueueingService().getNonPatientQueueByUuid(dto.getUuid());
		if (current == null) {
			throw new IllegalArgumentException("NonPatientQueue not found for uuid=" + dto.getUuid());
		}
		
		Location nextLocation = requireLocation(dto.getLocationToUuid(), "locationToUuid");
		Location nextQueueRoom = requireLocation(dto.getQueueRoomUuid(), "queueRoomUuid");
		
		current.setStatus(NonPatientQueue.NonPatientQueueStatus.COMPLETED);
		current.setEndedAt(new Date());
		if (dto.getComment() != null) {
			current.setComment(dto.getComment());
		}
		patientQueueingService().saveNonPatientQueue(current);
		
		NonPatientQueue next = new NonPatientQueue();
		next.setTicketNumber(current.getTicketNumber());
		next.setDisplayName(current.getDisplayName());
		next.setPhoneNumber(current.getPhoneNumber());
		next.setQueueType(current.getQueueType());
		next.setStatus(NonPatientQueue.NonPatientQueueStatus.WAITING);
		next.setCurrentLocation(current.getLocationTo() != null ? current.getLocationTo() : current.getQueueRoom());
		next.setLocationTo(nextLocation);
		next.setQueueRoom(nextQueueRoom);
		next.setPriority(current.getPriority());
		next.setComment(dto.getComment() != null ? dto.getComment() : current.getComment());
		next.setCalledBy(null);
		next.setServedBy(null);
		next.setCalledAt(null);
		next.setArrivedAt(null);
		next.setStartedAt(null);
		next.setEndedAt(null);
		
		patientQueueingService().saveNonPatientQueue(next);
		return toDto(next);
	}
	
	private Location requireLocation(String uuid, String fieldName) {
		if (uuid == null || uuid.trim().isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required");
		}
		
		Location location = locationService().getLocationByUuid(uuid);
		if (location == null) {
			throw new IllegalArgumentException("Location not found for uuid=" + uuid);
		}
		
		return location;
	}
}
