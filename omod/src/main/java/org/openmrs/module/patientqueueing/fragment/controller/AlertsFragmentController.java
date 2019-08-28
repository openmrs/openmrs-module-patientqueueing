package org.openmrs.module.patientqueueing.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	protected final Log log = LogFactory.getLog(getClass());
	
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
