package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.notification.Alert;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * VisitIncludes fragment. Retrieve and set the visit type and attributes
 */
public class CompleteQueueFragmentController {
	
	public void controller(FragmentModel model, UiSessionContext sessionContext) {
		
	}
	
	public SimpleObject completeQueue(FragmentModel model, @RequestParam("patientQueueId") PatientQueue patientQueue,
	        UiUtils ui) throws IOException {
		PatientQueueingService patientService = Context.getService(PatientQueueingService.class);
		patientService.completeQueue(patientQueue);
		return null;
	}
}
