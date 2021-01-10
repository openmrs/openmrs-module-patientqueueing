/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 *//*


package org.openmrs.module.patientqueueing.web.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;

public class PatientQueueControllerTest extends MainResourceControllerTest {
	
	private PatientQueueingService service;
	
	@Before
	public void before() {
		this.service = Context.getService(PatientQueueingService.class);
	}
	
	*/
/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 *//*

	
	@Override
	public String getURI() {
		return "patientqueue";
	}
	
	*/
/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 *//*

	
	@Override
	public String getUuid() {
		return "4102d6e2-9fb4-4084-b412-8e3838479170";
	}
	
	*/
/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 *//*

	
	@Override
	public long getAllCount() {
		return service.getPatientQueueList(null, null, null, null, null, null, null).size();
	}
	
	@Test
	public void shouldCreateAPatientQueue() throws Exception {
		int originalCount = service.getPatientQueueList(null, null, null, null, null, null, null).size();
		String json = "";
		
		Object newPatientQueue = deserialize(handle(newPostRequest(getURI(), json)));
		
		Assert.assertNotNull(PropertyUtils.getProperty(newPatientQueue, "uuid"));
		Assert.assertEquals(originalCount + 1, service.getPatientQueueList(null, null, null, null, null, null, null).size());
	}
	
	@Test
	public void shouldEditAPatientQueue() throws Exception {
		
		final PatientQueue.Status newStatus = PatientQueue.Status.PICKED;
		PatientQueue patientQueue = service.getPatientQueueByUuid(getUuid());
		Assert.assertNotNull(patientQueue);
		
		//sanity checks
		Assert.assertFalse(newStatus.equals(patientQueue.getStatus()));
		
		String json = "";
		
		handle(newPostRequest(getURI() + "/" + getUuid(), json));
		
		PatientQueue updated = service.getPatientQueueByUuid(getUuid());
		Assert.assertNotNull(updated);
		Assert.assertEquals(newStatus, patientQueue.getStatus());
		
	}
	
	@Test
	public void shouldVoidAPatientQueue() throws Exception {
		PatientQueue patientQueue = service.getPatientQueueByUuid(getUuid());
		Assert.assertFalse(patientQueue.isVoided());
		
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", "test reason")));

		patientQueue = service.getPatientQueueByUuid(getUuid());
		Assert.assertTrue(patientQueue.isVoided());
		Assert.assertEquals("test reason", patientQueue.getVoidReason());
	}
	
}
*/
