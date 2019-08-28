package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.utils.QueueingUtil;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClinicianQueueListFragmentController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ClinicianQueueListFragmentController() {
	}
	
	public void controller(@SpringBean FragmentModel pageModel, UiSessionContext uiSessionContext) {
		
	}
	
	public SimpleObject getPatientQueueList(@RequestParam(value = "searchfilter", required = false) String searchfilter,
	        UiSessionContext uiSessionContext) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		ObjectMapper objectMapper = new ObjectMapper();
		
		SimpleObject simpleObject = new SimpleObject();
		
		List<PatientQueue> patientQueueList = new ArrayList();
		if (!searchfilter.equals("")) {
			try {
				patientQueueList = patientQueueingService.searchQueue(searchfilter,
				    QueueingUtil.dateFormtterString(new Date(), "00:00:00"),
				    QueueingUtil.dateFormtterString(new Date(), "23:59:59"), uiSessionContext.getCurrentProvider(),
				    uiSessionContext.getSessionLocation());
			}
			catch (ParseException e) {
				log.error(e);
			}
		} else {
			try {
				patientQueueList = patientQueueingService.getPatientInQueueList(uiSessionContext.getCurrentProvider(),
				    QueueingUtil.dateFormtterDate(new Date(), "00:00:00"),
				    QueueingUtil.dateFormtterDate(new Date(), "23:59:59"), uiSessionContext.getSessionLocation());
			}
			catch (ParseException e) {
				log.error(e);
			}
			
		}
		
		List<PatientQueueMapper> patientQueueMappers = mapPatientQueueToMapper(patientQueueList);
		
		try {
			simpleObject.put("patientQueueList", objectMapper.writeValueAsString(patientQueueMappers));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return simpleObject;
	}
	
	/**
	 * Convert PatientQueue List to PatientQueueMapping
	 * 
	 * @param patientQueueList
	 * @return
	 */
	private List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
		List<PatientQueueMapper> patientQueueMappers = new ArrayList<PatientQueueMapper>();
		
		for (PatientQueue patientQueue : patientQueueList) {
			String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " "
			        + patientQueue.getPatient().getMiddleName();
			PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
			patientQueueMapper.setId(patientQueue.getId());
			patientQueueMapper.setPatientNames(names.replace("null", ""));
			patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
			patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
			patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
			patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
			patientQueueMapper.setStatus(patientQueue.getStatus());
			patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
			patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
			patientQueueMappers.add(patientQueueMapper);
		}
		return patientQueueMappers;
	}
	
}
