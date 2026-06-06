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
import java.util.Objects;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assigns providers in a round-robin fashion
 */
public class RoundRobinProviderStrategy implements ProviderAssignmentStrategy {
	
	private final Map<String, RoundRobinState> stateMap = new ConcurrentHashMap<String, RoundRobinState>();
	
	private final Object lock = new Object();
	
	@Override
	public Provider assignProvider(Location location, List<Provider> availableProviders) {
		if (availableProviders == null || availableProviders.isEmpty()) {
			return null;
		}
		
		if (availableProviders.size() == 1) {
			return availableProviders.get(0);
		}
		
		if (location == null || location.getUuid() == null) {
			throw new IllegalArgumentException("Location with non-null UUID required");
		}
		
		String locationKey = location.getUuid();
		RoundRobinState state;
		
		synchronized (lock) {
			state = stateMap.get(locationKey);
			if (state == null || !isSameProviderList(state.getProviderList(), availableProviders)) {
				state = new RoundRobinState(availableProviders);
				stateMap.put(locationKey, state);
			}
		}
		
		return state.getAndIncrement();
	}
	
	@Override
	public String getName() {
		return "roundRobin";
	}
	
	/**
	 * Check if two provider lists contain the same providers (by UUID)
	 */
	private boolean isSameProviderList(List<Provider> list1, List<Provider> list2) {
		if (list1 == list2) {
			return true;
		}
		if (list1 == null || list2 == null || list1.size() != list2.size()) {
			return false;
		}
		// Compare by UUID to handle different list instances with same contents
		for (int i = 0; i < list1.size(); i++) {
			Provider p1 = list1.get(i);
			Provider p2 = list2.get(i);
			String uuid1 = p1 != null ? p1.getUuid() : null;
			String uuid2 = p2 != null ? p2.getUuid() : null;
			if (!Objects.equals(uuid1, uuid2)) {
				return false;
			}
		}
		return true;
	}
	
	private static class RoundRobinState {
		
		private final List<Provider> providerList;
		
		private int currentIndex = 0;
		
		public RoundRobinState(List<Provider> providerList) {
			this.providerList = providerList;
		}
		
		public synchronized Provider getAndIncrement() {
			Provider provider = providerList.get(currentIndex);
			currentIndex = (currentIndex + 1) % providerList.size();
			return provider;
		}
		
		public List<Provider> getProviderList() {
			return providerList;
		}
	}
}
