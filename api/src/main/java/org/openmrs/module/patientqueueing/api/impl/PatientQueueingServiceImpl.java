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

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.util.OpenmrsUtil;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.patientqueueing.PatientQueueingConfig.ROOM_TAG_UUID;

public class PatientQueueingServiceImpl extends BaseOpenmrsService implements PatientQueueingService {
	
	PatientQueueingDao dao;
	
	private static final int VISIT_NUMBER_INTEGER_START_POSITION = 15;
	
	private static final int INTEGER_IN_VISIT_NUMBER_LENGTH = 3;
	
	private static final int MAX_TICKET_RETRIES = 10;
	
	public void setDao(PatientQueueingDao dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#savePatientQue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	public PatientQueue savePatientQue(PatientQueue patientQueue) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getPatient() == null) {
			throw new IllegalArgumentException("Patient is required to save a queue entry");
		}
		
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
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		// Idempotent: re-completing must not overwrite the original completion time
		if (patientQueue.getStatus() == PatientQueue.Status.COMPLETED) {
			return patientQueue;
		}
		
		patientQueue.setStatus(PatientQueue.Status.COMPLETED);
		patientQueue.setDateCompleted(new Date());
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
		
		String dateString = formatterExt.format(today);
		String locationName = (location != null && location.getName() != null) ? location.getName() : "LOC";
		if (locationName.length() > 3) {
			locationName = locationName.substring(0, 3);
		}
		
		Date fromDate = OpenmrsUtil.firstSecondOfDay(today);
		Date toDate = OpenmrsUtil.getLastMomentOfDay(today);
		
		// NOT cluster-safe: Find an unused ticket number with retry logic
		// This is check-then-insert with no DB uniqueness backing it.
		// Two concurrent check-ins can read the same base number and mint duplicate tickets.
		// The retry loop only defends against sequential collisions.
		Set<Integer> uniquePatientIds = getUniquePatientIdsForToday(fromDate, toDate);
		int baseNumber = uniquePatientIds.size() + 1;
		
		for (int attempt = 0; attempt < MAX_TICKET_RETRIES; attempt++) {
			int ticketNumber = baseNumber + attempt;
			String candidateVisitNumber = dateString + "-" + locationName + "-" + padTicketNumber(ticketNumber);
			
			// Check if this ticket number already exists today
			List<PatientQueue> existingQueues = dao.getPatientQueueByVisitNumber(candidateVisitNumber, fromDate, toDate);
			if (existingQueues.isEmpty()) {
				return candidateVisitNumber;
			}
		}
		
		throw new APIException("Failed to generate unique ticket number after " + MAX_TICKET_RETRIES + " attempts");
	}
	
	private String padTicketNumber(int number) {
		if (number <= 9) {
			return "00" + number;
		} else if (number < 100) {
			return "0" + number;
		}
		return String.valueOf(number);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientByPersonAttributeValue(java.lang.Integer,
	 *      java.lang.String)
	 */
	@Override
	public Patient getPatientByPersonAttributeValue(Integer personAttributeTypeId, String attributeValue) {
		return dao.getPatientByPersonAttributeValue(personAttributeTypeId, attributeValue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#countPatientsByPersonAttributeValue(java.lang.Integer,
	 *      java.lang.String)
	 */
	@Override
	public int countPatientsByPersonAttributeValue(Integer personAttributeTypeId, String attributeValue) {
		return dao.countPatientsByPersonAttributeValue(personAttributeTypeId, attributeValue);
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
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueList(org.openmrs.Provider,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.Patient,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status,org.openmrs.Location)
	 */
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom) {
		return dao.getPatientQueueList(provider, fromDate, toDate, locationTo, locationFrom, patient, status, queueRoom);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getIncompletePatientQueue(org.openmrs.Patient,
	 *      org.openmrs.Location,org.openmrs.Location )
	 */
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo, Location queueRoom) {
		
		return dao.getIncompletePatientQueue(patient, locationTo, queueRoom);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueListBySearchParams(java.lang.String,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status,org.openmrs.Location)
	 */
	@Override
	public List<PatientQueue> getPatientQueueListBySearchParams(String searchString, Date fromDate, Date toDate,
	        Location locationTo, Location locationFrom, PatientQueue.Status status, Location queueRoom) {
		
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
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueByUuid(java.lang.String)
	 */
	@Override
	public PatientQueue getPatientQueueByUuid(String uuid) {
		return dao.getPatientQueueByUUID(uuid);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#pickPatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue,org.openmrs.Provider,org.openmrs.Location)
	 */
	@Override
	public PatientQueue pickPatientQueue(PatientQueue patientQueue, Provider provider, Location queueRoom) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getStatus() != PatientQueue.Status.PENDING) {
			throw new APIException("Can only call queue entries with status PENDING; use recall for ON_HOLD or NO_SHOW");
		}
		
		patientQueue.setStatus(PatientQueue.Status.PICKED);
		patientQueue.setDatePicked(new Date());
		patientQueue.setProvider(provider);
		patientQueue.setQueueRoom(queueRoom);
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#holdPatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	@Override
	public PatientQueue holdPatientQueue(PatientQueue patientQueue) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getStatus() != PatientQueue.Status.PENDING
		        && patientQueue.getStatus() != PatientQueue.Status.PICKED) {
			throw new APIException("Can only hold queue entries with status PENDING or PICKED");
		}
		
		patientQueue.setStatus(PatientQueue.Status.ON_HOLD);
		patientQueue.setDateChanged(new Date());
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#recallPatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue,
	 *      org.openmrs.Provider)
	 */
	@Override
	public PatientQueue recallPatientQueue(PatientQueue patientQueue, Provider provider) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getStatus() != PatientQueue.Status.ON_HOLD
		        && patientQueue.getStatus() != PatientQueue.Status.NO_SHOW) {
			throw new APIException("Can only recall queue entries with status ON_HOLD or NO_SHOW");
		}
		
		patientQueue.setStatus(PatientQueue.Status.PICKED);
		patientQueue.setDatePicked(new Date());
		if (provider != null) {
			patientQueue.setProvider(provider);
		}
		patientQueue.setDateChanged(new Date());
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#markPatientQueueNoShow(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	@Override
	public PatientQueue markPatientQueueNoShow(PatientQueue patientQueue) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getStatus() != PatientQueue.Status.PICKED) {
			throw new APIException("Can only mark as no-show queue entries with status PICKED");
		}
		
		patientQueue.setStatus(PatientQueue.Status.NO_SHOW);
		patientQueue.setDateChanged(new Date());
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#reopenPatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	@Override
	public PatientQueue reopenPatientQueue(PatientQueue patientQueue) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (patientQueue.getStatus() != PatientQueue.Status.PICKED
		        && patientQueue.getStatus() != PatientQueue.Status.ON_HOLD
		        && patientQueue.getStatus() != PatientQueue.Status.NO_SHOW) {
			throw new APIException("Can only reopen queue entries with status PICKED, ON_HOLD or NO_SHOW");
		}
		
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setDatePicked(null);
		patientQueue.setDateCompleted(null);
		patientQueue.setDateChanged(new Date());
		return dao.savePatientQueue(patientQueue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#transferPatientQueue(org.openmrs.module.patientqueueing.model.PatientQueue,
	 *      org.openmrs.Location, org.openmrs.Location)
	 */
	@Override
	public PatientQueue transferPatientQueue(PatientQueue patientQueue, Location locationTo, Location queueRoom) {
		if (patientQueue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (locationTo == null) {
			throw new IllegalArgumentException("locationTo is required to transfer a queue entry");
		}
		
		PatientQueue.Status status = patientQueue.getStatus();
		if (status != PatientQueue.Status.PENDING && status != PatientQueue.Status.PICKED
		        && status != PatientQueue.Status.ON_HOLD && status != PatientQueue.Status.NO_SHOW) {
			throw new APIException("Can only transfer queue entries with status PENDING, PICKED, ON_HOLD or NO_SHOW");
		}
		
		Location previousLocation = patientQueue.getLocationTo() != null ? patientQueue.getLocationTo() : patientQueue
		        .getQueueRoom();
		
		patientQueue.setStatus(PatientQueue.Status.TRANSFERRED);
		patientQueue.setDateChanged(new Date());
		dao.savePatientQueue(patientQueue);
		
		PatientQueue next = new PatientQueue();
		next.setPatient(patientQueue.getPatient());
		next.setProvider(patientQueue.getProvider());
		next.setLocationFrom(previousLocation);
		next.setLocationTo(locationTo);
		next.setEncounter(patientQueue.getEncounter());
		next.setStatus(PatientQueue.Status.PENDING);
		next.setVisitNumber(patientQueue.getVisitNumber());
		next.setPriority(patientQueue.getPriority());
		next.setPriorityComment(patientQueue.getPriorityComment());
		next.setComment(patientQueue.getComment());
		next.setQueueRoom(queueRoom);
		// Save through savePatientQue so it completes any entry the patient still has open
		// at the destination (e.g. transferred back to a location queued at earlier today);
		// saving directly would leave two incomplete rows and 500 the next CALLED
		return savePatientQue(next);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#
	 *      getPatientQueueByParentLocation(org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status, java.util.Date dateFrom,
	 *      java.util.Date)
	 */
	@Override
    public List<PatientQueue> getPatientQueueByParentLocation(Location parentLocation, PatientQueue.Status status, Date fromDate, Date toDate, boolean onlyInQueueRooms) {
        LocationTag queueRomTag = Context.getLocationService().getLocationTagByUuid(ROOM_TAG_UUID);
        List<Location> childLocations = new ArrayList<>();
        flattenLocationHierarchy(parentLocation, childLocations, queueRomTag, onlyInQueueRooms);

        if (childLocations.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.getPatientsInQueueRoom(childLocations, status, fromDate, toDate);
    }
	
	/**
	 * Supportive class that helps to loop through locations recursively to ensure that all child
	 * locations are collected
	 * 
	 * @param parentLocation the parent location to check for children
	 * @param childLocations the childLocation List to be updated
	 * @param locationTag the tag to check if location has it
	 * @param onlyInQueueRooms condition to determine if to only include locations with locationTag
	 */
	private void flattenLocationHierarchy(Location parentLocation, List<Location> childLocations, LocationTag locationTag,
	        boolean onlyInQueueRooms) {
		flattenLocationHierarchy(parentLocation, childLocations, locationTag, onlyInQueueRooms, new HashSet<Location>());
	}
	
	private void flattenLocationHierarchy(Location parentLocation, List<Location> childLocations, LocationTag locationTag,
	        boolean onlyInQueueRooms, Set<Location> visited) {
		// Guard against null entries and cyclic parent/child data, which would recurse forever
		if (parentLocation == null || !visited.add(parentLocation)) {
			return;
		}
		
		if (onlyInQueueRooms) {
			if (parentLocation.getTags() != null && parentLocation.getTags().contains(locationTag)) {
				childLocations.add(parentLocation);
			}
		} else {
			childLocations.add(parentLocation);
		}
		
		for (Location childLocation : parentLocation.getChildLocations(false)) {
			flattenLocationHierarchy(childLocation, childLocations, locationTag, onlyInQueueRooms, visited);
		}
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueByVisitNumber(java.lang.String,
	 *      java.util.Date, java.util.Date)
	 */
	public List<PatientQueue> getPatientQueueByVisitNumber(String visitNumber, Date fromDate, Date toDate) {
		return dao.getPatientQueueByVisitNumber(visitNumber, fromDate, toDate);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueListFifo(org.openmrs.Provider,
	 *      java.util.Date,
	 *      java.util.Date,org.openmrs.Location,org.openmrs.Location,org.openmrs.Patient,org.openmrs.module.patientqueueing.model.PatientQueue.Status,org.openmrs.Location)
	 */
	public List<PatientQueue> getPatientQueueListFifo(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom) {
		return dao.getPatientQueueListFifo(provider, fromDate, toDate, locationTo, locationFrom, patient, status, queueRoom);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueByParentLocationFifo(org.openmrs.Location,org.openmrs.module.patientqueueing.model.PatientQueue.Status,java.util.Date,
	 *      java.util.Date,boolean)
	 */
	public List<PatientQueue> getPatientQueueByParentLocationFifo(Location parentLocation, PatientQueue.Status status,
																  Date fromDate, Date toDate, boolean onlyInQueueRooms) {
		LocationTag queueRomTag = Context.getLocationService().getLocationTagByUuid(ROOM_TAG_UUID);
		List<Location> childLocations = new ArrayList<>();
		flattenLocationHierarchy(parentLocation, childLocations, queueRomTag, onlyInQueueRooms);

		if (childLocations.isEmpty()) {
			return Collections.emptyList();
		}
		return dao.getPatientsInQueueRoomFifo(childLocations, status, fromDate, toDate);
	}
	
	// ========== Non-Patient Queue Service Methods ==========
	
	@Override
	public NonPatientQueue getNonPatientQueueById(Integer queueId) {
		return dao.getNonPatientQueueById(queueId);
	}
	
	@Override
	public NonPatientQueue getNonPatientQueueByUuid(String uuid) {
		return dao.getNonPatientQueueByUuid(uuid);
	}
	
	@Override
	public NonPatientQueue createNonPatientQueueEntry(String displayName, String phoneNumber, Concept queueType,
	        Location currentLocation, Location locationTo, Location queueRoom, Integer priority, String comment) {
		String ticketNumber = generateNonPatientQueueTicketNumber(currentLocation, queueType);
		
		NonPatientQueue queue = new NonPatientQueue.Builder().displayName(displayName).phoneNumber(phoneNumber)
		        .queueType(queueType).currentLocation(currentLocation).locationTo(locationTo).queueRoom(queueRoom)
		        .priority(priority).comment(comment).ticketNumber(ticketNumber).build();
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue saveNonPatientQueue(NonPatientQueue nonPatientQueue) {
		return dao.saveNonPatientQueue(nonPatientQueue);
	}
	
	@Override
	public List<NonPatientQueue> getNonPatientQueueByTicketNumber(String ticketNumber, Date fromDate, Date toDate) {
		return dao.getNonPatientQueueByTicketNumber(ticketNumber, fromDate, toDate);
	}
	
	@Override
	public List<NonPatientQueue> getNonPatientQueuesByQueueRoom(Location queueRoom, Date fromDate, Date toDate) {
		return dao.getNonPatientQueuesByQueueRoom(queueRoom, fromDate, toDate);
	}
	
	@Override
	public List<NonPatientQueue> getNonPatientQueuesByQueueRoomAndStatus(Location queueRoom,
	        NonPatientQueue.NonPatientQueueStatus status) {
		return dao.getNonPatientQueuesByQueueRoomAndStatus(queueRoom, status);
	}
	
	@Override
	public List<NonPatientQueue> getAllActiveNonPatientQueues() {
		return dao.getAllActiveNonPatientQueues();
	}
	
	@Override
	public List<NonPatientQueue> getNonPatientQueues(NonPatientQueue.NonPatientQueueStatus status, Concept queueType,
	        Location locationTo, Location queueRoom, Date fromDate, Date toDate) {
		LocationTag queueRoomTag = Context.getLocationService().getLocationTagByUuid(ROOM_TAG_UUID);

		// Flatten hierarchy for locationTo if provided
		List<Location> locationToList = new ArrayList<>();
		if (locationTo != null) {
			flattenLocationHierarchy(locationTo, locationToList, queueRoomTag, false);
		}

		// Flatten hierarchy for queueRoom if provided
		List<Location> queueRoomList = new ArrayList<>();
		if (queueRoom != null) {
			flattenLocationHierarchy(queueRoom, queueRoomList, queueRoomTag, true);
			// If queueRoom was provided but has no matching tagged locations, return empty result
			if (queueRoomList.isEmpty()) {
				return Collections.emptyList();
			}
		}

		return dao.getNonPatientQueues(status, queueType, locationToList, queueRoomList, fromDate, toDate);
	}
	
	@Override
	public NonPatientQueue callNonPatientQueue(NonPatientQueue queue, Provider provider) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.WAITING) {
			throw new APIException("Can only call queue entries with status WAITING");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.CALLED);
		queue.setCalledBy(provider);
		queue.setCalledAt(new Date());
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue markNonPatientQueueArrived(NonPatientQueue queue) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.CALLED) {
			throw new APIException("Can only mark as arrived queue entries with status CALLED");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.ARRIVED);
		queue.setArrivedAt(new Date());
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue startServingNonPatientQueue(NonPatientQueue queue, Provider provider) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.ARRIVED) {
			throw new APIException("Can only start serving queue entries with status ARRIVED");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.SERVING);
		queue.setServedBy(provider);
		queue.setStartedAt(new Date());
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue completeNonPatientQueue(NonPatientQueue queue, Provider provider) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.SERVING) {
			throw new APIException("Can only complete queue entries with status SERVING");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.COMPLETED);
		if (queue.getServedBy() == null) {
			queue.setServedBy(provider);
		}
		queue.setEndedAt(new Date());
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue skipNonPatientQueue(NonPatientQueue queue) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.CALLED) {
			throw new APIException("Can only skip queue entries with status CALLED");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.WAITING);
		queue.setCalledAt(null);
		queue.setCalledBy(null);
		
		return saveNonPatientQueue(queue);
	}
	
	@Override
	public NonPatientQueue cancelNonPatientQueue(NonPatientQueue queue) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() == NonPatientQueue.NonPatientQueueStatus.COMPLETED
		        || queue.getStatus() == NonPatientQueue.NonPatientQueueStatus.CANCELLED
		        || queue.getStatus() == NonPatientQueue.NonPatientQueueStatus.TRANSFERRED) {
			throw new APIException("Cannot cancel queue entries that are completed, cancelled or transferred");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.CANCELLED);
		queue.setEndedAt(new Date());
		
		return saveNonPatientQueue(queue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#holdNonPatientQueue(org.openmrs.module.patientqueueing.model.NonPatientQueue)
	 */
	@Override
	public NonPatientQueue holdNonPatientQueue(NonPatientQueue queue) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.WAITING
		        && queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.CALLED
		        && queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.ARRIVED) {
			throw new APIException("Can only hold queue entries with status WAITING, CALLED or ARRIVED");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.HOLD);
		return saveNonPatientQueue(queue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#recallNonPatientQueue(org.openmrs.module.patientqueueing.model.NonPatientQueue,
	 *      org.openmrs.Provider)
	 */
	@Override
	public NonPatientQueue recallNonPatientQueue(NonPatientQueue queue, Provider provider) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.HOLD
		        && queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.NO_SHOW) {
			throw new APIException("Can only recall queue entries with status HOLD or NO_SHOW");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.CALLED);
		queue.setCalledBy(provider);
		queue.setCalledAt(new Date());
		return saveNonPatientQueue(queue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#markNonPatientQueueNoShow(org.openmrs.module.patientqueueing.model.NonPatientQueue)
	 */
	@Override
	public NonPatientQueue markNonPatientQueueNoShow(NonPatientQueue queue) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.CALLED
		        && queue.getStatus() != NonPatientQueue.NonPatientQueueStatus.ARRIVED) {
			throw new APIException("Can only mark as no-show queue entries with status CALLED or ARRIVED");
		}
		
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.NO_SHOW);
		return saveNonPatientQueue(queue);
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#transferNonPatientQueue(org.openmrs.module.patientqueueing.model.NonPatientQueue,
	 *      org.openmrs.Location, org.openmrs.Location)
	 */
	@Override
	public NonPatientQueue transferNonPatientQueue(NonPatientQueue queue, Location locationTo, Location queueRoom) {
		if (queue == null) {
			throw new IllegalArgumentException("Queue cannot be null");
		}
		if (locationTo == null) {
			throw new IllegalArgumentException("locationTo is required to transfer a queue entry");
		}
		
		NonPatientQueue.NonPatientQueueStatus status = queue.getStatus();
		if (status == NonPatientQueue.NonPatientQueueStatus.COMPLETED
		        || status == NonPatientQueue.NonPatientQueueStatus.CANCELLED
		        || status == NonPatientQueue.NonPatientQueueStatus.TRANSFERRED) {
			throw new APIException("Cannot transfer queue entries that are completed, cancelled or already transferred");
		}
		
		Location previousLocation = queue.getLocationTo() != null ? queue.getLocationTo() : queue.getQueueRoom();
		
		// Keep the source location on the TRANSFERRED row (mirrors transferPatientQueue) so
		// the destination doesn't list the ticket twice and the source keeps its history
		queue.setStatus(NonPatientQueue.NonPatientQueueStatus.TRANSFERRED);
		saveNonPatientQueue(queue);
		
		NonPatientQueue next = new NonPatientQueue();
		next.setTicketNumber(queue.getTicketNumber());
		next.setDisplayName(queue.getDisplayName());
		next.setPhoneNumber(queue.getPhoneNumber());
		next.setQueueType(queue.getQueueType());
		next.setStatus(NonPatientQueue.NonPatientQueueStatus.WAITING);
		next.setCurrentLocation(previousLocation);
		next.setLocationTo(locationTo);
		next.setQueueRoom(queueRoom);
		next.setPriority(queue.getPriority());
		next.setComment(queue.getComment());
		return saveNonPatientQueue(next);
	}
	
	@Override
	public String generateNonPatientQueueTicketNumber(Location location, Concept queueType) {
		Date today = new Date();
		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");
		
		String dateString = formatterExt.format(today);
		String locationName = (location != null && location.getName() != null) ? location.getName() : "LOC";
		if (locationName.length() > 3) {
			locationName = locationName.substring(0, 3);
		}
		
		String queueTypeCode = "XXX";
		if (queueType != null && queueType.getUuid() != null) {
			String uuid = queueType.getUuid();
			queueTypeCode = uuid.length() >= 3 ? uuid.substring(0, 3).toUpperCase() : uuid.toUpperCase();
		}
		
		Date fromDate = OpenmrsUtil.firstSecondOfDay(today);
		Date toDate = OpenmrsUtil.getLastMomentOfDay(today);
		
		// NOT cluster-safe: Find an unused ticket number with retry logic
		// This is check-then-insert with no DB uniqueness backing it.
		// Two concurrent check-ins can read the same base number and mint duplicate tickets.
		// The retry loop only defends against sequential collisions.
		Long countToday = countNonPatientQueuesToday(fromDate, toDate);
		int baseNumber = countToday.intValue() + 1;
		
		for (int attempt = 0; attempt < MAX_TICKET_RETRIES; attempt++) {
			int ticketNumber = baseNumber + attempt;
			String candidateTicketNumber = dateString + "-" + locationName + "-" + queueTypeCode + "-"
			        + padTicketNumber(ticketNumber);
			
			// Check if this ticket number already exists today
			List<NonPatientQueue> existingQueues = dao.getNonPatientQueueByTicketNumber(candidateTicketNumber, fromDate,
			    toDate);
			if (existingQueues.isEmpty()) {
				return candidateTicketNumber;
			}
		}
		
		throw new APIException("Failed to generate unique ticket number after " + MAX_TICKET_RETRIES + " attempts");
	}
	
	@Override
	public List<NonPatientQueue> getNonPatientQueuesByParentLocationFifo(Location parentLocation,
	        NonPatientQueue.NonPatientQueueStatus status, Concept queueType, Date fromDate, Date toDate, boolean onlyInQueueRooms) {
		LocationTag queueRoomTag = Context.getLocationService().getLocationTagByUuid(ROOM_TAG_UUID);
		List<Location> childLocations = new ArrayList<>();
		flattenLocationHierarchy(parentLocation, childLocations, queueRoomTag, onlyInQueueRooms);

		if (childLocations.isEmpty()) {
			return Collections.emptyList();
		}
		return dao.getNonPatientQueuesInLocationsFifo(childLocations, status, queueType, fromDate, toDate);
	}
	
	@Override
	public java.util.Map<String, Integer> countPendingQueuesByLocation(java.util.List<Location> locations, Date fromDate,
	        Date toDate) {
		return dao.countPendingQueuesByLocation(locations, fromDate, toDate);
	}
	
	@Override
	public java.util.Set<Integer> getUniquePatientIdsForToday(Date fromDate, Date toDate) {
		return dao.getUniquePatientIdsForToday(fromDate, toDate);
	}
	
	@Override
	public Long countNonPatientQueuesToday(Date fromDate, Date toDate) {
		return dao.countNonPatientQueuesToday(fromDate, toDate);
	}
}
