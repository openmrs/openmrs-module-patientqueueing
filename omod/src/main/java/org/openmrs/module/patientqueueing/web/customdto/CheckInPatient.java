package org.openmrs.module.patientqueueing.web.customdto;

import org.openmrs.Visit;
import org.openmrs.module.patientqueueing.model.PatientQueue;

import java.io.Serializable;

/**
 * DTO for patient check-in results. Follows UgandaEMR pattern with PatientQueue and Visit.
 */
public class CheckInPatient implements Serializable {
	
	private String uuid;
	
	private PatientQueue patientQueue;
	
	private Visit visit;
	
	public CheckInPatient() {
	}
	
	public CheckInPatient(PatientQueue patientQueue) {
		this.patientQueue = patientQueue;
		if (patientQueue != null) {
			this.uuid = patientQueue.getUuid();
		}
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public PatientQueue getPatientQueue() {
		return patientQueue;
	}
	
	public void setPatientQueue(PatientQueue patientQueue) {
		this.patientQueue = patientQueue;
		if (patientQueue != null && this.uuid == null) {
			this.uuid = patientQueue.getUuid();
		}
	}
	
	public Visit getVisit() {
		return visit;
	}
	
	public void setVisit(Visit visit) {
		this.visit = visit;
	}
}
