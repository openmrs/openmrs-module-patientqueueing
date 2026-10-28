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
import java.util.Random;

/**
 * Assigns providers randomly
 */
public class RandomProviderStrategy implements ProviderAssignmentStrategy {
	
	private final Random random = new Random();
	
	@Override
	public Provider assignProvider(Location location, List<Provider> availableProviders) {
		if (availableProviders == null || availableProviders.isEmpty()) {
			return null;
		}
		
		if (availableProviders.size() == 1) {
			return availableProviders.get(0);
		}
		
		int index = random.nextInt(availableProviders.size());
		return availableProviders.get(index);
	}
	
	@Override
	public String getName() {
		return "random";
	}
}
