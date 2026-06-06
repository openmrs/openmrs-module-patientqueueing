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
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueByUuid(java.lang.String)
	 */
	public PatientQueue getPatientQueueByUUID(String uuid) {
		return (PatientQueue) getSession().createCriteria(PatientQueue.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueList(org.openmrs.Provider,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.Patient, org.openmrs.module.patientqueueing.model.PatientQueue.Status)
	 */
	public List<PatientQueue> getPatientQueueList(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom) {
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
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		criteria.add(Restrictions.eq("voided", false));
		
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
	public PatientQueue getIncompletePatientQueue(Patient patient, Location locationTo, Location queueRoom) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (locationTo != null) {
			criteria.add(Restrictions.eq("locationTo", locationTo));
		}
		
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		
		criteria.add(Restrictions.ne("status", PatientQueue.Status.COMPLETED));
		criteria.add(Restrictions.eq("voided", false));
		
		return (PatientQueue) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getMostRecentQueue(org.openmrs.Patient)
	 */
	public PatientQueue getMostRecentQueue(Patient patient) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		criteria.setMaxResults(1);
		
		return (PatientQueue) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#getPatientQueueListBySearchParams(java.lang.String,
	 *      java.util.Date, java.util.Date, org.openmrs.Location, org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status)
	 */
	
	public List<PatientQueue> getPatientQueueList(List<Patient> patientList, Date fromDate, Date toDate,
	        Location locationTo, Location locationFrom, PatientQueue.Status status, Location queueRoom) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (!patientList.isEmpty()) {
			criteria.add(Restrictions.in("patient", patientList));
		}
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		if (locationTo != null) {
			criteria.add(Restrictions.eq("locationTo", locationTo));
		}
		
		if (locationFrom != null) {
			criteria.add(Restrictions.eq("locationFrom", locationFrom));
		}
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.patientqueueing.api.PatientQueueingService#
	 *      getPatientQueueByParentLocation(org.openmrs.Location,
	 *      org.openmrs.module.patientqueueing.model.PatientQueue.Status,java.util.Date dateFrom,
	 *      java.util.Date)
	 */
	public List<PatientQueue> getPatientsInQueueRoom(List<Location> queueRooms, PatientQueue.Status status, Date fromDate,
	        Date toDate) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		if (queueRooms != null) {
			criteria.add(Restrictions.in("queueRoom", queueRooms));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}
	
	public List<PatientQueue> getPatientQueueByVisitNumber(String visitNumber, Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		criteria.add(Restrictions.eq("visitNumber", visitNumber));
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.desc("dateCreated"));
		
		return criteria.list();
	}
	
	public List<PatientQueue> getPatientQueueListFifo(Provider provider, Date fromDate, Date toDate, Location locationTo,
	        Location locationFrom, Patient patient, PatientQueue.Status status, Location queueRoom) {
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
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.asc("dateCreated"));
		return criteria.list();
	}
	
	public List<PatientQueue> getPatientsInQueueRoomFifo(List<Location> queueRooms, PatientQueue.Status status,
	        Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(PatientQueue.class);
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		if (queueRooms != null) {
			criteria.add(Restrictions.in("queueRoom", queueRooms));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.asc("dateCreated"));
		return criteria.list();
	}
	
	// ========== Non-Patient Queue DAO Methods ==========
	
	/**
	 * Get a NonPatientQueue by id
	 * 
	 * @param queueId the id of the queue entry
	 * @return the NonPatientQueue with the given id
	 */
	public NonPatientQueue getNonPatientQueueById(Integer queueId) {
		return (NonPatientQueue) getSession().createCriteria(NonPatientQueue.class)
		        .add(Restrictions.eq("nonPatientQueueId", queueId)).uniqueResult();
	}
	
	/**
	 * Get a NonPatientQueue by uuid
	 * 
	 * @param uuid the uuid of the queue entry
	 * @return the NonPatientQueue with the given uuid
	 */
	public NonPatientQueue getNonPatientQueueByUuid(String uuid) {
		return (NonPatientQueue) getSession().createCriteria(NonPatientQueue.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * Get NonPatientQueue entries by ticket number
	 * 
	 * @param ticketNumber the ticket number to search for
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return list of NonPatientQueue entries matching the ticket number
	 */
	public List<NonPatientQueue> getNonPatientQueueByTicketNumber(String ticketNumber, Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(NonPatientQueue.class);
		
		if (ticketNumber != null) {
			criteria.add(Restrictions.eq("ticketNumber", ticketNumber));
		}
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * Get NonPatientQueue entries by queue room
	 * 
	 * @param queueRoom the queue room location
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return list of NonPatientQueue entries in the queue room
	 */
	public List<NonPatientQueue> getNonPatientQueuesByQueueRoom(Location queueRoom, Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(NonPatientQueue.class);
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.asc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * Get NonPatientQueue entries by queue room and status
	 * 
	 * @param queueRoom the queue room location
	 * @param status the status to filter by
	 * @return list of NonPatientQueue entries matching the criteria
	 */
	public List<NonPatientQueue> getNonPatientQueuesByQueueRoomAndStatus(Location queueRoom,
	        NonPatientQueue.NonPatientQueueStatus status) {
		Criteria criteria = getSession().createCriteria(NonPatientQueue.class);
		
		if (queueRoom != null) {
			criteria.add(Restrictions.eq("queueRoom", queueRoom));
		}
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.asc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * Get all active NonPatientQueue entries
	 * 
	 * @return list of all active NonPatientQueue entries
	 */
	public List<NonPatientQueue> getAllActiveNonPatientQueues() {
		return getSession().createCriteria(NonPatientQueue.class)
		        .add(Restrictions.ne("status", NonPatientQueue.NonPatientQueueStatus.COMPLETED))
		        .add(Restrictions.ne("status", NonPatientQueue.NonPatientQueueStatus.CANCELLED))
		        .add(Restrictions.eq("voided", false)).addOrder(Order.asc("dateCreated")).list();
	}
	
	/**
	 * Get NonPatientQueue entries with flexible filtering
	 * 
	 * @param status the status to filter by (can be null)
	 * @param queueType the queue type concept to filter by (can be null)
	 * @param locationTo the destination location to filter by (can be null, list for hierarchy)
	 * @param queueRoom the queue room to filter by (can be null, list for hierarchy)
	 * @param fromDate the start date for filtering (can be null)
	 * @param toDate the end date for filtering (can be null)
	 * @return list of NonPatientQueue entries matching the criteria
	 */
	public List<NonPatientQueue> getNonPatientQueues(NonPatientQueue.NonPatientQueueStatus status, Concept queueType,
	        List<Location> locationTo, List<Location> queueRoom, Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(NonPatientQueue.class);
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		if (queueType != null) {
			criteria.add(Restrictions.eq("queueType", queueType));
		}
		
		if (locationTo != null && !locationTo.isEmpty()) {
			criteria.add(Restrictions.in("locationTo", locationTo));
		}
		
		if (queueRoom != null && !queueRoom.isEmpty()) {
			criteria.add(Restrictions.in("queueRoom", queueRoom));
		}
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.asc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * Save or update a NonPatientQueue entry
	 * 
	 * @param nonPatientQueue the queue entry to save
	 * @return the saved NonPatientQueue entry
	 */
	public NonPatientQueue saveNonPatientQueue(NonPatientQueue nonPatientQueue) {
		getSession().saveOrUpdate(nonPatientQueue);
		return nonPatientQueue;
	}
	
	/**
	 * Get NonPatientQueue entries in multiple locations with FIFO ordering
	 * 
	 * @param locations the list of locations to search in
	 * @param status the queue status to filter by; may be null
	 * @param queueType the queue type concept to filter by; may be null
	 * @param fromDate the start date for filtering by creation date; may be null
	 * @param toDate the end date for filtering by creation date; may be null
	 * @return a FIFO-ordered list of non-patient queue entries for the given locations
	 */
	public List<NonPatientQueue> getNonPatientQueuesInLocationsFifo(List<Location> locations,
	        NonPatientQueue.NonPatientQueueStatus status, Concept queueType, Date fromDate, Date toDate) {
		Criteria criteria = getSession().createCriteria(NonPatientQueue.class);
		
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		
		if (queueType != null) {
			criteria.add(Restrictions.eq("queueType", queueType));
		}
		
		if (locations != null && !locations.isEmpty()) {
			criteria.add(Restrictions.in("locationTo", locations));
		}
		
		if (fromDate != null && toDate != null) {
			criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
		}
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.asc("dateCreated"));
		
		return criteria.list();
	}
	
	/**
	 * Count pending patient queues by location. Returns a map of location UUID to count. This is
	 * more efficient than loading all queues and counting in Java.
	 * 
	 * @param locations the list of locations to count queues for
	 * @param fromDate the start date for filtering
	 * @param toDate the end date for filtering
	 * @return a map of location UUID to pending queue count
	 */
	@SuppressWarnings("unchecked")
	public java.util.Map<String, Integer> countPendingQueuesByLocation(List<Location> locations, Date fromDate, Date toDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pq.locationTo.uuid, COUNT(pq.patientQueueId) ");
		sb.append("FROM patientqueueing.PatientQueue pq ");
		sb.append("WHERE pq.dateCreated BETWEEN :fromDate AND :toDate ");
		sb.append("AND pq.status = :status ");
		sb.append("AND pq.voided = false ");
		
		if (locations != null && !locations.isEmpty()) {
			sb.append("AND pq.locationTo IN (:locations) ");
		}
		
		sb.append("GROUP BY pq.locationTo.uuid");
		
		org.hibernate.Query query = getSession().createQuery(sb.toString());
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		query.setParameter("status", PatientQueue.Status.PENDING);
		
		if (locations != null && !locations.isEmpty()) {
			query.setParameterList("locations", locations);
		}
		
		java.util.Map<String, Integer> result = new java.util.HashMap<String, Integer>();
		List<Object[]> rows = query.list();
		for (Object[] row : rows) {
			result.put((String) row[0], ((Number) row[1]).intValue());
		}
		
		return result;
	}
	
	/**
	 * Get all patient queues today for unique patient counting. Returns only patient IDs for
	 * efficiency, not full Patient objects.
	 * 
	 * @param fromDate the start date
	 * @param toDate the end date
	 * @return set of unique patient IDs
	 */
	@SuppressWarnings("unchecked")
	public Set<Integer> getUniquePatientIdsForToday(Date fromDate, Date toDate) {
		String hql = "SELECT DISTINCT pq.patient.patientId FROM patientqueueing.PatientQueue pq "
		        + "WHERE pq.dateCreated BETWEEN :fromDate AND :toDate " + "AND pq.patient IS NOT NULL "
		        + "AND pq.voided = false";
		
		org.hibernate.Query query = getSession().createQuery(hql);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		query.setMaxResults(10000); // Safety limit
		
		return new java.util.HashSet<Integer>((List<Integer>) query.list());
	}
	
	/**
	 * Count all non-patient queues created today.
	 * 
	 * @param fromDate the start date
	 * @param toDate the end date
	 * @return count of non-patient queues
	 */
	public Long countNonPatientQueuesToday(Date fromDate, Date toDate) {
		String hql = "SELECT COUNT(npq.nonPatientQueueId) FROM patientqueueing.NonPatientQueue npq "
		        + "WHERE npq.dateCreated BETWEEN :fromDate AND :toDate AND npq.voided = false";
		
		org.hibernate.Query query = getSession().createQuery(hql);
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
		
		return (Long) query.uniqueResult();
	}
	
	/**
	 * Find a patient by person attribute value using direct SQL execution.
	 * 
	 * @param personAttributeTypeId the person attribute type ID
	 * @param attributeValue the attribute value to match
	 * @return the first matching patient, or null if not found
	 */
	public Patient getPatientByPersonAttributeValue(final Integer personAttributeTypeId, final String attributeValue) {
		if (personAttributeTypeId == null) {
			throw new IllegalArgumentException("personAttributeTypeId cannot be null");
		}
		if (attributeValue == null || attributeValue.trim().isEmpty()) {
			throw new IllegalArgumentException("attributeValue cannot be null or empty");
		}
		
		final String sql = "SELECT p.patient_id FROM person_attribute pa "
		        + "INNER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id "
		        + "INNER JOIN patient p ON pa.person_id = p.patient_id "
		        + "WHERE pat.person_attribute_type_id = ? AND pa.value = ? " + "AND p.voided = 0 AND pa.voided = 0 LIMIT 1";
		
		final Integer[] patientIdHolder = new Integer[1];
		
		try {
			getSession().doWork(new org.hibernate.jdbc.Work() {
				
				@Override
				public void execute(java.sql.Connection connection) throws java.sql.SQLException {
					PreparedStatement ps = null;
					try {
						ps = connection.prepareStatement(sql);
						ps.setInt(1, personAttributeTypeId);
						ps.setString(2, attributeValue);
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								int id = rs.getInt("patient_id");
								if (!rs.wasNull()) {
									patientIdHolder[0] = id;
								}
							}
						}
						finally {
							rs.close();
						}
					}
					finally {
						if (ps != null) {
							ps.close();
						}
					}
				}
			});
		}
		catch (Exception e) {
			log.error("Error executing patient lookup query for attribute type: " + personAttributeTypeId, e);
			return null;
		}
		
		return patientIdHolder[0] != null ? (Patient) getSession().get(Patient.class, patientIdHolder[0]) : null;
	}
	
	/**
	 * Find a patient by person attribute value using attribute type UUID.
	 * 
	 * @param attributeTypeUuid the person attribute type UUID (more stable than numeric ID)
	 * @param attributeValue the attribute value to match
	 * @return the first matching patient, or null if not found
	 */
	public Patient getPatientByPersonAttributeValue(final String attributeTypeUuid, final String attributeValue) {
		if (attributeTypeUuid == null) {
			throw new IllegalArgumentException("attributeTypeUuid cannot be null");
		}
		if (attributeValue == null || attributeValue.trim().isEmpty()) {
			throw new IllegalArgumentException("attributeValue cannot be null or empty");
		}
		
		final String sql = "SELECT p.patient_id FROM person_attribute pa "
		        + "INNER JOIN person_attribute_type pat ON pa.person_attribute_type_id = pat.person_attribute_type_id "
		        + "INNER JOIN patient p ON pa.person_id = p.patient_id " + "WHERE pat.uuid = ? AND pa.value = ? "
		        + "AND p.voided = 0 AND pa.voided = 0";
		
		final Integer[] patientIdHolder = new Integer[1];
		
		try {
			getSession().doWork(new org.hibernate.jdbc.Work() {
				
				@Override
				public void execute(java.sql.Connection connection) throws java.sql.SQLException {
					PreparedStatement ps = null;
					try {
						ps = connection.prepareStatement(sql);
						ps.setString(1, attributeTypeUuid);
						ps.setString(2, attributeValue);
						ps.setMaxRows(1);
						ResultSet rs = ps.executeQuery();
						try {
							if (rs.next()) {
								int id = rs.getInt("patient_id");
								if (!rs.wasNull()) {
									patientIdHolder[0] = id;
								}
							}
						}
						finally {
							rs.close();
						}
					}
					finally {
						if (ps != null) {
							ps.close();
						}
					}
				}
			});
		}
		catch (Exception e) {
			log.error("Error executing patient lookup query for attribute type UUID: " + attributeTypeUuid, e);
			return null;
		}
		
		return patientIdHolder[0] != null ? (Patient) getSession().get(Patient.class, patientIdHolder[0]) : null;
	}
	
}
