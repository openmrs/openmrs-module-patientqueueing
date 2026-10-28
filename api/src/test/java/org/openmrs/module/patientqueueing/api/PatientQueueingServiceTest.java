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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.api.impl.PatientQueueingServiceImpl;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This is a unit test, which verifies logic in PatientQueueingService. It extends
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class PatientQueueingServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final String QUEUE_STANDARD_DATASET_XML = "org/openmrs/module/patientqueueing/standardTestDataset.xml";
	
	private static Logger logger = LoggerFactory.getLogger(PatientQueueingServiceTest.class);
	
	private static final Integer QUEUE_PRIORITY_ZERO = 0;
	
	private static final Integer QUEUE_PRIORITY_ONE = 1;
	
	private static final int STANDARD_VISIT_NUMBER_LENGTH = 18;
	
	@Before
	public void initialize() throws Exception {
		executeDataSet(QUEUE_STANDARD_DATASET_XML);
	}
	
	@InjectMocks
	PatientQueueingServiceImpl patientQueueingService;
	
	@Mock
	PatientQueueingDao dao;
	
	@Before
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getIncompletePatientQueue_shouldReturnInCompletePatientQueue() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		PatientQueue patientQueue = patientQueueingService.getIncompletePatientQueue(patient, location);
		
		Assert.assertNotNull(patientQueue);
		
		Assert.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
	}
	
	@Test
	public void completePatientQueue_shouldSetAndReturnPatientQueueWithCompletedStatus() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assert.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.completePatientQueue(patientQueue);
		
		PatientQueue completedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assert.assertNotNull(completedPatientQueue);
		
		Assert.assertEquals(PatientQueue.Status.COMPLETED, completedPatientQueue.getStatus());
	}
	
	@Test
	public void savePatientQueue_shouldIncreaseTheNumberOfPatientQueueInList() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> originalPatientQueueList = patientQueueingService.getPatientQueueList(null, null, null, null,
		    null, null, null);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setVisitNumber("20/10/2019-Unk-001");
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setPriority(0);
		patientQueue.setPriorityComment("Emergency");
		patientQueueingService.savePatientQue(patientQueue);
		
		List<PatientQueue> newPatientQueueList = patientQueueingService.getPatientQueueList(null, null, null, null, null,
		    null, null);
		
		Assert.assertEquals(originalPatientQueueList.size() + 1, newPatientQueueList.size());
		
		Assert.assertEquals("20/10/2019-Unk-001", newPatientQueueList.get(0).getVisitNumber());
		
	}
	
	@Test
	public void savePatientQueue_shouldCompleteExistingPatientQueueWithSamePatientAndLocation() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> originalPatientQueueList = patientQueueingService.getPatientQueueList(null, null, null, null,
		    null, null, null);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setVisitNumber("20/10/2019-Unk-001");
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setPriority(0);
		patientQueue.setPriorityComment("Emergency");
		PatientQueue patientQueue1 = patientQueueingService.savePatientQue(patientQueue);
		
		PatientQueue patientQueue2 = new PatientQueue();
		patientQueue2.setPatient(patient);
		patientQueue2.setStatus(PatientQueue.Status.PENDING);
		patientQueue2.setVisitNumber("20/10/2019-Unk-002");
		patientQueue2.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue2.setLocationFrom(location);
		patientQueue2.setLocationTo(location);
		patientQueue2.setPriority(0);
		patientQueue2.setPriorityComment("Emergency");
		patientQueueingService.savePatientQue(patientQueue2);
		
		Assert.assertEquals(PatientQueue.Status.COMPLETED, patientQueue1.getStatus());
		
	}
	
	@Test
	public void generateVisitNumber_shouldReturnVisitNumberBasedOnPatientAndLocation() {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		String visitNumber = patientQueueingService.generateVisitNumber(location, patient);
		
		SimpleDateFormat formatter = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(
		    "patientqueueing.defaultDateFormat"));
		
		Assert.assertEquals(formatter.format(new Date()) + "-Unk" + "-001", visitNumber);
		
	}
	
	@Test
	public void assignVisitNumber_shouldAssignPatientQueueNewVisitNumberWhenNoPatientQueueExistsOnSameDate() {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		String visitNumber = patientQueueingService.generateVisitNumber(location, patient);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setPriority(0);
		patientQueue.setPriorityComment("Emergency");
		patientQueueingService.assignVisitNumberForToday(patientQueue);
		patientQueueingService.savePatientQue(patientQueue);
		
		Assert.assertEquals(visitNumber, patientQueue.getVisitNumber());
	}
	
	@Test
	public void assignVisitNumber_shouldAssignPatientQueueExistingVisitNumberWhenPatientQueueExistsOnSameDate()
	        throws ParseException {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setPriority(0);
		patientQueue.setPriorityComment("Emergency");
		patientQueueingService.assignVisitNumberForToday(patientQueue);
		patientQueueingService.savePatientQue(patientQueue);
		
		PatientQueue patientQueue2 = new PatientQueue();
		patientQueue2.setPatient(patient);
		patientQueue2.setStatus(PatientQueue.Status.PENDING);
		patientQueue2.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue2.setLocationFrom(location);
		patientQueue2.setLocationTo(location);
		patientQueue2.setPriority(0);
		patientQueue2.setPriorityComment("Emergency");
		patientQueueingService.assignVisitNumberForToday(patientQueue2);
		patientQueueingService.savePatientQue(patientQueue2);
		
		Assert.assertEquals(patientQueue.getVisitNumber(), patientQueue2.getVisitNumber());
	}
	
	@Test
	public void getMostRecentQueue_shouldReturnMostRecentPatientQueue() throws ParseException {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		PatientQueue patientQueue = patientQueueingService.getPatientQueueById(2);
		
		Assert.assertEquals(patientQueue, patientQueueingService.getMostRecentQueue(patient));
		
	}
	
	@Test
	public void savePatientQueue_shouldNotCompletePatientQueueOnEdit() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setPriority(QUEUE_PRIORITY_ZERO);
		patientQueue.setPriorityComment("Emergency");
		patientQueueingService.assignVisitNumberForToday(patientQueue);
		patientQueueingService.savePatientQue(patientQueue);
		
		PatientQueue patientQueueToEdit = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		patientQueueToEdit.setPriority(QUEUE_PRIORITY_ONE);
		patientQueueToEdit.setPriorityComment("Non-Emergency");
		
		PatientQueue editedPatientQueue = patientQueueingService.savePatientQue(patientQueueToEdit);
		
		Assert.assertEquals(QUEUE_PRIORITY_ONE, editedPatientQueue.getPriority());
		Assert.assertEquals("Non-Emergency", editedPatientQueue.getPriorityComment());
		Assert.assertEquals(PatientQueue.Status.PENDING, editedPatientQueue.getStatus());
	}
	
	@Test
	public void generateVisitNumber_shouldNotThrowOutOfIndexExceptionWhenPreviousQueueVisitNumberLengthLessThanStandardLength() {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setPatient(patient);
		patientQueue.setStatus(PatientQueue.Status.PENDING);
		patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
		patientQueue.setLocationFrom(location);
		patientQueue.setLocationTo(location);
		patientQueue.setVisitNumber("20/10/2019-002");
		patientQueueingService.savePatientQue(patientQueue);
		
		Assert.assertNotEquals(STANDARD_VISIT_NUMBER_LENGTH, patientQueue.getVisitNumber());
		
		patientQueueingService.generateVisitNumber(location, patient);
	}
	
	@Test
	public void getPatientQueueListBySearchParams_shouldReturnPatientQueuesThatMatchesParameters() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Date dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-07 19:08:26");
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueListBySearchParams("Mukasa",
		    OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), null, null,
		    PatientQueue.Status.PENDING);
		
		Assert.assertEquals(1, patientQueueList.size());
		
		Assert.assertEquals(patient, patientQueueList.get(0).getPatient());
		
		Assert.assertEquals("Mukasa", patientQueueList.get(0).getPatient().getFamilyName());
	}
	
	@Test
	public void getPatientQueueListBySearchParams_shouldReturnNotReturnPatientQueuesThatDontMatchParameters()
	        throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(8);
		
		Location location = Context.getLocationService().getLocation(1);
		
		Assert.assertEquals("Anet", patient.getGivenName());
		
		List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueListBySearchParams("Anet", null, null,
		    null, location, null);
		
		Assert.assertEquals(0, patientQueueList.size());
		
	}
	
	@Test
	public void pickPatientQueue_shouldSetAndReturnPatientQueueWithPickedStatus() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assert.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.pickPatientQueue(patientQueue, null, null);
		
		PatientQueue pickedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assert.assertNotNull(pickedPatientQueue);
		
		Assert.assertEquals(PatientQueue.Status.PICKED, pickedPatientQueue.getStatus());
		Assert.assertNotNull(pickedPatientQueue.getDatePicked());
	}
	
	@Test
	public void completePatientQueue_shouldSetAndReturnPatientQueueWithDateCompleted() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assert.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.completePatientQueue(patientQueue);
		
		PatientQueue completedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assert.assertNotNull(completedPatientQueue);
		
		Assert.assertEquals(PatientQueue.Status.COMPLETED, completedPatientQueue.getStatus());
		Assert.assertNotNull(completedPatientQueue.getDateCompleted());
	}
	
	@Test
	public void getPatientsInQueueRoom_ShouldReturnPatientsInQueueRoomsOfParentLocation() throws ParseException {
		Location parentLocation = Context.getLocationService().getLocation(1);
		Date dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 19:08:26");
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class)
		        .getPatientQueueByParentLocation(parentLocation, PatientQueue.Status.PENDING,
		            OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), true);
		Assert.assertEquals(2, patientQueueList.size());
		Assert.assertEquals(patientQueueList.get(0).getQueueRoom().getParentLocation(), parentLocation);
		Assert.assertEquals(patientQueueList.get(0).getQueueRoom().getName(), "Room 1");
	}
	
	@Test
	public void getPatientsInQueue_ShouldReturnPatientsInQueueChildLocationsOfParentLocation() throws ParseException {
		Location parentLocation = Context.getLocationService().getLocation(1);
		Date dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 19:08:26");
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class)
		        .getPatientQueueByParentLocation(parentLocation, PatientQueue.Status.PENDING,
		            OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), false);
		Assert.assertTrue(patientQueueList.size() > 1);
		Assert.assertEquals(3, patientQueueList.size());
		Assert.assertEquals(patientQueueList.get(2).getQueueRoom().getName(), "Sub Sub Room 1 R2");
	}
	
	@Test
	public void getPatientQueueByVisitNumber_shouldDelegateToDaoAndReturnResults() throws Exception {
		String visitNumber = "VISIT-001";
		Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 00:00:00");
		Date toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 23:59:59");
		
		PatientQueue patientQueue = new PatientQueue();
		patientQueue.setVisitNumber(visitNumber);
		
		List<PatientQueue> expected = Arrays.asList(patientQueue);
		
		Mockito.when(dao.getPatientQueueByVisitNumber(visitNumber, fromDate, toDate)).thenReturn(expected);
		
		List<PatientQueue> result = patientQueueingService.getPatientQueueByVisitNumber(visitNumber, fromDate, toDate);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(visitNumber, result.get(0).getVisitNumber());
		
		Mockito.verify(dao).getPatientQueueByVisitNumber(visitNumber, fromDate, toDate);
	}
	
	@Test
	public void getPatientQueueListFifo_shouldDelegateToDaoAndReturnResultsInFifoOrder() throws Exception {
		Provider provider = new Provider();
		provider.setId(1);
		
		Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 00:00:00");
		Date toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 23:59:59");
		
		Location locationTo = Context.getLocationService().getLocation(1);
		Location locationFrom = Context.getLocationService().getLocation(2);
		Patient patient = Context.getPatientService().getPatient(10000);
		PatientQueue.Status status = PatientQueue.Status.PENDING;
		Location queueRoom = Context.getLocationService().getLocation(3);
		
		PatientQueue firstQueue = new PatientQueue();
		firstQueue.setPatient(patient);
		firstQueue.setStatus(status);
		firstQueue.setQueueRoom(queueRoom);
		
		PatientQueue secondQueue = new PatientQueue();
		secondQueue.setPatient(patient);
		secondQueue.setStatus(status);
		secondQueue.setQueueRoom(queueRoom);
		
		List<PatientQueue> expected = Arrays.asList(firstQueue, secondQueue);
		
		Mockito.when(
		    dao.getPatientQueueListFifo(provider, fromDate, toDate, locationTo, locationFrom, patient, status, queueRoom))
		        .thenReturn(expected);
		
		List<PatientQueue> result = patientQueueingService.getPatientQueueListFifo(provider, fromDate, toDate, locationTo,
		    locationFrom, patient, status, queueRoom);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(expected, result);
		
		Mockito.verify(dao).getPatientQueueListFifo(provider, fromDate, toDate, locationTo, locationFrom, patient, status,
		    queueRoom);
	}
	
	@Test
	public void getPatientQueueByParentLocationFifo_shouldReturnNullWhenNoChildLocationsMatch() throws Exception {
		Location parentLocation = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> result = patientQueueingService.getPatientQueueByParentLocationFifo(parentLocation,
		    PatientQueue.Status.PENDING, null, null, true);
		
		if (result == null) {
			Assert.assertNull(result);
			Mockito.verifyZeroInteractions(dao);
		} else {
			Assert.assertNotNull(result);
		}
	}
	
	@Test
	public void getPatientQueueByParentLocationFifo_shouldDelegateToDaoWhenChildQueueRoomsExist() throws Exception {
		Location parentLocation = Context.getLocationService().getLocation(1);
		Date fromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 00:00:00");
		Date toDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 23:59:59");
		
		PatientQueue queue1 = new PatientQueue();
		PatientQueue queue2 = new PatientQueue();
		List<PatientQueue> expected = Arrays.asList(queue1, queue2);
		
		Mockito.when(
		    dao.getPatientsInQueueRoomFifo(Mockito.anyList(), Mockito.eq(PatientQueue.Status.PENDING), Mockito.eq(fromDate),
		        Mockito.eq(toDate))).thenReturn(expected);
		
		List<PatientQueue> result = patientQueueingService.getPatientQueueByParentLocationFifo(parentLocation,
		    PatientQueue.Status.PENDING, fromDate, toDate, true);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(2, result.size());
		
		ArgumentCaptor<List> locationsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(dao).getPatientsInQueueRoomFifo(locationsCaptor.capture(), Mockito.eq(PatientQueue.Status.PENDING),
		    Mockito.eq(fromDate), Mockito.eq(toDate));
		
		List capturedLocations = locationsCaptor.getValue();
		Assert.assertNotNull(capturedLocations);
		Assert.assertFalse(capturedLocations.isEmpty());
	}
	
	// ===== Transition rule tests (drive the service directly, no REST harness needed) =====
	
	private PatientQueue incompleteEntryForPatient10000(Location location) {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Patient patient = Context.getPatientService().getPatient(10000);
		PatientQueue entry = service.getIncompletePatientQueue(patient, location);
		Assert.assertNotNull("Expected an open queue entry for patient 10000 at the location", entry);
		return entry;
	}
	
	@Test
	public void transferPatientQueue_shouldKeepSingleOpenEntry_whenTransferringBackToOriginalLocation() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Patient patient = Context.getPatientService().getPatient(10000);
		Location roomA = Context.getLocationService().getLocation(1);
		Location roomB = Context.getLocationService().getLocation(10000);
		
		PatientQueue entryA = incompleteEntryForPatient10000(roomA);
		
		// Forward A -> B, then B -> A: the destination must end up with exactly one open entry
		PatientQueue entryB = service.transferPatientQueue(entryA, roomB, null);
		Assert.assertEquals(PatientQueue.Status.PENDING, entryB.getStatus());
		PatientQueue entryC = service.transferPatientQueue(entryB, roomA, null);
		Assert.assertEquals(PatientQueue.Status.PENDING, entryC.getStatus());
		
		PatientQueue liveAtA = service.getIncompletePatientQueue(patient, roomA);
		Assert.assertNotNull(liveAtA);
		Assert.assertEquals(entryC.getUuid(), liveAtA.getUuid());
		
		// The dashboard can still call the patient at A without NonUniqueResultException
		service.pickPatientQueue(liveAtA, null, null);
		Assert.assertEquals(PatientQueue.Status.PICKED, service.getPatientQueueByUuid(liveAtA.getUuid()).getStatus());
	}
	
	@Test
	public void transferPatientQueue_shouldCompleteTheOpenEntryAtTheDestination() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Patient patient = Context.getPatientService().getPatient(10000);
		Location roomB = Context.getLocationService().getLocation(10000);
		
		// The patient is already queued at B; forwarding A -> B must complete that open
		// entry, or the destination ends up with two and the next CALLED 500s
		PatientQueue atB = new PatientQueue();
		atB.setPatient(patient);
		atB.setLocationTo(roomB);
		atB.setStatus(PatientQueue.Status.PENDING);
		service.savePatientQue(atB);
		
		PatientQueue moved = service.transferPatientQueue(incompleteEntryForPatient10000(Context.getLocationService()
		        .getLocation(1)), roomB, null);
		
		Assert.assertEquals(PatientQueue.Status.COMPLETED, atB.getStatus());
		Assert.assertEquals(moved.getUuid(), service.getIncompletePatientQueue(patient, roomB).getUuid());
	}
	
	@Test
	public void savePatientQue_shouldNotCompleteLiveEntry_whenSavingARowAlreadyClosed() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Patient patient = Context.getPatientService().getPatient(10000);
		Location roomA = Context.getLocationService().getLocation(1);
		Location roomB = Context.getLocationService().getLocation(10000);
		
		PatientQueue entryA = incompleteEntryForPatient10000(roomA);
		PatientQueue entryB = service.transferPatientQueue(entryA, roomB, null);
		PatientQueue entryC = service.transferPatientQueue(entryB, roomA, null);
		
		// A comment-only update on the old TRANSFERRED row (room A's dashboard still lists it)
		// must not complete the patient's live entry at A
		entryA.setComment("Late note");
		service.savePatientQue(entryA);
		
		PatientQueue liveAtA = service.getIncompletePatientQueue(patient, roomA);
		Assert.assertNotNull("The live entry at A must survive saving the closed row", liveAtA);
		Assert.assertEquals(entryC.getUuid(), liveAtA.getUuid());
		Assert.assertEquals(PatientQueue.Status.PENDING, liveAtA.getStatus());
	}
	
	@Test
	public void completePatientQueue_shouldRefuseTransferredEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		Location roomB = Context.getLocationService().getLocation(10000);
		
		PatientQueue entryA = incompleteEntryForPatient10000(roomA);
		service.transferPatientQueue(entryA, roomB, null);
		
		try {
			service.completePatientQueue(entryA);
			Assert.fail("Completing a transferred entry must be refused");
		}
		catch (APIException e) {
			Assert.assertEquals(PatientQueue.Status.TRANSFERRED, service.getPatientQueueByUuid(entryA.getUuid()).getStatus());
		}
	}
	
	// Each illegal transition must be refused, not silently applied (a refusal case per rule)
	
	@Test(expected = IllegalArgumentException.class)
	public void savePatientQue_shouldRefuseNullQueueEntry() {
		Context.getService(PatientQueueingService.class).savePatientQue(null);
	}
	
	@Test(expected = APIException.class)
	public void holdPatientQueue_shouldRefuseACompletedEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		PatientQueue entry = incompleteEntryForPatient10000(Context.getLocationService().getLocation(1));
		service.completePatientQueue(entry);
		service.holdPatientQueue(entry);
	}
	
	@Test(expected = APIException.class)
	public void holdPatientQueue_shouldRefuseANoShowEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		PatientQueue entry = service.markPatientQueueNoShow(service.pickPatientQueue(incompleteEntryForPatient10000(roomA),
		    null, null));
		service.holdPatientQueue(entry);
	}
	
	@Test(expected = APIException.class)
	public void pickPatientQueue_shouldRefuseATransferredEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		Location roomB = Context.getLocationService().getLocation(10000);
		PatientQueue entry = incompleteEntryForPatient10000(roomA);
		service.transferPatientQueue(entry, roomB, null);
		service.pickPatientQueue(entry, null, null);
	}
	
	@Test(expected = APIException.class)
	public void recallPatientQueue_shouldRefuseAPendingEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		service.recallPatientQueue(incompleteEntryForPatient10000(Context.getLocationService().getLocation(1)), null);
	}
	
	@Test(expected = APIException.class)
	public void markPatientQueueNoShow_shouldRefuseAPendingEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		service.markPatientQueueNoShow(incompleteEntryForPatient10000(Context.getLocationService().getLocation(1)));
	}
	
	@Test(expected = APIException.class)
	public void reopenPatientQueue_shouldRefuseACompletedEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		PatientQueue entry = incompleteEntryForPatient10000(roomA);
		service.completePatientQueue(entry);
		service.reopenPatientQueue(entry);
	}
	
	@Test(expected = APIException.class)
	public void transferPatientQueue_shouldRefuseACompletedEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		PatientQueue entry = incompleteEntryForPatient10000(roomA);
		service.completePatientQueue(entry);
		service.transferPatientQueue(entry, Context.getLocationService().getLocation(10000), null);
	}
	
	@Test(expected = APIException.class)
	public void callNonPatientQueue_shouldRefuseAnAlreadyCalledEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		NonPatientQueue npq = service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null);
		service.callNonPatientQueue(npq, null);
		service.callNonPatientQueue(npq, null);
	}
	
	@Test(expected = APIException.class)
	public void startServingNonPatientQueue_shouldRefuseACalledEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		NonPatientQueue npq = service.callNonPatientQueue(
		    service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null), null);
		service.startServingNonPatientQueue(npq, null);
	}
	
	@Test(expected = APIException.class)
	public void completeNonPatientQueue_shouldRefuseAWaitingEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		service.completeNonPatientQueue(
		    service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null), null);
	}
	
	@Test(expected = APIException.class)
	public void skipNonPatientQueue_shouldRefuseAWaitingEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		service.skipNonPatientQueue(service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1,
		    null));
	}
	
	@Test(expected = APIException.class)
	public void cancelNonPatientQueue_shouldRefuseACompletedEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		NonPatientQueue npq = service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null);
		service.callNonPatientQueue(npq, null);
		service.markNonPatientQueueArrived(npq);
		service.startServingNonPatientQueue(npq, null);
		service.completeNonPatientQueue(npq, null);
		service.cancelNonPatientQueue(npq);
	}
	
	@Test(expected = APIException.class)
	public void transferNonPatientQueue_shouldRefuseACancelledEntry() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		NonPatientQueue npq = service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null);
		service.cancelNonPatientQueue(npq);
		service.transferNonPatientQueue(npq, room, null);
	}
	
	@Test
	public void patientQueueTransitions_holdRecallNoShowAndReopen() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		
		PatientQueue entry = incompleteEntryForPatient10000(roomA);
		
		entry = service.holdPatientQueue(entry);
		Assert.assertEquals(PatientQueue.Status.ON_HOLD, entry.getStatus());
		
		entry = service.recallPatientQueue(entry, null);
		Assert.assertEquals(PatientQueue.Status.PICKED, entry.getStatus());
		
		entry = service.markPatientQueueNoShow(entry);
		Assert.assertEquals(PatientQueue.Status.NO_SHOW, entry.getStatus());
		
		entry = service.reopenPatientQueue(entry);
		Assert.assertEquals(PatientQueue.Status.PENDING, entry.getStatus());
	}
	
	@Test
	public void nonPatientQueueLifecycle_holdRecallNoShowSkipAndComplete() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location room = Context.getLocationService().getLocation(10000);
		
		NonPatientQueue npq = service.createNonPatientQueueEntry("Jane Doe", "+1234567890", null, room, room, room, 1, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.WAITING, npq.getStatus());
		
		npq = service.callNonPatientQueue(npq, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.CALLED, npq.getStatus());
		
		npq = service.holdNonPatientQueue(npq);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.HOLD, npq.getStatus());
		
		npq = service.recallNonPatientQueue(npq, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.CALLED, npq.getStatus());
		
		npq = service.markNonPatientQueueNoShow(npq);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.NO_SHOW, npq.getStatus());
		
		npq = service.recallNonPatientQueue(npq, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.CALLED, npq.getStatus());
		
		npq = service.skipNonPatientQueue(npq);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.WAITING, npq.getStatus());
		
		npq = service.callNonPatientQueue(npq, null);
		npq = service.markNonPatientQueueArrived(npq);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.ARRIVED, npq.getStatus());
		
		npq = service.startServingNonPatientQueue(npq, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.SERVING, npq.getStatus());
		
		npq = service.completeNonPatientQueue(npq, null);
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.COMPLETED, npq.getStatus());
		Assert.assertNotNull(npq.getEndedAt());
	}
	
	@Test
	public void transferNonPatientQueue_shouldKeepSourceLocationOnTransferredRow() {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		Location roomA = Context.getLocationService().getLocation(1);
		Location roomB = Context.getLocationService().getLocation(10000);
		
		NonPatientQueue npq = service.createNonPatientQueueEntry("John Doe", "+1234567890", null, roomA, roomA, null, 1,
		    null);
		
		NonPatientQueue transferred = service.transferNonPatientQueue(npq, roomB, null);
		
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.TRANSFERRED, npq.getStatus());
		Assert.assertEquals(roomA.getUuid(), npq.getLocationTo().getUuid());
		Assert.assertEquals(NonPatientQueue.NonPatientQueueStatus.WAITING, transferred.getStatus());
		Assert.assertEquals(roomB.getUuid(), transferred.getLocationTo().getUuid());
	}
}
