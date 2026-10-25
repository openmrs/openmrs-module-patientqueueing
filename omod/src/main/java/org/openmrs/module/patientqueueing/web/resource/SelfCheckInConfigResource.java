/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.web.resource;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.PatientQueueingConfig;
import org.openmrs.module.patientqueueing.web.customdto.SelfCheckInConfig;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST resource for exposing self check-in configuration to kiosk app. This allows the kiosk to
 * dynamically determine which input fields to display. GET /ws/rest/v1/selfcheckinconfig
 */
@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/selfcheckinconfig", supportedClass = SelfCheckInConfig.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class SelfCheckInConfigResource extends DelegatingCrudResource<SelfCheckInConfig> {
	
	private static final Logger log = LoggerFactory.getLogger(SelfCheckInConfigResource.class);
	
	@Override
	public SelfCheckInConfig newDelegate() {
		return new SelfCheckInConfig();
	}
	
	@Override
	public SelfCheckInConfig save(SelfCheckInConfig delegate) {
		throw new UnsupportedOperationException("Configuration is read-only");
	}
	
	@Override
	public SelfCheckInConfig getByUniqueId(String uniqueId) {
		// Return the current configuration
		return getSelfCheckInConfiguration();
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		// Return configuration as a single-item list
		SelfCheckInConfig config = getSelfCheckInConfiguration();
		List<SelfCheckInConfig> list = Arrays.asList(config);
		return new org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<SelfCheckInConfig>(list, context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) throws ResponseException {
		return doGetAll(context);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(
	        org.openmrs.module.webservices.rest.web.representation.Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("identifierTypes");
		description.addProperty("attributeTypes");
		description.addSelfLink();
		return description;
	}
	
	@Override
	protected void delete(SelfCheckInConfig delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Configuration is read-only");
	}
	
	@Override
	public void purge(SelfCheckInConfig delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Configuration is read-only");
	}
	
	/**
	 * Build the self check-in configuration from global properties
	 */
	private SelfCheckInConfig getSelfCheckInConfiguration() {
		AdministrationService administrationService = Context.getAdministrationService();
		PatientService patientService = Context.getPatientService();
		PersonService personService = Context.getPersonService();
		
		SelfCheckInConfig config = new SelfCheckInConfig();
		
		// Get configured identifier types
		String identifierTypeUuidsStr = administrationService.getGlobalProperty(
		    PatientQueueingConfig.GP_SELF_CHECK_IN_IDENTIFIER_TYPE_UUIDS, "");
		
		List<SelfCheckInConfig.IdentifierTypeConfig> identifierConfigs = new ArrayList<SelfCheckInConfig.IdentifierTypeConfig>();
		for (String uuid : identifierTypeUuidsStr.split(",")) {
			uuid = uuid.trim();
			if (uuid.isEmpty()) {
				continue;
			}
			
			try {
				PatientIdentifierType identifierType = patientService.getPatientIdentifierTypeByUuid(uuid);
				if (identifierType != null) {
					SelfCheckInConfig.IdentifierTypeConfig identifierTypeConfig = new SelfCheckInConfig.IdentifierTypeConfig();
					identifierTypeConfig.setUuid(identifierType.getUuid());
					identifierTypeConfig.setName(identifierType.getName());
					identifierTypeConfig.setDisplay(identifierType.getDescription() != null ? identifierType
					        .getDescription() : identifierType.getName());
					identifierTypeConfig.setDescription("Patient identifier");
					identifierTypeConfig.setInputType("text");
					identifierConfigs.add(identifierTypeConfig);
				}
			}
			catch (Exception e) {
				log.warn("Invalid identifier type UUID: " + uuid, e);
			}
		}
		
		config.setIdentifierTypes(identifierConfigs);
		
		// Get configured person attribute types
		String attributeTypeUuidsStr = administrationService.getGlobalProperty(
		    PatientQueueingConfig.GP_SELF_CHECK_IN_PERSON_ATTRIBUTE_TYPE_UUIDS, "");
		
		List<SelfCheckInConfig.AttributeTypeConfig> attributeConfigs = new ArrayList<SelfCheckInConfig.AttributeTypeConfig>();
		for (String uuid : attributeTypeUuidsStr.split(",")) {
			uuid = uuid.trim();
			if (uuid.isEmpty()) {
				continue;
			}
			
			try {
				PersonAttributeType attributeType = personService.getPersonAttributeTypeByUuid(uuid);
				if (attributeType != null) {
					SelfCheckInConfig.AttributeTypeConfig attributeTypeConfig = new SelfCheckInConfig.AttributeTypeConfig();
					attributeTypeConfig.setUuid(attributeType.getUuid());
					attributeTypeConfig.setName(attributeType.getName());
					attributeTypeConfig.setDisplay(attributeType.getDescription() != null ? attributeType.getDescription()
					        : attributeType.getName());
					attributeTypeConfig.setDescription("Person attribute");
					attributeTypeConfig.setInputHint("Enter "
					        + (attributeType.getDescription() != null ? attributeType.getDescription() : attributeType
					                .getName()).toLowerCase());
					attributeConfigs.add(attributeTypeConfig);
				}
			}
			catch (Exception e) {
				log.warn("Invalid attribute type UUID: " + uuid, e);
			}
		}
		
		config.setAttributeTypes(attributeConfigs);
		
		return config;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		throw new UnsupportedOperationException("Configuration is read-only");
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		throw new UnsupportedOperationException("Configuration is read-only");
	}
}
