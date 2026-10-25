/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.api.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.patientqueueing.PatientQueueingConfig;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.api.ProviderAssignmentService;
import org.openmrs.module.patientqueueing.api.providerAssignment.LeastBusyProviderStrategy;
import org.openmrs.module.patientqueueing.api.providerAssignment.ProviderAssignmentStrategy;
import org.openmrs.module.patientqueueing.api.providerAssignment.ManualProviderAssignmentStrategy;
import org.openmrs.module.patientqueueing.api.providerAssignment.RandomProviderStrategy;
import org.openmrs.module.patientqueueing.api.providerAssignment.RoundRobinProviderStrategy;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderAssignmentServiceImpl extends BaseOpenmrsService implements ProviderAssignmentService {
	
	private static final Log log = LogFactory.getLog(ProviderAssignmentServiceImpl.class);
	
	private static final String USER_DEFAULT_LOCATION_PROPERTY = "defaultLocation";
	
	private ProviderAssignmentStrategy strategy;
	
	@Override
	public Provider assignProvider(Location location) {
		List<Provider> providers = getProvidersForLocation(location);
		
		if (providers.isEmpty()) {
			throw new APIException("No providers available for location: " + location.getName());
		}
		
		return assignProvider(location, providers);
	}
	
	@Override
	public Provider assignProvider(Location location, List<Provider> availableProviders) {
		if (availableProviders == null || availableProviders.isEmpty()) {
			throw new APIException("No providers available for assignment");
		}
		
		ProviderAssignmentStrategy currentStrategy = getProviderAssignmentStrategy();
		// Strategy is always non-null after getProviderAssignmentStrategy()
		
		Provider provider = currentStrategy.assignProvider(location, availableProviders);
		
		if (provider == null) {
			throw new APIException("Provider assignment strategy returned null");
		}
		
		return provider;
	}
	
	@Override
	public ProviderAssignmentStrategy getProviderAssignmentStrategy() {
		// NOTE: Strategy is cached for the lifetime of the module context.
		// Changes to the GP_PROVIDER_ASSIGNMENT_STRATEGY global property require
		// a module restart to take effect.
		if (strategy != null) {
			return strategy;
		}
		
		AdministrationService adminService = Context.getAdministrationService();
		String strategyName = adminService.getGlobalProperty(PatientQueueingConfig.GP_PROVIDER_ASSIGNMENT_STRATEGY,
		    PatientQueueingConfig.DEFAULT_PROVIDER_ASSIGNMENT_STRATEGY);
		
		strategy = createStrategy(strategyName);
		
		return strategy;
	}
	
	@Override
	public void setProviderAssignmentStrategy(ProviderAssignmentStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public List<Provider> getProvidersForLocation(Location location) {
		List<Provider> providers = new ArrayList<>();

		if (location == null) {
			log.warn("Location is null, returning empty provider list");
			return providers;
		}

		AdministrationService adminService = Context.getAdministrationService();
		String locationAttributeTypeUuid = adminService.getGlobalProperty(
		    PatientQueueingConfig.GP_PROVIDER_LOCATION_ATTRIBUTE_TYPE_UUID);

		// Pre-fetch the attribute type if configured (performance optimization)
		org.openmrs.ProviderAttributeType attributeType = null;
		if (StringUtils.isNotBlank(locationAttributeTypeUuid)) {
			try {
				attributeType = Context.getProviderService().getProviderAttributeTypeByUuid(locationAttributeTypeUuid);
				if (attributeType == null) {
					log.warn("Provider attribute type configured but not found: " + locationAttributeTypeUuid
					        + ". Falling back to user default location approach.");
				}
			} catch (Exception e) {
				log.error("Error fetching provider attribute type: " + e.getMessage(), e);
			}
		}

		// Get the configured person location attribute name for fallback approach
		String personLocationAttrName = adminService.getGlobalProperty(
		    PatientQueueingConfig.GP_PERSON_LOCATION_ATTRIBUTE_NAME, "defaultLocation");

		for (Provider provider : Context.getProviderService().getAllProviders(false)) {
			if (provider.getPerson() == null) {
				continue;
			}

			// Approach 1: Use provider attribute if configured, otherwise use user default location
			boolean providerMatchesLocation = attributeType != null
			        ? isProviderAtLocationViaAttribute(provider, location, attributeType)
			        : isProviderAtLocationViaUserDefaultLocation(provider, location, personLocationAttrName);

			if (providerMatchesLocation) {
				providers.add(provider);
			}
		}

		// Log if no providers found for location (helps with configuration issues)
		if (providers.isEmpty() && log.isDebugEnabled()) {
			log.debug("No providers found for location: " + location.getName() + " (" + location.getUuid() + ")");
		}

		return providers;
	}
	
	/**
	 * Check if a provider is assigned to a location via a provider attribute.
	 * 
	 * @param provider the provider to check
	 * @param location the location to match
	 * @param attributeType the pre-fetched attribute type (never null when this method is called)
	 * @return true if the provider has an attribute of the given type with a value matching the
	 *         location
	 */
	private boolean isProviderAtLocationViaAttribute(Provider provider, Location location,
	        org.openmrs.ProviderAttributeType attributeType) {
		try {
			for (org.openmrs.ProviderAttribute attribute : provider.getActiveAttributes()) {
				if (attribute == null || attribute.getAttributeType() == null) {
					continue;
				}
				if (!attribute.getAttributeType().getId().equals(attributeType.getId())) {
					continue;
				}
				if (attribute.getValue() == null) {
					continue;
				}
				return matchesLocationUuid(attribute.getValue().toString(), location);
			}
			return false;
		}
		catch (Exception e) {
			log.error("Error checking provider location via attribute: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Check if a provider is assigned to a location via their associated user's default location.
	 * <p>
	 * This method finds the User associated with the provider's Person and checks if the user has a
	 * default location that matches the given location. The default location can be configured via:
	 * <ul>
	 * <li>User properties (keyed by {@link #USER_DEFAULT_LOCATION_PROPERTY})</li>
	 * <li>Person attributes (attribute type name configurable via
	 * GP_PERSON_LOCATION_ATTRIBUTE_NAME)</li>
	 * </ul>
	 * 
	 * @param provider the provider to check
	 * @param location the location to match
	 * @param personLocationAttrName the person attribute type name to check for location
	 *            assignments
	 * @return true if the provider's associated user has a default location matching the given
	 *         location
	 */
	private boolean isProviderAtLocationViaUserDefaultLocation(Provider provider, Location location,
	        String personLocationAttrName) {
		try {
			List<org.openmrs.User> users = Context.getUserService().getUsersByPerson(provider.getPerson(), false);
			
			if (users == null || users.isEmpty()) {
				return false;
			}
			
			// Check if any of the user's default locations match
			for (org.openmrs.User user : users) {
				// Check user properties for default location (stored as location UUID)
				Map<String, String> userProperties = user.getUserProperties();
				if (userProperties != null) {
					String defaultLocationUuid = userProperties.get(USER_DEFAULT_LOCATION_PROPERTY);
					if (matchesLocationUuid(defaultLocationUuid, location)) {
						return true;
					}
				}
				
				// Alternative: Check if the person has a location attribute
				if (provider.getPerson().getAttributes() != null) {
					org.openmrs.PersonAttribute locationAttr = provider.getPerson().getAttribute(personLocationAttrName);
					if (locationAttr != null && locationAttr.getValue() != null) {
						if (matchesLocationUuid(locationAttr.getValue().toString(), location)) {
							return true;
						}
					}
				}
			}
			
			return false;
			
		}
		catch (Exception e) {
			log.error("Error checking provider location via user default location: " + e.getMessage(), e);
			return false;
		}
	}
	
	/**
	 * Check if a trimmed string value matches the location's UUID.
	 * 
	 * @param value the string value to check (may be null)
	 * @param location the location to match against
	 * @return true if value is not blank and equals the location's UUID
	 */
	private boolean matchesLocationUuid(String value, Location location) {
		return StringUtils.isNotBlank(value) && location.getUuid().equals(value.trim());
	}
	
	@Override
	public Map<Provider, Integer> getProviderQueueCounts(Location location) {
		PatientQueueingService queueingService = Context.getService(PatientQueueingService.class);
		Map<Provider, Integer> counts = new HashMap<>();

		Date today = new Date();
		List<Provider> providers = getProvidersForLocation(location);

		for (Provider provider : providers) {
			List<PatientQueue> queues = queueingService.getPatientQueueList(provider,
			    org.openmrs.util.OpenmrsUtil.firstSecondOfDay(today),
			    org.openmrs.util.OpenmrsUtil.getLastMomentOfDay(today), location, null, null, null, null);

			int activeCount = 0;
			for (PatientQueue queue : queues) {
				if (queue.getStatus() != PatientQueue.Status.COMPLETED) {
					activeCount++;
				}
			}

			counts.put(provider, activeCount);
		}

		return counts;
	}
	
	private ProviderAssignmentStrategy createStrategy(String strategyName) {
		if (strategyName == null || strategyName.trim().isEmpty()) {
			log.warn("Empty strategy name, using least busy strategy");
			return new LeastBusyProviderStrategy();
		}
		
		switch (strategyName.trim()) {
			case PatientQueueingConfig.ProviderAssignmentStrategy.LEAST_BUSY:
				return new LeastBusyProviderStrategy();
			case PatientQueueingConfig.ProviderAssignmentStrategy.ROUND_ROBIN:
				return new RoundRobinProviderStrategy();
			case PatientQueueingConfig.ProviderAssignmentStrategy.RANDOM:
				return new RandomProviderStrategy();
			case PatientQueueingConfig.ProviderAssignmentStrategy.MANUAL:
				log.info("Manual provider assignment strategy selected");
				return new ManualProviderAssignmentStrategy();
			default:
				log.warn("Unknown strategy: " + strategyName + ", using least busy strategy");
				return new LeastBusyProviderStrategy();
		}
	}
}
