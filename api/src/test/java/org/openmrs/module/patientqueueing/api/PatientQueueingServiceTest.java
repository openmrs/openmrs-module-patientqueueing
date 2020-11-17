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
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.dao.PatientQueueingDao;
import org.openmrs.module.patientqueueing.api.impl.PatientQueueingServiceImpl;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.test.BaseModuleContextSensitiveTest;
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


        List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueListBySearchParams("Mukasa", OpenmrsUtil.firstSecondOfDay(dateCreated), OpenmrsUtil.getLastMomentOfDay(dateCreated), null, null, PatientQueue.Status.PENDING);

        Assert.assertEquals(1, patientQueueList.size());

        Assert.assertEquals(patient, patientQueueList.get(0).getPatient());

        Assert.assertEquals("Mukasa", patientQueueList.get(0).getPatient().getFamilyName());
    }

    @Test
    public void getPatientQueueListBySearchParams_shouldReturnNotReturnPatientQueuesThatDontMatchParameters() throws Exception {

        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        Patient patient = Context.getPatientService().getPatient(8);

        Location location = Context.getLocationService().getLocation(1);

        Assert.assertEquals("Anet", patient.getGivenName());

        List<PatientQueue> patientQueueList = patientQueueingService.getPatientQueueListBySearchParams("Anet", null, null, null, location, null);

        Assert.assertEquals(0, patientQueueList.size());

    }

    @Test
    public void saveQueueRoom_shouldReturnASavedQueueRoom() {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        QueueRoom queueRoom = new QueueRoom();
        queueRoom.setDateCreated(new Date());
        queueRoom.setName("Consultation 1");
        queueRoom.setLocation(Context.getLocationService().getLocation(1));
        queueRoom.setDescription("This is a consultation room 1");
        queueRoom.setCreator(Context.getAuthenticatedUser());
        patientQueueingService.saveQueueRoom(queueRoom);
        Assert.assertNotNull(queueRoom.getId());
    }


    @Test
    public void savePatientQueue_shouldSavePatientQueueWithQueueRoom() throws Exception {

        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        Patient patient = Context.getPatientService().getPatient(10000);

        Location location = Context.getLocationService().getLocation(1);

        PatientQueue patientQueue = new PatientQueue();
        patientQueue.setPatient(patient);
        patientQueue.setStatus(PatientQueue.Status.PENDING);
        patientQueue.setVisitNumber("20/10/2019-Unk-0019");
        patientQueue.setEncounter(Context.getEncounterService().getEncounter(10000));
        patientQueue.setLocationFrom(location);
        patientQueue.setLocationTo(location);
        patientQueue.setQueueRoom(patientQueueingService.getQueueRoomById(1));
        patientQueueingService.savePatientQue(patientQueue);

        List<PatientQueue> newPatientQueueList = patientQueueingService.getPatientQueueList(null, null, null, null, null,
                null, null);

        Assert.assertEquals("Consultation Room 1", newPatientQueueList.get(0).getQueueRoom().getName());

    }


    @Test
    public void getQueueRoomByUUID_shouldGetExistingQueueRoom() {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        QueueRoom queueRoom = patientQueueingService.getQueueRoomByUUID("bafc0239-9c15-4bdc-93fb-6df905ff38e0");
        Assert.assertEquals("Consultation Room 3", queueRoom.getName());
    }

    @Test
    public void getQueueRoomById_shouldGetExistingQueueRoom() {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        QueueRoom queueRoom = patientQueueingService.getQueueRoomById(2);
        Assert.assertEquals("Consultation Room 2", queueRoom.getName());
    }

    @Test
    public void getQueueRoomByLocation_shouldGetListOfExistingQueueRoom() {
        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);

        Location location = Context.getLocationService().getLocation(1);
        List<QueueRoom> queueRooms = patientQueueingService.getQueueRoomByLocation(location);
        Assert.assertTrue(queueRooms.size()>=3);
        Assert.assertEquals(location, queueRooms.get(0).getLocation());
    }
}
