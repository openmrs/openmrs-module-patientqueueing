package org.openmrs.module.patientqueueing.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.IntegerProperty;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
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
import org.openmrs.module.webservices.validation.ValidateUtil;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/patientqueue", supportedClass = PatientQueue.class, supportedOpenmrsVersions = {
        "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*", "2.5.*" })
public class PatientQueueResource extends DelegatingCrudResource<PatientQueue> {

	@Override
	public PatientQueue newDelegate() {
		return new PatientQueue();
	}

	@Override
	public PatientQueue save(PatientQueue patientQueue) {
		PatientQueueingService patientQueueingService=Context.getService(PatientQueueingService.class);
		patientQueue = patientQueueingService.assignVisitNumberForToday(patientQueue);
		return Context.getService(PatientQueueingService.class).savePatientQue(patientQueue);
	}
	
	@Override
	public PatientQueue getByUniqueId(String uniqueId) {
		PatientQueue patientQueue = null;
		Integer id = null;
		
		patientQueue = Context.getService(PatientQueueingService.class).getPatientQueueByUuid(uniqueId);
		if (patientQueue == null && uniqueId != null) {
			try {
				id = Integer.parseInt(uniqueId);
			}
			catch (Exception e) {}
			
			if (id != null) {
				patientQueue = Context.getService(PatientQueueingService.class).getPatientQueueById(id);
			}
		}
		
		return patientQueue;
	}
	
	@Override
	public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
		String status=propertiesToUpdate.get("status");
		String datePicked=propertiesToUpdate.get("datePicked");
		String dateCompleted=propertiesToUpdate.get("dateCompleted");

		if (status == null) {
			return super.update(uuid, propertiesToUpdate, context);
		}

		PatientQueue patientQueue = getPatientQueueForUpdate(uuid, propertiesToUpdate);
		ValidateUtil.validate(patientQueue);
		patientQueue.setDateChanged(new Date());

        if (status.equals("picked") && datePicked==null) {
            patientQueue.setDatePicked(new Date());
        } else if (status.equals("completed") && dateCompleted==null) {
            patientQueue.setDateCompleted(new Date());
        } else if (status.equals("pending") && patientQueue.getDateCompleted() != null) {
            patientQueue.setDateCompleted(null);
        } else if (status.equals("pending") && patientQueue.getDatePicked() != null) {
            patientQueue.setDatePicked(null);
        }

		patientQueue = save(patientQueue);
		return ConversionUtil.convertToRepresentation(patientQueue, Representation.DEFAULT);
	}

	public PatientQueue getPatientQueueForUpdate(String uuid, Map<String, Object> propertiesToUpdate) {
		PatientQueue patientQueue = getByUniqueId(uuid);
		PatientQueueResource patientQueueResource = (PatientQueueResource) Context.getService(RestService.class)
		        .getResourceBySupportedClass(PatientQueue.class);
		patientQueueResource.setConvertedProperties(patientQueue, propertiesToUpdate,
		    patientQueueResource.getUpdatableProperties(), false);
		return patientQueue;
	}

	@Override
	public NeedsPaging<PatientQueue> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<PatientQueue>(new ArrayList<PatientQueue>(Context.getService(PatientQueueingService.class)
		        .getPatientQueueList(null, null, null, null, null, null, null)), context);
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
			description.addProperty("patient", Representation.REF);
			description.addProperty("datePicked");
			description.addProperty("dateCompleted");
			description.addProperty("locationFrom", Representation.REF);
			description.addProperty("locationTo", Representation.REF);
			description.addProperty("provider", Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("status");
			description.addProperty("priority");
			description.addProperty("priorityComment");
			description.addProperty("visitNumber");
			description.addProperty("comment");
			description.addProperty("queueRoom", Representation.REF);
			
			description.addSelfLink();
			
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("creator");
			description.addProperty("dateCreated");
			description.addProperty("changedBy");
			description.addProperty("dateChanged");
			description.addProperty("voided");
			description.addProperty("dateVoided");
			description.addProperty("voidedBy");
			description.addProperty("patient");
			description.addProperty("provider");
			description.addProperty("locationFrom");
			description.addProperty("locationTo");
			description.addProperty("encounter");
			description.addProperty("status");
			description.addProperty("priority");
			description.addProperty("priorityComment");
			description.addProperty("visitNumber");
			description.addProperty("comment");
			description.addProperty("queueRoom");
			description.addProperty("datePicked");
			description.addProperty("dateCompleted");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("visitNumber");
			description.addProperty("dateCreated");
			description.addProperty("dateChanged");
			description.addProperty("locationFrom");
			description.addProperty("locationTo");
			description.addProperty("status");
			description.addProperty("queueRoom");
			description.addProperty("datePicked");
			description.addProperty("dateCompleted");
			description.addProperty("provider");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	protected void delete(PatientQueue patientQueue, String s, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public void purge(PatientQueue patientQueue, RequestContext requestContext) throws ResponseException {
		
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient");
		description.addProperty("provider");
		description.addProperty("locationTo");
		description.addProperty("locationFrom");
		description.addProperty("status");
		description.addProperty("encounter");
		description.addProperty("visitNumber");
		description.addProperty("priority");
		description.addProperty("queueRoom");
		description.addProperty("provider");
		description.addProperty("datePicked");
		description.addProperty("dateCompleted");
		description.addProperty("priorityComment");
		description.addProperty("comment");

		return description;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		String locationQuery = context.getParameter("location");
		String parentLocationQuery = context.getParameter("parentLocation");
		boolean onlyInQueueRooms = Boolean.parseBoolean(context.getParameter("onlyInQueueRooms"));
		String status = context.getParameter("status");
		String queueRoomQuery = context.getParameter("room");
		PatientQueue.Status queueStatus = null;
		
		Location location = null;
		Location room = null;
		
		if (locationQuery != null && !locationQuery.equals("")) {
			location = Context.getLocationService().getLocationByUuid(locationQuery);
		}
		
		if (status != null && status.equals("pending")) {
			queueStatus = PatientQueue.Status.PENDING;
		} else if (status != null && status.equals("completed")) {
			queueStatus = PatientQueue.Status.COMPLETED;
		} else if (status != null && status.equals("picked")) {
			queueStatus = PatientQueue.Status.PICKED;
		}
		
		if (queueRoomQuery != null) {
			room = Context.getLocationService().getLocationByUuid(queueRoomQuery);
		}
		
		List<PatientQueue> PatientQueuesByQuery = null;

		if (parentLocationQuery != null && !parentLocationQuery.equals("")) {
			PatientQueuesByQuery = patientQueueingService.getPatientQueueByParentLocation(Context.getLocationService()
			        .getLocationByUuid(parentLocationQuery), queueStatus, OpenmrsUtil.firstSecondOfDay(new Date()),
			    OpenmrsUtil.getLastMomentOfDay(new Date()),onlyInQueueRooms);
		} else {
			PatientQueuesByQuery = patientQueueingService.getPatientQueueListBySearchParams(
			    context.getParameter("searchString"), OpenmrsUtil.firstSecondOfDay(new Date()),
			    OpenmrsUtil.getLastMomentOfDay(new Date()), location, null, queueStatus, room);
		}

		return new NeedsPaging<PatientQueue>(PatientQueuesByQuery, context);
	}

	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("provider");
		description.addProperty("status");
		description.addProperty("encounter");
		description.addProperty("queueRoom");
		description.addProperty("datePicked");
		description.addProperty("dateCompleted");
		description.addProperty("dateChanged");
		description.addProperty("comment");
		description.addProperty("priorityComment");
		description.addProperty("priority");
		return description;
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("dateCreated", new DateProperty())
			        .property("voided", new BooleanProperty()).property("priority", new IntegerProperty())
			        .property("priorityComment", new StringProperty()).property("visitNumber", new StringProperty())
			        .property("comment", new StringProperty()).property("status", new StringProperty())
			        .property("datePicked", new DateProperty()).property("dateCompleted", new DateProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("creator", new RefProperty("#/definitions/UserGetRef"))
			        .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("voidedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("provider", new RefProperty("#/definitions/ProviderGetRef"))
			        .property("locationFrom", new RefProperty("#/definitions/LocationGetRef"))
			        .property("locationTo", new RefProperty("#/definitions/LocationGetRef"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGetRef"));
			
		} else if (rep instanceof FullRepresentation) {
			model.property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("creator", new RefProperty("#/definitions/UserGetRef"))
			        .property("changedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("voidedBy", new RefProperty("#/definitions/UserGetRef"))
			        .property("provider", new RefProperty("#/definitions/ProviderGetRef"))
			        .property("locationFrom", new RefProperty("#/definitions/LocationGetRef"))
			        .property("locationTo", new RefProperty("#/definitions/LocationGetRef"))
			        .property("encounter", new RefProperty("#/definitions/EncounterGetRef"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model.property("uuid", new StringProperty()).property("dateCreated", new DateProperty())
			        .property("voided", new BooleanProperty()).property("status", new StringProperty())
			        .property("priority", new IntegerProperty()).property("priorityComment", new StringProperty())
			        .property("visitNumber", new StringProperty()).property("comment", new StringProperty())
			        .property("status", new StringProperty()).property("datePicked", new DateProperty())
			        .property("dateCompleted", new DateProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model.property("patient", new RefProperty("#/definitions/PatientCreate"))
			        .property("creator", new RefProperty("#/definitions/UserCreate"))
			        .property("changedBy", new RefProperty("#/definitions/UserCreate"))
			        .property("voidedBy", new RefProperty("#/definitions/UserCreate"))
			        .property("provider", new RefProperty("#/definitions/ProviderCreate"))
			        .property("locationFrom", new RefProperty("#/definitions/LocationCreate"))
			        .property("locationTo", new RefProperty("#/definitions/LocationCreate"))
			        .property("encounter", new RefProperty("#/definitions/EncounterCreate"));
			
		} else if (rep instanceof FullRepresentation) {
			model.property("patient", new RefProperty("#/definitions/PatientCreate"))
			        .property("creator", new RefProperty("#/definitions/UserCreate"))
			        .property("changedBy", new RefProperty("#/definitions/UserCreate"))
			        .property("voidedBy", new RefProperty("#/definitions/UserCreate"))
			        .property("provider", new RefProperty("#/definitions/ProviderCreate"))
			        .property("locationFrom", new RefProperty("#/definitions/LocationCreate"))
			        .property("locationTo", new RefProperty("#/definitions/LocationCreate"))
			        .property("encounter", new RefProperty("#/definitions/EncounterCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
				.property("status", new StringProperty())
				.property("priority", new IntegerProperty())
		        .property("priorityComment", new StringProperty())
				.property("comment", new StringProperty())
				.property("encounter", new StringProperty())
		        .property("status", new StringProperty())
				.property("datePicked", new DateProperty())
		        .property("dateCompleted", new DateProperty())
		        .property("provider", new RefProperty("#/definitions/ProviderCreate"))
				.property("voided", new BooleanProperty())
				.property("queueRoom", new BooleanProperty());
	}
}
