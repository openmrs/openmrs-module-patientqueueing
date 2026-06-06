package org.openmrs.module.patientqueueing.web.resource;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/nonpatientqueue", supportedClass = NonPatientQueue.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class NonPatientQueueResource extends DelegatingCrudResource<NonPatientQueue> {
	
	@Override
	public NonPatientQueue newDelegate() {
		return new NonPatientQueue();
	}
	
	@Override
	public NonPatientQueue save(NonPatientQueue nonPatientQueue) {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		
		// Check if this is a new entry (no UUID) or an update
		if (nonPatientQueue.getUuid() == null || nonPatientQueue.getId() == null) {
			// New entry - use createNonPatientQueueEntry to ensure ticket number generation
			return service.createNonPatientQueueEntry(nonPatientQueue.getDisplayName(), nonPatientQueue.getPhoneNumber(),
			    nonPatientQueue.getQueueType(), nonPatientQueue.getCurrentLocation(), nonPatientQueue.getLocationTo(),
			    nonPatientQueue.getQueueRoom(), nonPatientQueue.getPriority(), nonPatientQueue.getComment());
		} else {
			// Existing entry - update directly without generating new ticket
			return service.saveNonPatientQueue(nonPatientQueue);
		}
	}
	
	@Override
	public NonPatientQueue getByUniqueId(String uniqueId) {
		NonPatientQueue queue = null;
		Integer id = null;
		
		queue = Context.getService(PatientQueueingService.class).getNonPatientQueueByUuid(uniqueId);
		if (queue == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}
			
			if (id != null) {
				queue = Context.getService(PatientQueueingService.class).getNonPatientQueueById(id);
			}
		}
		
		return queue;
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		NonPatientQueue queue = getByUniqueId(uuid);
		if (queue == null) {
			throw new IllegalArgumentException("NonPatientQueue not found");
		}
		
		NonPatientQueueResource resource = (NonPatientQueueResource) Context.getService(RestService.class)
		        .getResourceBySupportedClass(NonPatientQueue.class);
		resource.setConvertedProperties(queue, propertiesToUpdate, resource.getUpdatableProperties(), false);
		
		queue = save(queue);
		return ConversionUtil.convertToRepresentation(queue, Representation.DEFAULT);
	}
	
	@Override
	public NeedsPaging<NonPatientQueue> doGetAll(RequestContext context) throws ResponseException {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		
		String statusParam = context.getParameter("status");
		String queueTypeParam = context.getParameter("queueType");
		String locationToParam = context.getParameter("locationTo");
		String queueRoomParam = context.getParameter("queueRoom");
		String fromDateParam = context.getParameter("fromDate");
		String toDateParam = context.getParameter("toDate");
		boolean includeHistory = Boolean.parseBoolean(context.getParameter("includeHistory"));
		
		NonPatientQueue.NonPatientQueueStatus status = null;
		if (statusParam != null && !statusParam.isEmpty()) {
			try {
				status = NonPatientQueue.NonPatientQueueStatus.valueOf(statusParam.toUpperCase());
			}
			catch (IllegalArgumentException e) {
				// Invalid status, ignore
			}
		}
		
		Concept queueType = null;
		if (queueTypeParam != null && !queueTypeParam.isEmpty()) {
			queueType = Context.getConceptService().getConceptByUuid(queueTypeParam);
		}
		
		Location locationTo = null;
		if (locationToParam != null && !locationToParam.isEmpty()) {
			locationTo = Context.getLocationService().getLocationByUuid(locationToParam);
		}
		
		Location queueRoom = null;
		if (queueRoomParam != null && !queueRoomParam.isEmpty()) {
			queueRoom = Context.getLocationService().getLocationByUuid(queueRoomParam);
		}
		
		Date fromDate = null;
		Date toDate = null;
		if (fromDateParam != null && toDateParam != null) {
			try {
				fromDate = OpenmrsUtil.firstSecondOfDay(new Date(Long.parseLong(fromDateParam)));
				toDate = OpenmrsUtil.getLastMomentOfDay(new Date(Long.parseLong(toDateParam)));
			}
			catch (NumberFormatException e) {
				// Invalid date format, use today
				fromDate = OpenmrsUtil.firstSecondOfDay(new Date());
				toDate = OpenmrsUtil.getLastMomentOfDay(new Date());
			}
		} else {
			// Default to today
			fromDate = OpenmrsUtil.firstSecondOfDay(new Date());
			toDate = OpenmrsUtil.getLastMomentOfDay(new Date());
		}
		
		List<NonPatientQueue> queues = service.getNonPatientQueues(status, queueType, locationTo, queueRoom, fromDate,
		    toDate);
		
		// Filter out COMPLETED entries unless includeHistory is true
		if (!includeHistory && queues != null && status != NonPatientQueue.NonPatientQueueStatus.COMPLETED) {
			List<NonPatientQueue> filtered = new ArrayList<NonPatientQueue>();
			for (NonPatientQueue queue : queues) {
				if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.COMPLETED) {
					filtered.add(queue);
				}
			}
			queues = filtered;
		}
		
		return new NeedsPaging<NonPatientQueue>(queues, context);
	}
	
	@Override
	public List<Representation> getAvailableRepresentations() {
		return Arrays.asList(Representation.DEFAULT, Representation.FULL);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("displayName");
			description.addProperty("phoneNumber");
			description.addProperty("queueType", Representation.REF);
			description.addProperty("currentLocation", Representation.REF);
			description.addProperty("locationTo", Representation.REF);
			description.addProperty("queueRoom", Representation.REF);
			description.addProperty("status");
			description.addProperty("priority");
			description.addProperty("comment");
			description.addProperty("ticketNumber");
			description.addProperty("calledBy", Representation.REF);
			description.addProperty("calledAt");
			description.addProperty("arrivedAt");
			description.addProperty("servedBy", Representation.REF);
			description.addProperty("startedAt");
			description.addProperty("endedAt");
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("displayName");
			description.addProperty("phoneNumber");
			description.addProperty("queueType");
			description.addProperty("currentLocation");
			description.addProperty("locationTo");
			description.addProperty("queueRoom");
			description.addProperty("status");
			description.addProperty("priority");
			description.addProperty("comment");
			description.addProperty("ticketNumber");
			description.addProperty("calledBy");
			description.addProperty("calledAt");
			description.addProperty("arrivedAt");
			description.addProperty("servedBy");
			description.addProperty("startedAt");
			description.addProperty("endedAt");
			description.addProperty("creator");
			description.addProperty("dateCreated");
			description.addProperty("changedBy");
			description.addProperty("dateChanged");
			description.addProperty("voided");
			description.addProperty("dateVoided");
			description.addProperty("voidedBy");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("displayName");
			description.addProperty("ticketNumber");
			description.addProperty("status");
			description.addProperty("queueRoom");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected void delete(NonPatientQueue nonPatientQueue, String s, RequestContext requestContext) throws ResponseException {
		// Non-patient queues are not deleted, they are cancelled
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(NonPatientQueue nonPatientQueue, RequestContext requestContext) throws ResponseException {
		// Non-patient queues are not purged
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("displayName");
		description.addProperty("phoneNumber");
		description.addProperty("queueType");
		description.addProperty("currentLocation");
		description.addProperty("locationTo");
		description.addProperty("queueRoom");
		description.addProperty("priority");
		description.addProperty("comment");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("displayName");
		description.addProperty("phoneNumber");
		description.addProperty("queueType");
		description.addProperty("currentLocation");
		description.addProperty("locationTo");
		description.addProperty("queueRoom");
		description.addProperty("priority");
		description.addProperty("comment");
		return description;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		
		String ticketNumber = context.getParameter("ticketNumber");
		String queueRoomParam = context.getParameter("queueRoom");
		String statusParam = context.getParameter("status");
		String fromDateParam = context.getParameter("fromDate");
		String toDateParam = context.getParameter("toDate");
		boolean includeHistory = Boolean.parseBoolean(context.getParameter("includeHistory"));
		
		Location queueRoom = null;
		if (queueRoomParam != null && !queueRoomParam.isEmpty()) {
			queueRoom = Context.getLocationService().getLocationByUuid(queueRoomParam);
		}
		
		NonPatientQueue.NonPatientQueueStatus status = null;
		if (statusParam != null && !statusParam.isEmpty()) {
			try {
				status = NonPatientQueue.NonPatientQueueStatus.valueOf(statusParam.toUpperCase());
			}
			catch (IllegalArgumentException e) {
				// Invalid status, ignore
			}
		}
		
		Date fromDate = null;
		Date toDate = null;
		if (fromDateParam != null && toDateParam != null) {
			try {
				fromDate = new Date(Long.parseLong(fromDateParam));
				toDate = new Date(Long.parseLong(toDateParam));
			}
			catch (NumberFormatException e) {
				// Invalid date format, use today
				fromDate = OpenmrsUtil.firstSecondOfDay(new Date());
				toDate = OpenmrsUtil.getLastMomentOfDay(new Date());
			}
		} else {
			// Default to today
			fromDate = OpenmrsUtil.firstSecondOfDay(new Date());
			toDate = OpenmrsUtil.getLastMomentOfDay(new Date());
		}
		
		List<NonPatientQueue> results = new ArrayList<NonPatientQueue>();
		
		if (ticketNumber != null && !ticketNumber.isEmpty()) {
			// Search by ticket number
			results = service.getNonPatientQueueByTicketNumber(ticketNumber, fromDate, toDate);
		} else if (queueRoom != null) {
			// Search by queue room
			if (status != null) {
				results = service.getNonPatientQueuesByQueueRoomAndStatus(queueRoom, status);
			} else {
				results = service.getNonPatientQueuesByQueueRoom(queueRoom, fromDate, toDate);
			}
		} else {
			// Get all active queues
			results = service.getAllActiveNonPatientQueues();
		}
		
		// Filter out COMPLETED entries unless includeHistory is true
		if (!includeHistory && results != null && status != NonPatientQueue.NonPatientQueueStatus.COMPLETED) {
			List<NonPatientQueue> filtered = new ArrayList<NonPatientQueue>();
			for (NonPatientQueue queue : results) {
				if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.COMPLETED) {
					filtered.add(queue);
				}
			}
			results = filtered;
		}
		
		return new NeedsPaging<NonPatientQueue>(results, context);
	}
	
}
