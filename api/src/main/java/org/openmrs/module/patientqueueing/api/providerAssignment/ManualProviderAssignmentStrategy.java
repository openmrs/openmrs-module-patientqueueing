/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.api.providerAssignment;

import org.openmrs.Location;
import org.openmrs.Provider;

import java.util.List;

/**
 * Manual provider assignment strategy.
 * <p>
 * This strategy indicates that provider assignment is done manually by users, not automatically by
 * the system. It always declines to pick by returning null, which callers treat as "queue the
 * patient unassigned" rather than failing the check-in.
 */
public class ManualProviderAssignmentStrategy implements ProviderAssignmentStrategy {
	
	@Override
	public Provider assignProvider(Location location, List<Provider> availableProviders) {
		// Manual means providers are assigned by hand through the provider dashboard later
		return null;
	}
	
	@Override
	public String getName() {
		return "manual";
	}
}
