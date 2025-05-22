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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.api.impl.PatientQueueingServiceImpl;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	
	@BeforeEach
	public void initialize() throws Exception {
		executeDataSet(QUEUE_STANDARD_DATASET_XML);
	}
	
	@InjectMocks
	PatientQueueingServiceImpl patientQueueingService;
	
	@Mock
	PatientQueueingDao dao;
	
	@BeforeEach
	public void setupMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void getIncompletePatientQueue_shouldReturnInCompletePatientQueue() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		PatientQueue patientQueue = patientQueueingService.getIncompletePatientQueue(patient, location);
		
		Assertions.assertNotNull(patientQueue);
		Assertions.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
	}
	
	@Test
	public void completePatientQueue_shouldSetAndReturnPatientQueueWithCompletedStatus() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assertions.assertNotNull(patientQueue);
		Assertions.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.completePatientQueue(patientQueue);
		
		PatientQueue completedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assertions.assertNotNull(completedPatientQueue);
		
		Assertions.assertEquals(PatientQueue.Status.COMPLETED, completedPatientQueue.getStatus());
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
		
		Assertions.assertEquals(originalPatientQueueList.size() + 1, newPatientQueueList.size());
		
		Assertions.assertEquals("20/10/2019-Unk-001", newPatientQueueList.get(0).getVisitNumber());
		
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
		
		Assertions.assertEquals(PatientQueue.Status.COMPLETED, patientQueue1.getStatus());
		
	}
	
	@Test
	public void generateVisitNumber_shouldReturnVisitNumberBasedOnPatientAndLocation() {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		Location location = Context.getLocationService().getLocation(1);
		
		String visitNumber = patientQueueingService.generateVisitNumber(location, patient);
		
		SimpleDateFormat formatter = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(
		    "patientqueueing.defaultDateFormat"));
		
		Assertions.assertEquals(formatter.format(new Date()) + "-Unk" + "-001", visitNumber);
		
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
		
		Assertions.assertEquals(visitNumber, patientQueue.getVisitNumber());
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
		
		Assertions.assertEquals(patientQueue.getVisitNumber(), patientQueue2.getVisitNumber());
	}
	
	@Test
	public void getMostRecentQueue_shouldReturnMostRecentPatientQueue() throws ParseException {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		
		PatientQueue patientQueue = patientQueueingService.getPatientQueueById(2);
		
		Assertions.assertEquals(patientQueue, patientQueueingService.getMostRecentQueue(patient));
		
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
		
		Assertions.assertEquals(QUEUE_PRIORITY_ONE, editedPatientQueue.getPriority());
		Assertions.assertEquals("Non-Emergency", editedPatientQueue.getPriorityComment());
		Assertions.assertEquals(PatientQueue.Status.PENDING, editedPatientQueue.getStatus());
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
		
		Assertions.assertNotEquals(STANDARD_VISIT_NUMBER_LENGTH, patientQueue.getVisitNumber());
		
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
		
		Assertions.assertEquals(1, patientQueueList.size());
		
		Assertions.assertEquals(patient, patientQueueList.get(0).getPatient());
		
		Assertions.assertEquals("Mukasa", patientQueueList.get(0).getPatient().getFamilyName());
	}
	
	@Test
	public void getPatientQueueListBySearchParams_shouldReturnNotReturnPatientQueuesThatDontMatchParameters()
	        throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(8);
		
		Location location = Context.getLocationService().getLocation(1);
		
		Assertions.assertEquals("Anet", patient.getGivenName());
		
		List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueListBySearchParams("Anet", null, null,
		    null, location, null);
		
		Assertions.assertEquals(0, patientQueueList.size());
		
	}
	
	@Test
	public void pickPatientQueue_shouldSetAndReturnPatientQueueWithPickedStatus() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assertions.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.pickPatientQueue(patientQueue, null, null);
		
		PatientQueue pickedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assertions.assertNotNull(pickedPatientQueue);
		
		Assertions.assertEquals(PatientQueue.Status.PICKED, pickedPatientQueue.getStatus());
		Assertions.assertNotNull(pickedPatientQueue.getDatePicked());
	}
	
	@Test
	public void completePatientQueue_shouldSetAndReturnPatientQueueWithDateCompleted() throws Exception {
		
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		Patient patient = Context.getPatientService().getPatient(10000);
		Location location = Context.getLocationService().getLocation(1);
		
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class).getPatientQueueList(null,
		    null, null, location, null, patient, PatientQueue.Status.PENDING);
		
		PatientQueue patientQueue = patientQueueList.get(0);
		
		Assertions.assertEquals(PatientQueue.Status.PENDING, patientQueue.getStatus());
		
		patientQueueingService.completePatientQueue(patientQueue);
		
		PatientQueue completedPatientQueue = patientQueueingService.getPatientQueueById(patientQueue.getPatientQueueId());
		
		Assertions.assertNotNull(completedPatientQueue);
		
		Assertions.assertEquals(PatientQueue.Status.COMPLETED, completedPatientQueue.getStatus());
		Assertions.assertNotNull(completedPatientQueue.getDateCompleted());
	}
	
	@Test
	public void getPatientsInQueueRoom_ShouldReturnPatientsInQueueRoomsOfParentLocation() throws ParseException {
		Location parentLocation = Context.getLocationService().getLocation(1);
		Date dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 19:08:26");
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class)
		        .getPatientQueueByParentLocation(parentLocation, PatientQueue.Status.PENDING,
		            OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), true);
		Assertions.assertEquals(2, patientQueueList.size());
		Assertions.assertEquals(patientQueueList.get(0).getQueueRoom().getParentLocation(), parentLocation);
		Assertions.assertEquals(patientQueueList.get(0).getQueueRoom().getName(), "Room 1");
	}
	
	@Test
	public void getPatientsInQueue_ShouldReturnPatientsInQueueChildLocationsOfParentLocation() throws ParseException {
		Location parentLocation = Context.getLocationService().getLocation(1);
		Date dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-07-07 19:08:26");
		List<PatientQueue> patientQueueList = Context.getService(PatientQueueingService.class)
		        .getPatientQueueByParentLocation(parentLocation, PatientQueue.Status.PENDING,
		            OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), false);
		Assertions.assertTrue(patientQueueList.size() > 1);
		Assertions.assertEquals(3, patientQueueList.size());
		Assertions.assertEquals(patientQueueList.get(2).getQueueRoom().getName(), "Sub Sub Room 1 R2");
	}
}
