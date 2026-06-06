package org.openmrs.module.patientqueueing.web.customdto;

import org.openmrs.Patient;
import org.openmrs.Visit;

import java.io.Serializable;

/**
 * Result of a self check-in operation Contains the visit, queue entry, and ticket information
 */
public class CheckInResult implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The patient who checked in
	 */
	private Patient patient;
	
	/**
	 * The visit created by check-in
	 */
	private Visit visit;
	
	/**
	 * The queue entry created Can be either PatientQueue or NonPatientQueue
	 */
	private QueueEntry queueEntry;
	
	/**
	 * The ticket number assigned
	 */
	private String ticketNumber;
	
	/**
	 * The estimated wait time in minutes
	 */
	private Integer estimatedWaitMinutes;
	
	/**
	 * The current position in queue
	 */
	private Integer queuePosition;
	
	/**
	 * Whether the check-in was successful
	 */
	private Boolean success;
	
	/**
	 * Error message if check-in failed
	 */
	private String errorMessage;
	
	// Empty constructor
	public CheckInResult() {
	}
	
	// Constructor for successful check-in
	public CheckInResult(Patient patient, Visit visit, QueueEntry queueEntry, String ticketNumber,
	    Integer estimatedWaitMinutes, Integer queuePosition) {
		this.patient = patient;
		this.visit = visit;
		this.queueEntry = queueEntry;
		this.ticketNumber = ticketNumber;
		this.estimatedWaitMinutes = estimatedWaitMinutes;
		this.queuePosition = queuePosition;
		this.success = true;
	}
	
	// Constructor for failed check-in
	public CheckInResult(String errorMessage) {
		this.errorMessage = errorMessage;
		this.success = false;
	}
	
	// Getters and Setters
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Visit getVisit() {
		return visit;
	}
	
	public void setVisit(Visit visit) {
		this.visit = visit;
	}
	
	public QueueEntry getQueueEntry() {
		return queueEntry;
	}
	
	public void setQueueEntry(QueueEntry queueEntry) {
		this.queueEntry = queueEntry;
	}
	
	public String getTicketNumber() {
		return ticketNumber;
	}
	
	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
	}
	
	public Integer getEstimatedWaitMinutes() {
		return estimatedWaitMinutes;
	}
	
	public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
		this.estimatedWaitMinutes = estimatedWaitMinutes;
	}
	
	public Integer getQueuePosition() {
		return queuePosition;
	}
	
	public void setQueuePosition(Integer queuePosition) {
		this.queuePosition = queuePosition;
	}
	
	public Boolean getSuccess() {
		return success;
	}
	
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
