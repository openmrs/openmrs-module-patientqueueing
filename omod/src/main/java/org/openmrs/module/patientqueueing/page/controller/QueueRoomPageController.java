package org.openmrs.module.patientqueueing.page.controller;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.model.QueueRoom;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public class QueueRoomPageController {
	
	public void controller(@RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
	        PageModel pageModel) {
		List<Location> location = Context.getLocationService().getAllLocations();
		pageModel.put("location", location);
		pageModel.put("breadcrumbOverride", breadcrumbOverride);
		pageModel.put("queueRooms", Context.getService(PatientQueueingService.class).getAllQueueRoom());
	}
	
	public void post(@SpringBean PageModel pageModel, @RequestParam(value = "returnUrl", required = false) String returnUrl,
	        @RequestParam(value = "queueRoomName", required = false) String name,
	        @RequestParam(value = "queueRoomId", required = false) Integer queueRoomId,
	        @RequestParam(value = "location", required = false) Location location,
	        @RequestParam(value = "description", required = false) String description, UiSessionContext uiSessionContext,
	        UiUtils uiUtils, HttpServletRequest request) {
		PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
		
		if (queueRoomId == null) {
			QueueRoom newQueueRoom = new QueueRoom();
			newQueueRoom.setDateCreated(new Date());
			newQueueRoom.setName(name);
			newQueueRoom.setLocation(location);
			newQueueRoom.setDescription(description);
			newQueueRoom.setCreator(Context.getAuthenticatedUser());
			patientQueueingService.saveQueueRoom(newQueueRoom);
		} else {
			QueueRoom queueRoom = Context.getService(PatientQueueingService.class).getQueueRoomById(queueRoomId);
			queueRoom.setName(name);
			queueRoom.setLocation(location);
			queueRoom.setDescription(description);
			patientQueueingService.saveQueueRoom(queueRoom);
		}
		List<Location> locations = Context.getLocationService().getAllLocations();
		pageModel.put("location", locations);
		
		pageModel.put("queueRooms", patientQueueingService.getAllQueueRoom());
	}
}
