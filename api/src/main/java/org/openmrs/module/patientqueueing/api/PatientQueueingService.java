/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.api;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface PatientQueueingService extends OpenmrsService {
	
	@Authorized
	@Transactional(readOnly = true)
	public abstract PatientQueue getPatientQueueById(String queueId) throws APIException;
	
	public List<PatientQueue> getPatientQueueByPatient(Patient patient) throws APIException;
	
	@Transactional
	public abstract List<PatientQueue> getPatientInQueueList(Provider provider, Date fromDate, Date toDate,
	        Location sessionLocation) throws APIException;
	
	@Transactional
	public abstract List<PatientQueue> getPatientInQueueList(Date fromDate, Date toDate, Location sessionLocation)
	        throws APIException;
	
	@Transactional
	public abstract PatientQueue savePatientQue(PatientQueue patientQueue) throws APIException;
	
	@Transactional
	public abstract PatientQueue completeQueue(PatientQueue patientQueue) throws APIException;
}
