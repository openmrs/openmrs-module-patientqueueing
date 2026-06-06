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
 * Central configuration class for the patient queueing module. Contains all global property names,
 * constants, and default values.
 */
@Component("patientqueueing.PatientQueueingConfig")
public final class PatientQueueingConfig {

	private PatientQueueingConfig() {
	}

	// Module identifiers

	public static final String MODULE_ID = "patientqueueing";

	public static final String MODULE_PRIVILEGE = "Patient Queueing Privilege";

	public static final String ROOM_TAG_UUID = "c0e1d1d8-c97d-4869-ba16-68d351d3d5f5";

	// Global Property Names - Provider Assignment

	public static final String GP_PROVIDER_ASSIGNMENT_STRATEGY = "patientqueueing.providerAssignmentStrategy";

	public static final String GP_PROVIDER_LOCATION_ATTRIBUTE_TYPE_UUID = "patientqueueing.providerLocationAttributeTypeUuid";

	public static final String GP_PERSON_LOCATION_ATTRIBUTE_NAME = "patientqueueing.personLocationAttributeName";

	public static final String GP_AUTO_ASSIGN_PROVIDER = "patientqueueing.autoAssignProvider";

	public static final String GP_DEFAULT_VISIT_TYPE = "patientqueueing.defaultVisitTypeUuid";

	// Global Property Names - Patient Identifier Configuration

	public static final String GP_PHONE_NUMBER_ATTRIBUTE_TYPE = "patientqueueing.phoneNumberAttributeTypeUuid";

	// Global Property Names - Queue Type Configuration

	public static final String GP_ESTIMATED_WAIT_MINUTES_PER_PERSON = "patientqueueing.estimatedWaitMinutesPerPerson";

	// Global Property Names - Self Check-In

	public static final String GP_SELF_CHECK_IN_PERSON_ATTRIBUTE_TYPE_UUIDS = "patientqueueing.selfCheckInPersonAttributeTypeUuids";

	public static final String GP_SELF_CHECK_IN_IDENTIFIER_TYPE_UUIDS = "patientqueueing.selfCheckInIdentifierTypeUuids";

	public static final String GP_SELF_CHECK_IN_VISIT_TYPE_UUID = "patientqueueing.selfCheckInVisitTypeUuid";

	// Default Values

	public static final String DEFAULT_PROVIDER_ASSIGNMENT_STRATEGY = "leastBusy";

	public static final boolean DEFAULT_AUTO_ASSIGN_PROVIDER = true;

	public static final int DEFAULT_ESTIMATED_WAIT_MINUTES_PER_PERSON = 5;

	public static final String DEFAULT_PHONE_ATTRIBUTE_TYPE_UUID = "14d4f066-15f5-102d-96e4-000c29c2a5d7"; // Telephone Number

	// Provider Assignment Strategy Options

	public interface ProviderAssignmentStrategy {

		String LEAST_BUSY = "leastBusy";

		String ROUND_ROBIN = "roundRobin";

		String RANDOM = "random";

		String MANUAL = "manual";
	}

	/**
	 * Validates if a provider assignment strategy is valid
	 *
	 * @param strategy the strategy to validate
	 * @return true if the strategy is valid, false otherwise
	 */
	public static boolean isValidProviderAssignmentStrategy(String strategy) {
		return ProviderAssignmentStrategy.LEAST_BUSY.equals(strategy)
		        || ProviderAssignmentStrategy.ROUND_ROBIN.equals(strategy)
		        || ProviderAssignmentStrategy.RANDOM.equals(strategy) || ProviderAssignmentStrategy.MANUAL.equals(strategy);
	}
}
