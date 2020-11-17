/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.mapper;

import java.io.Serializable;

/**
 * This class is used to map  patient queue attributes to either string or integers into an object i object that can be converted to a simple object.
 */
public class PatientQueueMapper implements Serializable {
	
	private Integer patientQueueId;
	
	private Integer patientId;
	
	private String patientNames;
	
	private String age;
	
	private String gender;
	
	private String providerNames;
	
	private String locationFrom;
	
	private String locationTo;
	
	private String status;
	
	private String dateCreated;

	private String dateChanged;

	private String changedBy;
	
	private String creatorId;
	
	private String creatorNames;
	
	private String encounterId;
	
	private String visitNumber;
	
	private Integer priority;
	
	private String priorityComment;
	
	public PatientQueueMapper() {
	}
	
	public Integer getPatientQueueId() {
		return patientQueueId;
	}
	
	public void setPatientQueueId(Integer patientQueueId) {
		this.patientQueueId = patientQueueId;
	}
	
	public Integer getPatientId() {
		return patientId;
	}
	
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	public String getPatientNames() {
		return patientNames;
	}
	
	public void setPatientNames(String patientNames) {
		this.patientNames = patientNames;
	}
	
	public String getProviderNames() {
		return providerNames;
	}
	
	public void setProviderNames(String providerNames) {
		this.providerNames = providerNames;
	}
	
	public String getLocationFrom() {
		return locationFrom;
	}
	
	public void setLocationFrom(String locationFrom) {
		this.locationFrom = locationFrom;
	}
	
	public String getLocationTo() {
		return locationTo;
	}
	
	public void setLocationTo(String locationTo) {
		this.locationTo = locationTo;
	}
	
	public Integer getId() {
		return patientQueueId;
	}
	
	public void setId(Integer integer) {
		patientQueueId = integer;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getAge() {
		return age;
	}
	
	public void setAge(String age) {
		this.age = age;
	}
	
	public String getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(String dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public String getChangedBy() {
		return changedBy;
	}
	
	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}
	
	public String getCreatorId() {
		return creatorId;
	}
	
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	
	public String getCreatorNames() {
		return creatorNames;
	}
	
	public void setCreatorNames(String creatorNames) {
		this.creatorNames = creatorNames;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getVisitNumber() {
		return visitNumber;
	}
	
	public void setVisitNumber(String visitNumber) {
		this.visitNumber = visitNumber;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public String getPriorityComment() {
		return priorityComment;
	}
	
	public void setPriorityComment(String priorityComment) {
		this.priorityComment = priorityComment;
	}
	
	public String getEncounterId() {
		return encounterId;
	}
	
	public void setEncounterId(String encounterId) {
		this.encounterId = encounterId;
	}
}
