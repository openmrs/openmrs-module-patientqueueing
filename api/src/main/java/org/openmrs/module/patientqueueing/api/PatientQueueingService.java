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

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
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
	 * Find a patient by person attribute value using an efficient database query.
	 * 
	 * @param personAttributeTypeId the person attribute type ID
	 * @param attributeValue the attribute value to match
	 * @return the first matching patient, or null if not found
	 */
	@Transactional(readOnly = true)
	Patient getPatientByPersonAttributeValue(Integer personAttributeTypeId, String attributeValue);
	
	/**
	 * Get a single patient queue record by queueId. The queueId can not be null
	 * 
	 * @param queueId Id of the patient queue to be retrieved
	 * @return The patient queue that matches the queueId
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public PatientQueue getPatientQueueById(Integer queueId);
	
	/**
	 * Update or Save patientQueue. Requires a patientQueue
	 * 
	 * @param patientQueue The PatientQueue to be saved
	 * @return PatientQueue that has been saved
	 * @throws APIException
	 */
	@Transactional
	@Authorized("Task: patientqueueing.sendPatientToQueue")
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
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status);
	
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
	 * @param queueRoom The room where a patient is queued to
	 * @return List<PatientQueue> A list of patientQueue that meet the parameters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom);
	
	/**
	 * Mark passed patientQueue completed.
	 * 
	 * @param patientQueue The PatientQueue to be completed
	 * @return PatientQueue. The Queue that is completed
	 * @throws APIException
	 */
	@Transactional
	@Authorized("Task: patientqueueing.completePatientQueue")
	public PatientQueue completePatientQueue(PatientQueue patientQueue);
	
	/**
	 * Gets the patientQueue for a patient at a given location which is not complete.
	 * 
	 * @param locationTo The Location where the patient was is queued to
	 * @param patient The patient who is in the queue
	 * @return a patient queue that meets the criteria of parameters
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo);
	
	/**
	 * Gets the patientQueue for a patient at a given location which is not complete.
	 * 
	 * @param locationTo The Location where the patient was is queued to
	 * @param patient The patient who is in the queue
	 * @param queueRoom The room where patient has been sent
	 * @return a patient queue that meets the criteria of parameters
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo, Location queueRoom);
	
	/**
	 * Gets the most recent patientQueue of a patient
	 * 
	 * @param patient the patient whose most recent queue will be returned
	 * @return The most recent patient queue
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public PatientQueue getMostRecentQueue(Patient patient);
	
	/**
	 * Assigns a visit number to a patient queue
	 * 
	 * @param patientQueue the patient queue to be assigned a visit number
	 * @return patient queue that has been assigned a visit number
	 */
	@Transactional(readOnly = true)
	public PatientQueue assignVisitNumberForToday(PatientQueue patientQueue);
	
	/**
	 * Get Patient Queue List By search Params
	 * 
	 * @param searchString search string eg first name, last name, middle name.
	 * @param fromDate lowest date a query will be built upon. It can be null
	 * @param toDate highest date a query will be built upon. It Can be null
	 * @param locationTo Location Where patient was sent to
	 * @param locationFrom Location Where patient was sent from
	 * @param status Status such as COMPLETED,PENDING
	 * @return List<PatientQueue> A list of patientQueue that meet the parameters
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueListBySearchParams(String searchString, Date fromDate, Date toDate,
	        Location locationTo, Location locationFrom, PatientQueue.Status status);
	
	/**
	 * Get Patient Queue List By search Params
	 * 
	 * @param searchString search string eg first name, last name, middle name.
	 * @param fromDate lowest date a query will be built upon. It can be null
	 * @param toDate highest date a query will be built upon. It Can be null
	 * @param locationTo Location Where patient was sent to
	 * @param locationFrom Location Where patient was sent from
	 * @param status Status such as COMPLETED,PENDING
	 * @param queueRoom The room where the patient has been sent to
	 * @return List<PatientQueue> A list of patientQueue that meet the parameters
	 */
	
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueListBySearchParams(String searchString, Date fromDate, Date toDate,
	        Location locationTo, Location locationFrom, PatientQueue.Status status, Location queueRoom);
	
	/**
	 * Get a single patient queue record by queueId. The uuid can not be null
	 * 
	 * @param uuid Id of the patient queue to be retrieved
	 * @return The patient queue that matches the uuid
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	PatientQueue getPatientQueueByUuid(String uuid);
	
	/**
	 * Change status of a patient queue from pending to picked
	 * 
	 * @param patientQueue the queue which will be picked
	 * @param provider the health worker who is picking the patient
	 * @param queueRoom the specific room where the provider is at the time of picking the patient
	 * @return the queue which has been picked
	 */
	@Transactional
	@Authorized("Task: patientqueueing.manageQueue")
	PatientQueue pickPatientQueue(PatientQueue patientQueue, Provider provider, Location queueRoom);
	
	/**
	 * Get Patient Queues of parent location queue rooms
	 * 
	 * @param parentLocation the parent location of patient queue rooms to search patients from
	 * @param status Status such as COMPLETED,PENDING,PICKED
	 * @param fromDate lowest date a query will be built upon. It can be null
	 * @param toDate highest date a query will be built upon. It Can be null
	 * @param onlyInQueueRooms when set to true only includes patients in child locations with tag
	 *            queue room
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueByParentLocation(Location parentLocation, PatientQueue.Status status,
	        Date fromDate, Date toDate, boolean onlyInQueueRooms);
	
	/**
	 * Gets patient queue entries by visit number.
	 * <p>
	 * This returns all queue entries associated with the given visit number. When both
	 * {@code fromDate} and {@code toDate} are provided, only queue entries created within that date
	 * range are returned.
	 * 
	 * @param visitNumber the visit number used to find matching queue entries
	 * @param fromDate the start date for filtering by creation date; may be null
	 * @param toDate the end date for filtering by creation date; may be null
	 * @return a list of patient queue entries matching the given visit number
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueByVisitNumber(String visitNumber, Date fromDate, Date toDate);
	
	/**
	 * Gets patient queue entries in first-in-first-out order based on the supplied filters.
	 * <p>
	 * This method returns queue entries sorted by creation date in ascending order, so the oldest
	 * queue entry appears first. Any filter parameter may be null, in which case that filter is not
	 * applied.
	 * 
	 * @param provider the provider assigned to the queue entry; may be null
	 * @param fromDate the start date for filtering by creation date; may be null
	 * @param toDate the end date for filtering by creation date; may be null
	 * @param locationTo the destination location of the queue entry; may be null
	 * @param locationFrom the originating location of the queue entry; may be null
	 * @param patient the patient associated with the queue entry; may be null
	 * @param status the queue status to filter by; may be null
	 * @param queueRoom the queue room to filter by; may be null
	 * @return a FIFO-ordered list of patient queue entries matching the supplied filters
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueListFifo(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom);
	
	/**
	 * Gets patient queue entries for queue rooms under a parent location in first-in-first-out
	 * order.
	 * <p>
	 * This method resolves child locations under the given parent location and retrieves queue
	 * entries for those locations, sorted by creation date in ascending order. When
	 * {@code onlyInQueueRooms} is {@code true}, only child locations tagged as queue rooms are
	 * considered. If no matching child locations are found, this method returns {@code null}.
	 * 
	 * @param parentLocation the parent location whose child locations are to be searched
	 * @param status the queue status to filter by; may be null
	 * @param fromDate the start date for filtering by creation date; may be null
	 * @param toDate the end date for filtering by creation date; may be null
	 * @param onlyInQueueRooms whether to include only child locations tagged as queue rooms
	 * @return a FIFO-ordered list of patient queue entries for matching child locations, or
	 *         {@code null} if no matching child locations are found
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	public List<PatientQueue> getPatientQueueByParentLocationFifo(Location parentLocation, PatientQueue.Status status,
	        Date fromDate, Date toDate, boolean onlyInQueueRooms);
	
	// ========== Non-Patient Queue Methods ==========
	
	/**
	 * Get a NonPatientQueue by id
	 * 
	 * @param queueId the id of the queue entry
	 * @return the NonPatientQueue with the given id
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	NonPatientQueue getNonPatientQueueById(Integer queueId);
	
	/**
	 * Get a NonPatientQueue by uuid
	 * 
	 * @param uuid the uuid of the queue entry
	 * @return the NonPatientQueue with the given uuid
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	NonPatientQueue getNonPatientQueueByUuid(String uuid);
	
	/**
	 * Create a new non-patient queue entry with auto-generated ticket number
	 * 
	 * @param displayName the display name for the queue entry
	 * @param phoneNumber the phone number (optional)
	 * @param queueType the queue type concept
	 * @param currentLocation the current location
	 * @param locationTo the destination location
	 * @param queueRoom the queue room location
	 * @param priority the priority (optional)
	 * @param comment any additional comments (optional)
	 * @return the created NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.checkIn")
	NonPatientQueue createNonPatientQueueEntry(String displayName, String phoneNumber, Concept queueType,
	        Location currentLocation, Location locationTo, Location queueRoom, Integer priority, String comment);
	
	/**
	 * Save or update a NonPatientQueue entry
	 * 
	 * @param nonPatientQueue the queue entry to save
	 * @return the saved NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.manageQueue")
	NonPatientQueue saveNonPatientQueue(NonPatientQueue nonPatientQueue);
	
	/**
	 * Get NonPatientQueue entries by ticket number
	 * 
	 * @param ticketNumber the ticket number to search for
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return list of NonPatientQueue entries matching the ticket number
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getNonPatientQueueByTicketNumber(String ticketNumber, Date fromDate, Date toDate);
	
	/**
	 * Get NonPatientQueue entries by queue room
	 * 
	 * @param queueRoom the queue room location
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return list of NonPatientQueue entries in the queue room
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getNonPatientQueuesByQueueRoom(Location queueRoom, Date fromDate, Date toDate);
	
	/**
	 * Get NonPatientQueue entries by queue room and status
	 * 
	 * @param queueRoom the queue room location
	 * @param status the status to filter by
	 * @return list of NonPatientQueue entries matching the criteria
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getNonPatientQueuesByQueueRoomAndStatus(Location queueRoom,
	        NonPatientQueue.NonPatientQueueStatus status);
	
	/**
	 * Get all active NonPatientQueue entries (not completed or cancelled)
	 * 
	 * @return list of all active NonPatientQueue entries
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getAllActiveNonPatientQueues();
	
	/**
	 * Get NonPatientQueue entries with flexible filtering
	 * 
	 * @param status the status to filter by (can be null)
	 * @param queueType the queue type concept to filter by (can be null)
	 * @param locationTo the destination location to filter by (can be null)
	 * @param queueRoom the queue room to filter by (can be null)
	 * @param fromDate the start date for filtering (can be null)
	 * @param toDate the end date for filtering (can be null)
	 * @return list of NonPatientQueue entries matching the criteria
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getNonPatientQueues(NonPatientQueue.NonPatientQueueStatus status, Concept queueType,
	        Location locationTo, Location queueRoom, Date fromDate, Date toDate);
	
	/**
	 * Call a non-patient queue entry - transitions status from WAITING to CALLED
	 * 
	 * @param queue the queue entry to call
	 * @param provider the provider calling the queue entry
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	NonPatientQueue callNonPatientQueue(NonPatientQueue queue, Provider provider);
	
	/**
	 * Mark a non-patient queue entry as arrived - transitions status from CALLED to ARRIVED
	 * 
	 * @param queue the queue entry to mark as arrived
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.manageQueue")
	NonPatientQueue markNonPatientQueueArrived(NonPatientQueue queue);
	
	/**
	 * Start serving a non-patient queue entry - transitions status from ARRIVED to SERVING
	 * 
	 * @param queue the queue entry to start serving
	 * @param provider the provider serving the queue entry
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	NonPatientQueue startServingNonPatientQueue(NonPatientQueue queue, Provider provider);
	
	/**
	 * Complete a non-patient queue entry - transitions status from SERVING to COMPLETED
	 * 
	 * @param queue the queue entry to complete
	 * @param provider the provider completing the queue entry
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.completePatientQueue")
	NonPatientQueue completeNonPatientQueue(NonPatientQueue queue, Provider provider);
	
	/**
	 * Skip a non-patient queue entry - transitions status from CALLED to WAITING
	 * 
	 * @param queue the queue entry to skip
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.manageQueue")
	NonPatientQueue skipNonPatientQueue(NonPatientQueue queue);
	
	/**
	 * Cancel a non-patient queue entry - transitions status to CANCELLED
	 * 
	 * @param queue the queue entry to cancel
	 * @return the updated NonPatientQueue entry
	 */
	@Transactional
	@Authorized("Task: patientqueueing.manageQueue")
	NonPatientQueue cancelNonPatientQueue(NonPatientQueue queue);
	
	/**
	 * Generate a unique ticket number for a non-patient queue entry
	 * 
	 * @param location the location where the ticket is generated
	 * @param queueType the queue type concept
	 * @return a unique ticket number
	 */
	@Transactional(readOnly = true)
	String generateNonPatientQueueTicketNumber(Location location, Concept queueType);
	
	/**
	 * Gets non-patient queue entries for queue rooms under a parent location in first-in-first-out
	 * order.
	 * <p>
	 * This method resolves child locations under the given parent location and retrieves queue
	 * entries for those locations, sorted by creation date in ascending order. When
	 * {@code onlyInQueueRooms} is {@code true}, only child locations tagged as queue rooms are
	 * considered. If no matching child locations are found, this method returns {@code null}.
	 * 
	 * @param parentLocation the parent location whose child locations are to be searched
	 * @param status the queue status to filter by; may be null
	 * @param queueType the queue type concept to filter by; may be null
	 * @param fromDate the start date for filtering by creation date; may be null
	 * @param toDate the end date for filtering by creation date; may be null
	 * @param onlyInQueueRooms whether to include only child locations tagged as queue rooms
	 * @return a FIFO-ordered list of non-patient queue entries for matching child locations, or
	 *         {@code null} if no matching child locations are found
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	List<NonPatientQueue> getNonPatientQueuesByParentLocationFifo(Location parentLocation,
	        NonPatientQueue.NonPatientQueueStatus status, Concept queueType, Date fromDate, Date toDate,
	        boolean onlyInQueueRooms);
	
	/**
	 * Count pending patient queues by location. Returns a map of location UUID to count. This is
	 * more efficient than loading all queues and counting in Java.
	 * 
	 * @param locations the list of locations to count queues for
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return a map of location UUID to pending queue count
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	java.util.Map<String, Integer> countPendingQueuesByLocation(java.util.List<Location> locations, Date fromDate,
	        Date toDate);
	
	/**
	 * Get all unique patient IDs for today. Returns only patient IDs for efficiency, not full
	 * Patient objects.
	 * 
	 * @param fromDate the start date
	 * @param toDate the end date
	 * @return set of unique patient IDs
	 */
	@Transactional(readOnly = true)
	@Authorized("Task: patientqueueing.viewQueue")
	java.util.Set<Integer> getUniquePatientIdsForToday(Date fromDate, Date toDate);
	
	/**
	 * Count all non-patient queues created today.
	 * 
	 * @param fromDate the start date
	 * @param toDate the end date
	 * @return count of non-patient queues
	 */
	@Transactional(readOnly = true)
	Long countNonPatientQueuesToday(Date fromDate, Date toDate);
}
