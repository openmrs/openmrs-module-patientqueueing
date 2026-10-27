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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		// One query for the whole location/day, then count per provider in memory —
		// counting per provider inside the comparator would issue O(n log n) queries
		// and risks "Comparison method violates its general contract" if counts
		// shift mid-sort
		Map<String, Integer> activeQueueCounts = countActiveQueuesByProvider(location, new Date());

		List<Provider> sortedProviders = new ArrayList<>(availableProviders);
		sortedProviders.sort(Comparator.comparingInt(p -> activeQueueCounts.getOrDefault(p.getUuid(), 0)));
		return sortedProviders.get(0);
	}
	
	@Override
	public String getName() {
		return "leastBusy";
	}
	
	/**
	 * Count each provider's active (non-completed) queues at a location for a single day, with one
	 * query for all providers.
	 * 
	 * @param location the location
	 * @param today the date to query
	 * @return map of provider UUID to active queue count (providers with no queues are absent)
	 */
	private Map<String, Integer> countActiveQueuesByProvider(Location location, Date today) {
		List<PatientQueue> queues = queueingService.getPatientQueueList(null,
		    org.openmrs.util.OpenmrsUtil.firstSecondOfDay(today), org.openmrs.util.OpenmrsUtil.getLastMomentOfDay(today),
		    location, null, null, null, null);

		Map<String, Integer> counts = new HashMap<>();
		for (PatientQueue queue : queues) {
			// Terminal statuses must not keep inflating a provider's busy count for the
			// rest of the day (TRANSFERRED entries are never completed)
			if (queue.getProvider() == null || queue.getStatus() == PatientQueue.Status.COMPLETED
			        || queue.getStatus() == PatientQueue.Status.TRANSFERRED) {
				continue;
			}
			counts.merge(queue.getProvider().getUuid(), 1, Integer::sum);
		}
		return counts;
	}
}
