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
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface PatientQueueingService extends OpenmrsService {
	
	/**
	 * Generates a visit number based on location and date. The number generated is unique for a
	 * patient on a given day. A patient will only have one patient visit number for a given day.
	 * The same number will be reassigned to another queue, in-case its the same patient on the same
	 * day.
	 * 
	 * @param location Location where the generation of the queue is initiated
	 * @param patient the patient who the visit number is for in a given queue
	 * @return will return a string with format LOC-dd/MM/yyy-000-1
	 * @throws ParseException
	 * @throws IOException
	 */
	public String generateVisitNumber(Location location, Patient patient);
	
	/**
	 * Get a single patient queue record by queueId. The queueId can not be null
	 * 
	 * @param queueId Id of the patient queue to be retrieved
	 * @return The patient queue that matches the queueId
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public PatientQueue getPatientQueueById(Integer queueId);
	
	/**
	 * Update or Save patientQueue. Requires a patientQueue
	 * 
	 * @param patientQueue The PatientQueue to be saved
	 * @return PatientQueue that has been saved
	 * @throws APIException
	 */
	@Transactional
	public PatientQueue savePatientQue(PatientQueue patientQueue);
	
	/**
	 * Gets a list of patient queues basing on given parameters.
	 * 
	 * @param provider The provider where the patient was being sent. It Can be null
	 * @param fromDate lowest date a query will be built upon. It can be null
	 * @param toDate highest date a query will be built upon. It Can be null
	 * @param locationTo Location Where patient was sent to
	 * @param locationFrom Location Where patient was sent from
	 * @param patient The patient who is in the queue
	 * @param status Status such as COMPLETED,PENDING
	 * @return List<PatientQueue> A list of patientQueue that meet the parameters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status);
	
	/**
	 * Mark passed patientQueue completed.
	 * 
	 * @param patientQueue The PatientQueue to be completed
	 * @return PatientQueue. The Queue that is completed
	 * @throws APIException
	 */
	@Transactional
	public PatientQueue completePatientQueue(PatientQueue patientQueue);
	
	/**
	 * Gets the patientQueue for a patient at a given location which is not complete.
	 * 
	 * @param locationTo The Location where the patient was is queued to
	 * @param patient The patient who is in the queue
	 * @return a patient queue that meets the criteria of parameters
	 */
	@Transactional(readOnly = true)
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo);
	
	/**
	 * Gets the most recent patientQueue of a patient
	 * 
	 * @param patient the patient whose most recent queue will be returned
	 * @return The most recent patient queue
	 */
	@Transactional(readOnly = true)
	public PatientQueue getMostRecentQueue(Patient patient);
	
	/**
	 * Assigns a visit number to a patient queue
	 * 
	 * @param patientQueue the patient queue to be assigned a visit number
	 * @return patient queue that has been assigned a visit number
	 */
	@Transactional(readOnly = true)
	public PatientQueue assignVisitNumberForToday(PatientQueue patientQueue);
}
