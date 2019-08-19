/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.api.impl;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.Date;
import java.util.List;

public class PatientQueueingServiceImpl extends BaseOpenmrsService implements PatientQueueingService {
	
	PatientQueueingDao dao;
	
	public void setDao(PatientQueueingDao dao) {
		this.dao = dao;
	}
	
	public PatientQueue getPatientQueueById(String queueId) throws APIException {
		return dao.getPatientQueueById(queueId);
	}
	
	public List<PatientQueue> getPatientQueueByPatient(Patient patient) throws APIException {
		return dao.getPatientQueueByPatient(patient);
	}
	
	public List<PatientQueue> getPatientInQueueList(Provider provider, Date fromDate, Date toDate, Location sessionLocation)
	        throws APIException {
		return dao.getPatientInQueue(provider, fromDate, toDate, sessionLocation);
	}
	
	@Override
	public List<PatientQueue> getPatientInQueueList(Date fromDate, Date toDate, Location sessionLocation)
	        throws APIException {
		return dao.getPatientInQueue(null, fromDate, toDate, sessionLocation);
	}
	
	public PatientQueue savePatientQue(PatientQueue patientQueue) throws APIException {
		return dao.savePatientQueue(patientQueue);
	}
	
	@Override
	public PatientQueue completeQueue(PatientQueue patientQueue) throws APIException {
		return null;
	}
	
}
