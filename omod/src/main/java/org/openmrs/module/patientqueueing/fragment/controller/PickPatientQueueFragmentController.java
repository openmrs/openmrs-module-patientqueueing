/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.session.Session;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

public class PickPatientQueueFragmentController {
	
	public void controller(@RequestParam("patientId") Patient patient, FragmentModel model, UiSessionContext sessionContext)
	        throws ParseException {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		PatientQueue patientQueue = patientQueueingService.getIncompletePatientQueue(patient,
		    sessionContext.getSessionLocation());
		
		Integer currentPatientQueueId = null;
		if (patientQueue != null) {
			currentPatientQueueId = patientQueue.getPatientQueueId();
		}
		if (currentPatientQueueId != null) {
			model.put("patientQueueId", currentPatientQueueId);
		} else {
			model.put("patientQueueId", "");
		}
		
		model.put("currentLocationUUID", sessionContext.getSessionLocation().getUuid());
		model.put("currentProvider", sessionContext.getCurrentProvider());
	}
}
