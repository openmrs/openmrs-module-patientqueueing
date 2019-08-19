package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClinicianQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ClinicianQueueListFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		List<PatientQueue> patientQueueList = new ArrayList();
		try {
			patientQueueList = patientQueueingService.getPatientInQueueList(uiSessionContext.getCurrentProvider(),
			    QueueingUtil.dateFormtter(new Date(), "00:00:00"), QueueingUtil.dateFormtter(new Date(), "23:59:59"),
			    uiSessionContext.getSessionLocation());
		}
		catch (ParseException e) {
			log.error(e);
		}
		pageModel.put("patientQueueList", patientQueueList);
		
	}
	
}
