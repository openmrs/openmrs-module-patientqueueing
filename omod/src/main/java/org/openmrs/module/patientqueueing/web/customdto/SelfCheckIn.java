package org.openmrs.module.patientqueueing.web.customdto;

import org.openmrs.Patient;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;

import java.io.Serializable;

/**
 * DTO for self check-in supporting both Patients and NonPatients. Extends CheckInPatient to
 * maintain backward compatibility while adding support for non-patient check-ins.
 */
public class SelfCheckIn extends CheckInPatient implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Type of check-in: "patient" or "nonPatient"
	 */
	private String checkInType;
	
	/**
	 * Non-patient queue (populated when checkInType is "nonPatient")
	 */
	private NonPatientQueue nonPatientQueue;
	
	/**
	 * Patient details (populated when checkInType is "patient")
	 */
	private Patient patient;
	
	/**
	 * Display name (for non-patient check-ins)
	 */
	private String displayName;
	
	/**
	 * Phone number (used for both patient lookup and non-patient check-ins)
	 */
	private String phoneNumber;
	
	/**
	 * Ticket/visit number
	 */
	private String ticketNumber;
	
	public SelfCheckIn() {
		super();
	}
	
	public String getCheckInType() {
		return checkInType;
	}
	
	public void setCheckInType(String checkInType) {
		this.checkInType = checkInType;
	}
	
	public NonPatientQueue getNonPatientQueue() {
		return nonPatientQueue;
	}
	
	public void setNonPatientQueue(NonPatientQueue nonPatientQueue) {
		this.nonPatientQueue = nonPatientQueue;
		if (nonPatientQueue != null && this.getUuid() == null) {
			setUuid(nonPatientQueue.getUuid());
		}
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getTicketNumber() {
		return ticketNumber;
	}
	
	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
	}
}
