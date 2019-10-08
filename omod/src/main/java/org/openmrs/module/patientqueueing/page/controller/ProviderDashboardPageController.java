/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.patientqueueing.page.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.ui.framework.UiUtils;

import java.util.Collections;
import java.util.List;

public class ProviderDashboardPageController {
	
	public Object controller(PageModel model, @RequestParam(required = false, value = "apps") AppDescriptor app,
	        @RequestParam(required = false, value = "dashboard") String dashboard,
	        @SpringBean("adtService") AdtService adtService,
	        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	        @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
	        @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties, UiSessionContext sessionContext) {
		
		if (StringUtils.isEmpty(dashboard)) {
			dashboard = "providerDashboard";
		}
		model.addAttribute("apps", app);
		UiUtils uiUtils = new BasicUiUtils();
		
		AppContextModel contextModel = sessionContext.generateAppContextModel();
		model.addAttribute("appContextModel", contextModel);
		
		List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".includeFragments",
		    contextModel);
		Collections.sort(includeFragments);
		model.addAttribute("includeFragments", includeFragments);
		
		List<Extension> rightColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard
		        + ".rightColumnFragments", contextModel);
		Collections.sort(rightColumnFragments);
		model.addAttribute("rightColumnFragments", rightColumnFragments);
		
		List<Extension> leftColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard
		        + ".leftColumnFragments", contextModel);
		Collections.sort(leftColumnFragments);
		model.addAttribute("leftColumnFragments", leftColumnFragments);
		
		List<Extension> otherActions = appFrameworkService.getExtensionsForCurrentUser(
		    (dashboard == "providerDashboard" ? "provider" : dashboard) + ".otherActions", contextModel);
		Collections.sort(otherActions);
		model.addAttribute("otherActions", otherActions);

        // used for breadcrumbs to link back to the base dashboard in the case when this is used to render a context-specific dashboard
		model.addAttribute("baseDashboardUrl", coreAppsProperties.getDashboardUrl());

		model.addAttribute("dashboard", dashboard);

		model.put("currentLocation", sessionContext.getSessionLocation());
		return null;
	}
}
