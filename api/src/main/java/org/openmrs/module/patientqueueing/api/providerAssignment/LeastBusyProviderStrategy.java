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
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Assigns the provider with the fewest active patient queues
 */
public class LeastBusyProviderStrategy implements ProviderAssignmentStrategy {
	
	private final PatientQueueingService queueingService;
	
	public LeastBusyProviderStrategy() {
		this(Context.getService(PatientQueueingService.class));
	}
	
	/**
	 * Constructor for dependency injection (useful for testing)
	 * 
	 * @param queueingService the patient queueing service
	 */
	public LeastBusyProviderStrategy(PatientQueueingService queueingService) {
		this.queueingService = queueingService;
	}
	
	@Override
	public Provider assignProvider(Location location, List<Provider> availableProviders) {
		if (availableProviders == null || availableProviders.isEmpty()) {
			return null;
		}
		
		if (availableProviders.size() == 1) {
			return availableProviders.get(0);
		}
		
		// Sort providers by their current queue count (ascending)
		final Location finalLocation = location;
		final Date today = new Date();
		List<Provider> sortedProviders = new ArrayList<>(availableProviders);
		sortedProviders.sort(Comparator.comparingInt(p -> getActiveQueueCount(finalLocation, p, today)));
		
		return sortedProviders.get(0);
	}
	
	@Override
	public String getName() {
		return "leastBusy";
	}
	
	/**
	 * Get the count of active queues for a provider at a location
	 * 
	 * @param location the location
	 * @param provider the provider
	 * @param today the date to use for the query (ensures all comparisons use the same time
	 *            reference)
	 * @return the number of active queues
	 */
	private int getActiveQueueCount(Location location, Provider provider, Date today) {
		List<PatientQueue> queues = queueingService.getPatientQueueList(provider,
		    org.openmrs.util.OpenmrsUtil.firstSecondOfDay(today), org.openmrs.util.OpenmrsUtil.getLastMomentOfDay(today),
		    location, null, null, null, null);
		
		int count = 0;
		for (PatientQueue queue : queues) {
			if (queue.getStatus() != PatientQueue.Status.COMPLETED) {
				count++;
			}
		}
		
		return count;
	}
}
