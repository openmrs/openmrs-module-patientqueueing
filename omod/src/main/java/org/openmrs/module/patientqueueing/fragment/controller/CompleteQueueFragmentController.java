package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.lang.math.NumberUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * VisitIncludes fragment. Retrieve and set the visit type and attributes
 */
public class CompleteQueueFragmentController {
	
	public void controller(FragmentConfiguration config, FragmentModel model, @InjectBeans PatientDomainWrapper wrapper,
	        @SpringBean("adtService") AdtService adtService, UiSessionContext sessionContext, HttpServletRequest request,
	        @SpringBean("patientService") PatientService patientService) {
		
		Object patient = config.get("patient");
		
		if (patient == null) {
			// retrieve patient id from parameter map
			if (request.getParameter("patientId") != null) {
				String patientId = request.getParameter("patientId");
				if (patientId != null) {
					if (!NumberUtils.isDigits(patientId)) {
						patient = patientService.getPatientByUuid(patientId);
					} else {
						patient = patientService.getPatient(NumberUtils.toInt(patientId));
					}
				}
			}
		}
		
		if (patient instanceof Patient) {
			wrapper.setPatient((Patient) patient);
		} else if (patient instanceof PatientDomainWrapper) {
			wrapper = (PatientDomainWrapper) patient;
		} else {
			throw new IllegalArgumentException("Patient must be of type Patient or PatientDomainWrapper");
		}
		
		model.addAttribute("patient", wrapper);
	}
	
	public void post(FragmentModel model, @SpringBean("patientService") PatientService patientService,
	        @RequestParam("patientId") Patient patient, UiUtils ui,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl, UiSessionContext uiSessionContext,
	        UiUtils uiUtils, HttpServletRequest request) {
		
	}
}
