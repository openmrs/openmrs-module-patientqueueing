package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.fragment.controller.visit.QuickVisitFragmentController;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class AddPatientToQueueFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public AddPatientToQueueFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel, @SpringBean("patientService") PatientService patientService,
	        @SpringBean("locationService") LocationService locationService,
	        @RequestParam(value = "patientId", required = false) Patient patient, UiSessionContext uiSessionContext) {
		if (patient != null) {
			pageModel.put("birthDate", patient.getBirthdate());
			pageModel.put("patient", patient);
			pageModel.put("patientId", patient.getPatientId());
		}
		pageModel.put("locationList", ((Location) locationService.getRootLocations(false).get(0)).getChildLocations());
		pageModel.put("providerList", Context.getProviderService().getAllProviders(false));
	}
	
	public String post(@SpringBean("patientService") PatientService patientService,
	        @RequestParam(value = "patientId") Patient patient,
	        @RequestParam(value = "providerId", required = false) Provider provider, UiUtils ui,
	        @RequestParam("locationId") Location location,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl, UiSessionContext uiSessionContext,
	        UiUtils uiUtils, HttpServletRequest request) {
		PatientQueue patientQueue = new PatientQueue();
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		patientQueue.setLocationFrom(uiSessionContext.getSessionLocation());
		patientQueue.setPatient(patient);
		patientQueue.setLocationTo(location);
		patientQueue.setProvider(provider);
		patientQueue.setStatus("pending");
		patientQueue.setCreator(uiSessionContext.getCurrentUser());
		patientQueue.setDateCreated(new Date());
		patientQueueingService.savePatientQue(patientQueue);
		
		if (Context.getVisitService().getActiveVisitsByPatient(patient).size() <= 0) {
			QuickVisitFragmentController quickVisitFragmentController = new QuickVisitFragmentController();
			quickVisitFragmentController.create((AdtService) Context.getService(AdtService.class),
			    Context.getVisitService(), patient, location, uiUtils, getFacilityVisitType(), uiSessionContext, request);
		}
		
		try {
			return "redirect:"
			        + ui.pageLink("coreapps", "clinicianfacing/patient",
			            SimpleObject.create(new Object[] { "patientId", patient.getId(), "returnUrl", returnUrl }));
		}
		catch (APIException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	private VisitType getFacilityVisitType() {
		String visitTypeUUID = Context.getAdministrationService().getGlobalProperty(
		    "patientqueueing.defaultFacilityVisitTypeUUID");
		return Context.getVisitService().getVisitTypeByUuid(visitTypeUUID);
	}
}
