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

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("patientqueueing.PatientQueueingDao")
public class PatientQueueingDao {
	
	private static final Logger log = LoggerFactory.getLogger(PatientQueueingDao.class);
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	public DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueById(java.lang.Integer)
	 */
	public PatientQueue getPatientQueueById(Integer queueId) {
		return (PatientQueue) getSession().createCriteria(PatientQueue.class)
		        .add(Restrictions.eq("patientQueueId", queueId)).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueList(org.openmrs.Provider,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.Patient, org.openmrs.module.patientqueueing.model.PatientQueue.Status)
	 */
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		if (provider != null) {
			criteria.add(Restrictions.eq("provider", provider));
		}
		
		if (locationTo != null) {
			criteria.add(Restrictions.eq("locationTo", locationTo));
		}
		
		if (locationFrom != null) {
			criteria.add(Restrictions.eq("locationFrom", locationFrom));
		}
		
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		criteria.addOrder(Order.desc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#savePatientQue(org.openmrs.module.patientqueueing.model.PatientQueue)
	 */
	public PatientQueue savePatientQueue(PatientQueue patientQueue) {
		sessionFactory.getCurrentSession().saveOrUpdate(patientQueue);
		return patientQueue;
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getIncompletePatientQueue(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 */
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (locationTo != null) {
			criteria.add(Restrictions.eq("locationTo", locationTo));
		}
		
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		
		criteria.add(Restrictions.not(Restrictions.in("status", new Enum[] { PatientQueue.Status.COMPLETED })));
		
		return (PatientQueue) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getMostRecentQueue(org.openmrs.Patient)
	 */
	public PatientQueue getMostRecentQueue(Patient patient) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		criteria.add(Restrictions.eq("patient", patient));
		criteria.addOrder(Order.desc("dateCreated"));
		criteria.setMaxResults(1);
		
		return (PatientQueue) criteria.uniqueResult();
	}
	
}
