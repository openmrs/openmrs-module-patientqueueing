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
 * Strategy interface for assigning providers to locations
 */
public interface ProviderAssignmentStrategy {
	
	/**
	 * Assign a provider to a location
	 * 
	 * @param location the location to assign a provider to
	 * @param availableProviders the list of available providers
	 * @return the assigned provider, or null if no providers are available
	 */
	Provider assignProvider(Location location, List<Provider> availableProviders);
	
	/**
	 * Get the name of this strategy
	 * 
	 * @return the strategy name
	 */
	String getName();
}
