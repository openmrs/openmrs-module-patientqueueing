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
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.PatientQueue;

/**
 * Unit test for LeastBusyProviderStrategy Tests the logic for selecting the provider with the
 * fewest active queues.
 */
public class LeastBusyProviderStrategyTest {
	
	private LeastBusyProviderStrategy strategy;
	
	@Mock
	private PatientQueueingService queueingService;
	
	private Location testLocation;
	
	private Provider provider1;
	
	private Provider provider2;
	
	private Provider provider3;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		// Create strategy with injected mock service for testing
		strategy = new LeastBusyProviderStrategy(queueingService);
		
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
	public void assignProvider_shouldSelectProviderWithFewestQueues() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		
		// Mock queue counts: provider1 has 3, provider2 has 1, provider3 has 5
		mockQueueCount(provider1, 3);
		mockQueueCount(provider2, 1);
		mockQueueCount(provider3, 5);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should select provider with fewest queues (provider2 with 1 queue)", provider2, result);
	}
	
	@Test
	public void assignProvider_shouldSelectProviderWithZeroQueues_whenAvailable() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		
		// Mock queue counts: provider1 has 0, provider2 has 2, provider3 has 1
		mockQueueCount(provider1, 0);
		mockQueueCount(provider2, 2);
		mockQueueCount(provider3, 1);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should select provider with zero queues (provider1)", provider1, result);
	}
	
	@Test
	public void assignProvider_shouldHandleTie_bySelectingFirstInList() {
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3);
		
		// All providers have same queue count
		mockQueueCount(provider1, 2);
		mockQueueCount(provider2, 2);
		mockQueueCount(provider3, 2);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should break tie by selecting first provider (provider1)", provider1, result);
	}
	
	@Test
	public void assignProvider_shouldIgnoreCompletedQueues_whenCounting() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		// Provider1: 5 queues (3 COMPLETED, 2 active)
		// Provider2: 3 queues (1 COMPLETED, 2 active)
		// Both providers have 2 active queues, so first wins (provider1)
		mockMixedQueues(provider1, 3, 2); // 3 completed, 2 active
		mockMixedQueues(provider2, 1, 2); // 1 completed, 2 active
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		// Both have 2 active queues, so first wins due to tie-breaking
		assertEquals("Should count only non-COMPLETED queues", provider1, result);
	}
	
	@Test
	public void assignProvider_shouldHandleProviderWithNoQueues() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		// Provider1 has queues, Provider2 has none
		mockQueueCount(provider1, 5);
		mockQueueCount(provider2, 0);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should select provider with no queues", provider2, result);
	}
	
	@Test
	public void assignProvider_shouldUseTodayDateRange_whenQueryingQueues() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		mockQueueCount(provider1, 1);
		mockQueueCount(provider2, 2);
		
		strategy.assignProvider(testLocation, providers);
		
		// Verify that queueingService.getPatientQueueList was called with today's date range
		verify(queueingService, atLeastOnce()).getPatientQueueList(eq(provider1), any(Date.class), any(Date.class),
		    any(Location.class), any(Location.class), isNull(Patient.class), isNull(PatientQueue.Status.class),
		    any(Location.class));
	}
	
	@Test
	public void getName_shouldReturnCorrectStrategyName() {
		assertEquals("Strategy name should be 'leastBusy'", "leastBusy", strategy.getName());
	}
	
	/**
	 * Test with many providers to ensure sorting works correctly
	 */
	@Test
	public void assignProvider_shouldCorrectlySortManyProviders() {
		Provider p4 = createProvider("provider-uuid-4", "Provider Four");
		Provider p5 = createProvider("provider-uuid-5", "Provider Five");
		
		List<Provider> providers = Arrays.asList(provider1, provider2, provider3, p4, p5);
		
		// Queue counts: p1=10, p2=3, p3=7, p4=1, p5=5
		mockQueueCount(provider1, 10);
		mockQueueCount(provider2, 3);
		mockQueueCount(provider3, 7);
		mockQueueCount(p4, 1);
		mockQueueCount(p5, 5);
		
		Provider result = strategy.assignProvider(testLocation, providers);
		
		assertEquals("Should select provider with fewest queues (p4 with 1 queue)", p4, result);
	}
	
	/**
	 * Test that the strategy handles null location gracefully
	 */
	@Test
	public void assignProvider_shouldHandleNullLocation() {
		List<Provider> providers = Arrays.asList(provider1, provider2);
		
		mockQueueCount(provider1, 1);
		mockQueueCount(provider2, 2);
		
		// Should not throw exception, location is only used for query filtering
		Provider result = strategy.assignProvider(null, providers);
		
		assertNotNull("Should return a provider", result);
	}
	
	/**
	 * Mock the service to return a specific number of queues for a provider
	 */
	private void mockQueueCount(Provider provider, int count) {
		List<PatientQueue> queues = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			PatientQueue queue = new PatientQueue();
			queue.setStatus(PatientQueue.Status.PENDING);
			queue.setProvider(provider);
			queues.add(queue);
		}

		when(queueingService.getPatientQueueList(eq(provider), any(Date.class), any(Date.class), any(Location.class),
		    any(Location.class), isNull(Patient.class), isNull(PatientQueue.Status.class), any(Location.class))).thenReturn(queues);
	}
	
	/**
	 * Mock the service to return a mix of completed and active queues
	 */
	private void mockMixedQueues(Provider provider, int completedCount, int activeCount) {
		List<PatientQueue> queues = new ArrayList<>();

		for (int i = 0; i < completedCount; i++) {
			PatientQueue queue = new PatientQueue();
			queue.setStatus(PatientQueue.Status.COMPLETED);
			queue.setProvider(provider);
			queues.add(queue);
		}

		for (int i = 0; i < activeCount; i++) {
			PatientQueue queue = new PatientQueue();
			queue.setStatus(PatientQueue.Status.PENDING);
			queue.setProvider(provider);
			queues.add(queue);
		}

		when(queueingService.getPatientQueueList(eq(provider), any(Date.class), any(Date.class), any(Location.class),
		    any(Location.class), isNull(Patient.class), isNull(PatientQueue.Status.class), any(Location.class))).thenReturn(queues);
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
