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

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.patientqueueing.api.providerAssignment.ProviderAssignmentStrategy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for assigning providers to locations using various strategies
 */
public interface ProviderAssignmentService extends OpenmrsService {
	
	/**
	 * Assign a provider to a location using the configured strategy
	 * 
	 * @param location the location to assign a provider to
	 * @return the assigned provider
	 * @throws APIException if no providers are available
	 */
	@Transactional(readOnly = true)
	Provider assignProvider(Location location);
	
	/**
	 * Assign a provider from a specific list
	 * 
	 * @param location the location
	 * @param availableProviders the list of providers to choose from
	 * @return the assigned provider
	 * @throws APIException if no providers are available
	 */
	@Transactional(readOnly = true)
	Provider assignProvider(Location location, List<Provider> availableProviders);
	
	/**
	 * Get the current provider assignment strategy
	 * 
	 * @return the provider assignment strategy
	 */
	@Transactional(readOnly = true)
	ProviderAssignmentStrategy getProviderAssignmentStrategy();
	
	/**
	 * Set the provider assignment strategy
	 * 
	 * @param strategy the strategy to use
	 */
	void setProviderAssignmentStrategy(ProviderAssignmentStrategy strategy);
	
	/**
	 * Get all providers assigned to a location
	 * 
	 * @param location the location
	 * @return list of providers
	 */
	@Transactional(readOnly = true)
	List<Provider> getProvidersForLocation(Location location);
	
	/**
	 * Get the count of active patient queues for each provider at a location
	 * 
	 * @param location the location
	 * @return map of provider to queue count
	 */
	@Transactional(readOnly = true)
	java.util.Map<Provider, Integer> getProviderQueueCounts(Location location);
}
