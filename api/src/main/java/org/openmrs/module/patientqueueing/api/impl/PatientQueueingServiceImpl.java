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
import org.openmrs.api.context.Context;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.model.QueueRoom;
import org.openmrs.util.OpenmrsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PatientQueueingServiceImpl extends BaseOpenmrsService implements PatientQueueingService {
	
	PatientQueueingDao dao;
	
	private static final int VISIT_NUMBER_INTEGER_START_POSITION = 15;
	
	private static final int INTEGER_IN_VISIT_NUMBER_LENGTH = 3;
	
	public void setDao(PatientQueueingDao dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#savePatientQue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	public PatientQueue savePatientQue(PatientQueue patientQueue) {
		PatientQueue currentQueue = dao.getIncompletePatientQueue(patientQueue.getPatient(), patientQueue.getLocationTo(),
		    null);
		
		if (currentQueue != null && !patientQueue.equals(currentQueue)) {
			completePatientQueue(currentQueue);
		}
		
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueById(java.lang.Integer)
	 */
	public PatientQueue getPatientQueueById(Integer queueId) {
		return dao.getPatientQueueById(queueId);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueList(org.openmrs.Provider,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.Patient, org.openmrs.module.patientqueueing.model.PatientQueue.Status)
	 */
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status) {
		return dao.getPatientQueueList(provider, fromDate, toDate, locationTo, locationFrom, patient, status, null);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#completePatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	@Override
	public PatientQueue completePatientQueue(PatientQueue patientQueue) {
		patientQueue.setStatus(PatientQueue.Status.COMPLETED);
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getIncompletePatientQueue(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 */
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo) {
		
		return dao.getIncompletePatientQueue(patient, locationTo, null);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getMostRecentQueue(org.openmrs.Patient)
	 */
	@Override
	public PatientQueue getMostRecentQueue(Patient patient) {
		return dao.getMostRecentQueue(patient);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#assignVisitNumberForToday(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	@Override
	public PatientQueue assignVisitNumberForToday(PatientQueue patientQueue) {
		Date today = new Date();
		List<PatientQueue> patientQueueList = getPatientQueueList(null, OpenmrsUtil.firstSecondOfDay(today),
		    OpenmrsUtil.getLastMomentOfDay(today), null, null, patientQueue.getPatient(), null);
		
		if (!patientQueueList.isEmpty() && patientQueueList.get(0).getVisitNumber() != null) {
			patientQueue.setVisitNumber(patientQueueList.get(0).getVisitNumber());
		} else {
			patientQueue.setVisitNumber(generateVisitNumber(patientQueue.getLocationFrom(), patientQueue.getPatient()));
		}
		return patientQueue;
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#generateVisitNumber(org.openmrs.Location,
	 *      org.openmrs.Patient)
	 */
	public String generateVisitNumber(Location location, Patient patient) {
		
		Date today = new Date();
		
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		List<PatientQueue> patientQueues = getPatientQueueList(null, OpenmrsUtil.firstSecondOfDay(today),
		    OpenmrsUtil.getLastMomentOfDay(today), null, location, null, null);
		
		int nextNumberInQueue = 1;
		if (!patientQueues.isEmpty()) {
			
			int visitNumberLength = patientQueues.get(0).getVisitNumber().length();
			
			if (visitNumberLength == VISIT_NUMBER_INTEGER_START_POSITION + INTEGER_IN_VISIT_NUMBER_LENGTH) {
				nextNumberInQueue = Integer.parseInt(patientQueues.get(0).getVisitNumber()
				        .subSequence(VISIT_NUMBER_INTEGER_START_POSITION, visitNumberLength).toString());
				nextNumberInQueue += 1;
			}
		}
		
		String dateString = formatterExt.format(today);
		
		String locationName = location.getName();
		
		if (locationName.length() > 3) {
			locationName = locationName.substring(0, 3);
		}
		
		String zeroesToAppend = "";
		if (nextNumberInQueue <= 9) {
			zeroesToAppend = "00";
		} else if (nextNumberInQueue < 100) {
			zeroesToAppend = "0";
		}
		
		return dateString + "-" + locationName + "-" + zeroesToAppend + nextNumberInQueue;
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueListBySearchParams(java.lang.String,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status)
	 */
	@Override
	public List<PatientQueue> getPatientQueueListBySearchParams(String searchString, Date fromDate, Date toDate,
	        Location locationTo, Location locationFrom, PatientQueue.Status status) {
		
		List<Patient> patientList = new ArrayList<Patient>();
		
		if (searchString != null && !searchString.equals("")) {
			PatientService patientService = Context.getPatientService();
			List list = Arrays.asList(searchString.split(","));
			for (Object o : list) {
				List<Patient> patients = patientService.getPatients(o.toString());
				patientList.addAll(patients);
			}
		}
		
		return dao.getPatientQueueList(patientList, fromDate, toDate, locationTo, locationFrom, status, null);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#saveQueueRoom(org.openmrs.module.patientqueueing.model.QueueRoom)
	 */
	@Override
	public QueueRoom saveQueueRoom(QueueRoom queueRoom) {
		return dao.saveQueueRoom(queueRoom);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getQueueRoomByUUID(java.lang.String)
	 */
	@Override
	public QueueRoom getQueueRoomById(Integer queueRoomId) {
		return dao.getQueueRoomById(queueRoomId);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getQueueRoomByUUID(java.lang.String)
	 */
	@Override
	public QueueRoom getQueueRoomByUUID(String queueRoomUUID) {
		return dao.getQueueRoomByUUID(queueRoomUUID);
	}
	
	/**
	 * @see PatientQueueingService#getAllQueueRoom()
	 */
	@Override
	public List<QueueRoom> getAllQueueRoom() {
		return dao.getAllQueueRoom();
	}


	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueList(org.openmrs.Provider,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.Patient,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status,org.openmrs.module.patientqueueing.model.QueueRoom)
	 */
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
												  Location locationFrom, Patient patient, PatientQueue.Status status, QueueRoom queueRoom) {
		return dao.getPatientQueueList(provider, fromDate, toDate, locationTo, locationFrom, patient, status, queueRoom);
	}

	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getIncompletePatientQueue(org.openmrs.Patient,
	 *      org.openmrs.Location,org.openmrs.module.patientqueueing.model.QueueRoom )
	 */
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo, QueueRoom queueRoom) {

		return dao.getIncompletePatientQueue(patient, locationTo, queueRoom);
	}

	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueListBySearchParams(java.lang.String,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status,org.openmrs.module.patientqueueing.model.QueueRoom)
	 */
	@Override
	public List<PatientQueue> getPatientQueueListBySearchParams(String searchString, Date fromDate, Date toDate,
																Location locationTo, Location locationFrom, PatientQueue.Status status, QueueRoom queueRoom) {

		List<Patient> patientList = new ArrayList<Patient>();

		if (searchString != null && !searchString.equals("")) {
			PatientService patientService = Context.getPatientService();
			List list = Arrays.asList(searchString.split(","));
			for (Object o : list) {
				List<Patient> patients = patientService.getPatients(o.toString());
				patientList.addAll(patients);
			}
		}

		return dao.getPatientQueueList(patientList, fromDate, toDate, locationTo, locationFrom, status, queueRoom);
	}
}
