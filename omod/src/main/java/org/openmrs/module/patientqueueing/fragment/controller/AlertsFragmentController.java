/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.fragment.controller;

import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.notification.Alert;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlertsFragmentController {
	
	public AlertsFragmentController() {
	}
	
	public void controller(FragmentModel model, UiSessionContext sessionContext) {
		List<Alert> alerts = new ArrayList();
		ProviderService providerService = Context.getProviderService();
		alerts = Context.getAlertService().getAlertsByUser(sessionContext.getCurrentUser());
		model.put("providerList", providerService.getAllProviders(false));
		model.put("alerts", alerts);
	}
	
	public SimpleObject markAlertAsRead(FragmentModel model, @RequestParam("alert_message_id") String alertId, UiUtils ui)
	        throws IOException {
		Alert alert = Context.getAlertService().getAlert(Integer.valueOf(alertId));
		if (alert != null) {
			alert.setAlertRead(true);
			Context.getAlertService().saveAlert(alert);
		}
		return null;
	}
}
