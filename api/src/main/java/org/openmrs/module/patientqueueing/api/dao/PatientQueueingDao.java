/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.api.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("patientqueueing.PatientQueueingDao")
public class PatientQueueingDao {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	public DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public PatientQueue getPatientQueueById(String queueId) {
		return (PatientQueue) getSession().createCriteria(PatientQueue.class)
		        .add(Restrictions.eq("patient_queue_id", queueId)).uniqueResult();
	}
	
	public List<PatientQueue> getPatientQueueByPatient(Patient patient) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.eq("patient", patient));
		return criteria.list();
	}
	
	public PatientQueue savePatientQueue(PatientQueue patientQueue) {
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(patientQueue);
			return patientQueue;
		}
		catch (Exception e) {
			log.error(e);
		}
		return null;
	}
	
	public List<PatientQueue> getPatientInQueue(Provider provider, Date fromDate, Date toDate, Location sessionLocation) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		criteria.add(Restrictions.eq("provider", provider));
		criteria.add(Restrictions.eq("locationTo", sessionLocation));
		return criteria.list();
	}
}
