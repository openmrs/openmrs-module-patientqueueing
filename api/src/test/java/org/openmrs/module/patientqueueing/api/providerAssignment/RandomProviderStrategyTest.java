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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Provider;

/**
 * Unit test for RandomProviderStrategy Tests the random provider assignment logic and distribution.
 */
public class RandomProviderStrategyTest {
	
	private RandomProviderStrategy strategy;
	
	private Location testLocation;
	
	private Provider provider1;
	
	private Provider provider2;
	
	private Provider provider3;
	
	@Before
	public void setUp() {
		strategy = new RandomProviderStrategy();
		
		// Create test location
		testLocation = new Location();
		testLocation.setUuid("location-uuid-123");
		
		// Create test providers
		provider1 = createProvider("provider-uuid-1", "Provider One");
		provider2 = createProvider("provider-uuid-2", "Provider Two");
		provider3 = createProvider("provider-uuid-3", "Provider Three");
	}
	
	@Test
	public void assignProvider_shouldReturnNull_whenProviderListIsNull() {
		Provider result = strategy.assignProvider(testLocation, null);
		assertNull("Should return null when provider list is null", result);
	}
	
	@Test
	public void assignProvider_shouldReturnNull_whenProviderListIsEmpty() {
		List<Provider> emptyList = new ArrayList<>();
		Provider result = strategy.assignProvider(testLocation, emptyList);
		assertNull("Should return null when provider list is empty", result);
	}
	
	@Test
	public void assignProvider_shouldReturnSingleProvider_whenOnlyOneAvailable() {
		List<Provider> providers = Collections.singletonList(provider1);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should return the only available provider", provider1, result);
	}
	
	@Test
	public void assignProvider_shouldReturnProviderFromList_whenMultipleAvailable() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertNotNull("Should return a provider", result);
		assertTrue("Returned provider should be in the list", providers.contains(result));
	}
	
	@Test
	public void assignProvider_shouldHandleNullLocation_gracefully() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		// Should not throw exception
		Provider result = strategy.assignProvider(null, providers);
		
		assertNotNull("Should return a provider even with null location", result);
		assertTrue("Returned provider should be in the list", providers.contains(result));
	}
	
	@Test
	public void assignProvider_shouldDistributeAssignmentsAcrossMultipleProviders() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		int iterations = 100;

		// Count how many times each provider is selected
		Map<Provider, Integer> selectionCount = new HashMap<>();
		selectionCount.put(provider1, 0);
		selectionCount.put(provider2, 0);
		selectionCount.put(provider3, 0);

		for (int i = 0; i < iterations; i++) {
			Provider result = strategy.assignProvider(testLocation, providers);
			selectionCount.put(result, selectionCount.get(result) + 1);
		}

		// Each provider should be selected roughly 1/3 of the time
		// With 100 iterations and 3 providers, expected ~33 selections each
		// Allow for variance: at least 10 selections each (10% margin)
		int minExpected = iterations / providers.size() / 3; // Very permissive threshold
		for (Map.Entry<Provider, Integer> entry : selectionCount.entrySet()) {
			assertTrue("Provider " + entry.getKey().getName() + " should be selected at least " +
				minExpected + " times, got " + entry.getValue(),
				entry.getValue() >= minExpected);
		}

		// Verify all selections sum to iterations
		int totalSelections = selectionCount.values().stream().mapToInt(Integer::intValue).sum();
		assertEquals("Total selections should equal iterations", iterations, totalSelections);
	}
	
	@Test
	public void assignProvider_shouldHandleTwoProviders() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		int iterations = 50;

		Map<Provider, Integer> selectionCount = new HashMap<>();
		selectionCount.put(provider1, 0);
		selectionCount.put(provider2, 0);

		for (int i = 0; i < iterations; i++) {
			Provider result = strategy.assignProvider(testLocation, providers);
			selectionCount.put(result, selectionCount.get(result) + 1);
		}

		// Both should be selected
		assertTrue("Provider1 should be selected", selectionCount.get(provider1) > 0);
		assertTrue("Provider2 should be selected", selectionCount.get(provider2) > 0);

		int totalSelections = selectionCount.values().stream().mapToInt(Integer::intValue).sum();
		assertEquals("Total selections should equal iterations", iterations, totalSelections);
	}
	
	@Test
	public void assignProvider_shouldHandleLargeProviderList() {
		// Create a list of 10 providers
		List<Provider> providers = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			providers.add(createProvider("provider-uuid-" + i, "Provider " + i));
		}

		int iterations = 100;

		// Count selections
		Map<String, Integer> selectionCount = new HashMap<>();
		for (Provider p : providers) {
			selectionCount.put(p.getUuid(), 0);
		}

		for (int i = 0; i < iterations; i++) {
			Provider result = strategy.assignProvider(testLocation, providers);
			selectionCount.put(result.getUuid(), selectionCount.get(result.getUuid()) + 1);
		}

		// At least 50% of providers should be selected at least once
		long providersSelected = selectionCount.values().stream().filter(count -> count > 0).count();
		assertTrue("At least half of providers should be selected",
			providersSelected >= providers.size() / 2);
	}
	
	@Test
	public void assignProvider_shouldBeIndependentOfLocation() {
		Location location1 = new Location();
		location1.setUuid("location-1");
		
		Location location2 = new Location();
		location2.setUuid("location-2");
		
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		Provider result1 = strategy.assignProvider(location1, providers);
		Provider result2 = strategy.assignProvider(location2, providers);
		
		// Both should return valid providers (not null)
		assertNotNull("Result for location1 should not be null", result1);
		assertNotNull("Result for location2 should not be null", result2);
		assertTrue("Result1 should be in provider list", providers.contains(result1));
		assertTrue("Result2 should be in provider list", providers.contains(result2));
	}
	
	@Test
	public void assignProvider_shouldHandleProviderWithNullUuid() {
		Provider nullUuidProvider = createProvider(null, "Null UUID Provider");
		List<Provider> providers = Arrays.asList(provider1, nullUuidProvider, provider2);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertNotNull("Should return a provider", result);
		assertTrue("Returned provider should be in the list", providers.contains(result));
	}
	
	@Test
	public void assignProvider_shouldHandleAllProvidersWithNullUuids() {
		Provider nullProvider1 = createProvider(null, "Null UUID 1");
		Provider nullProvider2 = createProvider(null, "Null UUID 2");
		
		List<Provider> providers = Arrays.asList(nullProvider1, nullProvider2);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertNotNull("Should return a provider", result);
		assertTrue("Returned provider should be in the list", providers.contains(result));
	}
	
	@Test
	public void getName_shouldReturnCorrectStrategyName() {
		assertEquals("Strategy name should be 'random'", "random", strategy.getName());
	}
	
	@Test
	public void assignProvider_shouldBeRandom_inStatisticalSense() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		int iterations = 300;
		
		// Track selection order
		StringBuilder selectionOrder = new StringBuilder();
		
		for (int i = 0; i < iterations; i++) {
			Provider result = strategy.assignProvider(testLocation, providers);
			// Append provider index to track pattern
			if (result.equals(provider1)) {
				selectionOrder.append("1");
			} else if (result.equals(provider2)) {
				selectionOrder.append("2");
			} else {
				selectionOrder.append("3");
			}
		}
		
		// Check that we don't have a repeating pattern
		// A pattern like "123123123..." would indicate non-randomness
		String order = selectionOrder.toString();
		assertFalse("Should not have simple repeating pattern of period 3", hasRepeatingPattern(order, "123"));
		assertFalse("Should not have simple repeating pattern of period 2", hasRepeatingPattern(order, "12"));
	}
	
	/**
	 * Helper to check for repeating patterns
	 */
	private boolean hasRepeatingPattern(String str, String pattern) {
		// Check if string consists mostly of the pattern repeated
		int patternCount = str.length() / pattern.length();
		int matches = 0;
		for (int i = 0; i < patternCount; i++) {
			if (str.startsWith(pattern, i * pattern.length())) {
				matches++;
			}
		}
		return matches >= str.length() / pattern.length() * 0.8; // 80% threshold
	}
	
	/**
	 * Helper method to create a test provider
	 */
	private Provider createProvider(String uuid, String name) {
		Provider provider = new Provider();
		provider.setUuid(uuid);
		provider.setName(name);
		return provider;
	}
}
