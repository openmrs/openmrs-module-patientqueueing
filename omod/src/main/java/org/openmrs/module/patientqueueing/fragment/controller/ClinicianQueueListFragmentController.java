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

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.mapper.PatientQueueMapper;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ClinicianQueueListFragmentController {

    private static final Logger log = LoggerFactory.getLogger(ClinicianQueueListFragmentController.class);

    public void controller(@SpringBean FragmentModel pageModel) {

        String locationUUIDS = Context.getAdministrationService().getGlobalProperty("patientqueueing.clinicianLocationUUIDS");

        List clinicianLocationUUIDList = Arrays.asList(locationUUIDS.split(","));

        pageModel.put("clinicianLocationUUIDList", clinicianLocationUUIDList);
        pageModel.put("currentProvider", Context.getAuthenticatedUser());
    }

    public SimpleObject getPatientQueueList(@RequestParam(value = "searchfilter", required = false) String searchfilter, UiSessionContext uiSessionContext) throws IOException {

        PatientQueueingService patientQueueingService = Context.getService(PatientQueueingService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleObject simpleObject = new SimpleObject();
        List<PatientQueue> patientQueueList = new ArrayList();

		patientQueueList = patientQueueingService.getPatientQueueListBySearchParams(searchfilter,
				OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()),
				uiSessionContext.getSessionLocation(), null, null);

        List<PatientQueueMapper> patientQueueMappers = mapPatientQueueToMapper(patientQueueList);
        simpleObject.put("patientQueueList", objectMapper.writeValueAsString(patientQueueMappers));

        return simpleObject;
    }

    /**
     * Convert PatientQueue List to PatientQueueMapper
     *
     * @param patientQueueList The list of patient queues to be converted into a map
     * @return List<PatientQueueMapper> a list of patient queue with parameters mapped to strings of integers only
     */
    private List<PatientQueueMapper> mapPatientQueueToMapper(List<PatientQueue> patientQueueList) {
        List<PatientQueueMapper> patientQueueMappers = new ArrayList<PatientQueueMapper>();

        for (PatientQueue patientQueue : patientQueueList) {
            String names = patientQueue.getPatient().getFamilyName() + " " + patientQueue.getPatient().getGivenName() + " " + patientQueue.getPatient().getMiddleName();
            PatientQueueMapper patientQueueMapper = new PatientQueueMapper();
            patientQueueMapper.setId(patientQueue.getId());
            patientQueueMapper.setPatientNames(names.replace("null", ""));
            patientQueueMapper.setPatientId(patientQueue.getPatient().getPatientId());
            patientQueueMapper.setLocationFrom(patientQueue.getLocationFrom().getName());
            patientQueueMapper.setLocationTo(patientQueue.getLocationTo().getName());
            patientQueueMapper.setProviderNames(patientQueue.getProvider().getName());
            patientQueueMapper.setStatus(patientQueue.getStatus().name());
            patientQueueMapper.setAge(patientQueue.getPatient().getAge().toString());
            patientQueueMapper.setDateCreated(patientQueue.getDateCreated().toString());
            patientQueueMappers.add(patientQueueMapper);
        }
        return patientQueueMappers;
    }
}
