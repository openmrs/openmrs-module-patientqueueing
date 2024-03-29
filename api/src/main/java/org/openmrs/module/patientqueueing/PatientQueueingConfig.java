/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing;

import org.springframework.stereotype.Component;

/**
 * Contains module's config.
 */
@Component("patientqueueing.PatientQueueingConfig")
public class PatientQueueingConfig {
	
	public final static String MODULE_PRIVILEGE = "Patient Queueing Privilege";
	
	public static final String MODULE_ID = "patientqueueing";
	
	public static final String ROOM_TAG_UUID = "c0e1d1d8-c97d-4869-ba16-68d351d3d5f5";
}
