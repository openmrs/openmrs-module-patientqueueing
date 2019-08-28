package org.openmrs.module.patientqueueing.mapper;

import java.io.Serializable;

public class PatientQueueMapper implements Serializable {
	
	private Integer patientQueueId;
	
	private Integer patientId;
	
	private String patientNames;
	
	private String age;
	
	private String providerNames;
	
	private String locationFrom;
	
	private String locationTo;
	
	private String status;
	
	private String dateCreated;
	
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
}
