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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Provider;

/**
 * Unit test for RoundRobinProviderStrategy Tests the round-robin provider assignment logic
 * including rotation, state management, and handling of edge cases.
 */
public class RoundRobinProviderStrategyTest {
	
	private RoundRobinProviderStrategy strategy;
	
	private Location testLocation;
	
	private Provider provider1;
	
	private Provider provider2;
	
	private Provider provider3;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		strategy = new RoundRobinProviderStrategy();
		
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
	public void assignProvider_shouldThrowException_whenLocationUuidIsNull() {
		// Mock a Location with null UUID (OpenMRS Location auto-generates UUID, so we need to mock it)
		Location nullLocation = mock(Location.class);
		when(nullLocation.getUuid()).thenReturn(null);
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		try {
			strategy.assignProvider(nullLocation, providers);
			fail("Should throw IllegalArgumentException when location UUID is null");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Location with non-null UUID required", e.getMessage());
		}
	}
	
	@Test
	public void assignProvider_shouldThrowException_whenLocationIsNull() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		try {
			strategy.assignProvider(null, providers);
			fail("Should throw IllegalArgumentException when location is null");
		}
		catch (IllegalArgumentException e) {
			assertEquals("Location with non-null UUID required", e.getMessage());
		}
	}
	
	@Test
	public void assignProvider_shouldRotateProvidersInOrder() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		
		Provider first = strategy.assignProvider(testLocation, providers);
		Provider second = strategy.assignProvider(testLocation, providers);
		Provider third = strategy.assignProvider(testLocation, providers);
		Provider fourth = strategy.assignProvider(testLocation, providers);
		
		assertEquals("First assignment should be provider1", provider1, first);
		assertEquals("Second assignment should be provider2", provider2, second);
		assertEquals("Third assignment should be provider3", provider3, third);
		assertEquals("Fourth assignment should cycle back to provider1", provider1, fourth);
	}
	
	@Test
	public void assignProvider_shouldMaintainSeparateStateForDifferentLocations() {
		Location location1 = new Location();
		location1.setUuid("location-1");
		
		Location location2 = new Location();
		location2.setUuid("location-2");
		
		List<Provider> providers1 = Arrays.asList(provider1, provider2);
		List<Provider> providers2 = Arrays.asList(provider1, provider3);
		
		Provider result1 = strategy.assignProvider(location1, providers1);
		Provider result2 = strategy.assignProvider(location2, providers2);
		Provider result1Second = strategy.assignProvider(location1, providers1);
		
		assertEquals("Location1 first assignment should be provider1", provider1, result1);
		assertEquals("Location2 first assignment should be provider1", provider1, result2);
		assertEquals("Location1 second assignment should be provider2", provider2, result1Second);
	}
	
	@Test
	public void assignProvider_shouldResetState_whenProviderListChanges() {
		List<Provider> providers1 = Arrays.asList(provider1, provider2);
		List<Provider> providers2 = Arrays.asList(provider2, provider3);
		
		// Assign with first list
		Provider first = strategy.assignProvider(testLocation, providers1);
		assertEquals("First assignment with list1 should be provider1", provider1, first);
		
		// Change provider list - should reset state
		Provider afterChange = strategy.assignProvider(testLocation, providers2);
		assertEquals("After list change, should start from provider2", provider2, afterChange);
	}
	
	@Test
	public void assignProvider_shouldHandleProviderListWithSameProvidersDifferentOrder() {
		// Create new provider instances with same UUIDs
		Provider provider1Copy = createProvider("provider-uuid-1", "Provider One Copy");
		Provider provider2Copy = createProvider("provider-uuid-2", "Provider Two Copy");
		
		List<Provider> providers1 = Arrays.asList(provider1, provider2);
		List<Provider> providers2 = Arrays.asList(provider1Copy, provider2Copy);
		
		Provider first = strategy.assignProvider(testLocation, providers1);
		Provider second = strategy.assignProvider(testLocation, providers2);
		
		assertEquals("First assignment should be provider1", provider1, first);
		assertEquals("Second assignment should rotate to provider2 (state maintained across list instances)", provider2Copy,
		    second);
	}
	
	@Test
	public void assignProvider_shouldHandleNullProvidersInList() {
		Provider nullProvider1 = createProvider(null, "Null UUID 1");
		Provider nullProvider2 = createProvider(null, "Null UUID 2");
		
		List<Provider> providers = Arrays.asList(provider1, nullProvider1, provider2, nullProvider2);
		
		Provider first = strategy.assignProvider(testLocation, providers);
		Provider second = strategy.assignProvider(testLocation, providers);
		Provider third = strategy.assignProvider(testLocation, providers);
		Provider fourth = strategy.assignProvider(testLocation, providers);
		
		assertEquals("First assignment should be provider1", provider1, first);
		assertEquals("Second assignment should be null UUID provider 1", nullProvider1, second);
		assertEquals("Third assignment should be provider2", provider2, third);
		assertEquals("Fourth assignment should be null UUID provider 2", nullProvider2, fourth);
	}
	
	@Test
	public void assignProvider_shouldHandleAllProvidersWithNullUuids() {
		Provider nullProvider1 = createProvider(null, "Null UUID 1");
		Provider nullProvider2 = createProvider(null, "Null UUID 2");
		
		List<Provider> providers = Arrays.asList(nullProvider1, nullProvider2);
		
		Provider first = strategy.assignProvider(testLocation, providers);
		Provider second = strategy.assignProvider(testLocation, providers);
		Provider third = strategy.assignProvider(testLocation, providers);
		
		assertEquals("First assignment should be first null UUID provider", nullProvider1, first);
		assertEquals("Second assignment should be second null UUID provider", nullProvider2, second);
		assertEquals("Third assignment should cycle back", nullProvider1, third);
	}
	
	@Test
	public void getName_shouldReturnCorrectStrategyName() {
		assertEquals("Strategy name should be 'roundRobin'", "roundRobin", strategy.getName());
	}
	
	/**
	 * Test concurrent assignment to verify thread safety
	 */
	@Test
	public void assignProvider_shouldBeThreadSafe() throws InterruptedException {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);

		// Create multiple threads that will assign concurrently
		Thread[] threads = new Thread[10];
		final Provider[] results = new Provider[10];

		for (int i = 0; i < threads.length; i++) {
			final int index = i;
			threads[i] = new Thread(() -> {
				results[index] = strategy.assignProvider(testLocation, providers);
			});
		}

		// Start all threads
		for (Thread thread : threads) {
			thread.start();
		}

		// Wait for all threads to complete
		for (Thread thread : threads) {
			thread.join();
		}

		// Verify all assignments are valid providers
		for (Provider result : results) {
			assertNotNull("Result should not be null", result);
			assertTrue("Result should be one of the test providers",
				providers.contains(result));
		}

		// Verify rotation occurred (not all same provider)
		boolean hasRotation = false;
		Provider firstResult = results[0];
		for (Provider result : results) {
			if (!firstResult.equals(result)) {
				hasRotation = true;
				break;
			}
		}
		assertTrue("Should have rotation across concurrent assignments", hasRotation);
	}
	
	/**
	 * Test that state is correctly maintained when location has providers removed
	 */
	@Test
	public void assignProvider_shouldHandleProviderListReduction() {
		List<Provider> threeProviders = Arrays.asList(provider1, provider2, provider3);
		List<Provider> twoProviders = Arrays.asList(provider1, provider2);
		
		// Assign with 3 providers
		Provider first = strategy.assignProvider(testLocation, threeProviders);
		Provider second = strategy.assignProvider(testLocation, threeProviders);
		
		// Reduce to 2 providers (should reset state)
		Provider afterReduction = strategy.assignProvider(testLocation, twoProviders);
		
		assertEquals("First assignment should be provider1", provider1, first);
		assertEquals("Second assignment should be provider2", provider2, second);
		assertEquals("After reduction, should reset to first provider", provider1, afterReduction);
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
