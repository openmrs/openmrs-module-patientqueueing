package org.openmrs.module.patientqueueing.fragment.controller;

import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PatientQueueFragmentController {
	
	public PatientQueueFragmentController() {
	}
	
	public void controller() {
	}
	
	public SimpleObject getPatientInQueue(@RequestParam("providerId") Provider provider,
	        @RequestParam("fromDate") Date fromDate, @RequestParam("toDate") Date toDate, UiUtils ui,
	        UiSessionContext uiSessionContext) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		String fromDateString = formatterExt.format(new Date()) + " 00:00:00";
		
		String toDateString = formatterExt.format(new Date()) + " 23:59:59";
		
		List<PatientQueue> patientQueues = null;
		try {
			patientQueues = ((PatientQueueingService) Context.getService(PatientQueueingService.class))
			        .getPatientInQueueList(provider, formatter.parse(fromDateString), formatter.parse(toDateString),
			            uiSessionContext.getSessionLocation());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		
		List<SimpleObject> patientQueueObject = SimpleObject.fromCollection(patientQueues, ui, new String[] { "patient",
		        "provider", "locationFrom", "locationTo", "status", "status" });
		
		return SimpleObject.create(new Object[] { "data", patientQueueObject });
	}
}
