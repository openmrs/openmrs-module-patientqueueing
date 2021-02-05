/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.web.recource;

import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.junit.Ignore;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.web.resource.PatientQueueResource;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

/**
 * Contains tests for the {@link PatientQueueResource}
 */
@Ignore
public class PatientQueueTest extends BaseDelegatingResourceTest<PatientQueueResource, PatientQueue> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#newObject()
	 */
	@Override
	public PatientQueue newObject() {
		return Context.getService(PatientQueueingService.class).getPatientQueueById(Integer.parseInt(getUuidProperty()));
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("uuid");
		assertPropPresent("status");
		assertPropPresent("location");
		assertPropPresent("dateCreated");
		assertPropPresent("visitNumber");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("uuid");
		assertPropPresent("creator");
		assertPropPresent("dateCreated");
		assertPropPresent("changedBy");
		assertPropPresent("dateChanged");
		assertPropPresent("dateVoided");
		assertPropPresent("voidedBy");
		assertPropPresent("patient");
		assertPropPresent("provider");
		assertPropPresent("locationFrom");
		assertPropPresent("locationTo");
		assertPropPresent("encounter");
		assertPropPresent("status");
		assertPropPresent("priority");
		assertPropPresent("priorityComment");
		assertPropPresent("visitNumber");
		assertPropPresent("comment");
		assertPropPresent("queueRoom");
		assertPropEquals("voided", getObject().isVoided());
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getDisplayProperty()
	 */
	@Override
	public String getDisplayProperty() {
		return "Initial HIV Clinic PatientQueue @ Unknown Location - 01/01/2005 00:00";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest#getUuidProperty()
	 */
	@Override
	public String getUuidProperty() {
		return "2";
	}
}
