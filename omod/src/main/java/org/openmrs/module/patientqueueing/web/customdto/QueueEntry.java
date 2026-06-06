package org.openmrs.module.patientqueueing.web.customdto;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;

import java.io.Serializable;
import java.util.Date;

/**
 * Unified DTO for queue entries that can represent either a PatientQueue or NonPatientQueue. This
 * class serves multiple purposes: 1. Kiosk displays and public queue displays where both types need
 * to be shown together 2. Provider dashboard REST API responses 3. Check-in result responses
 * Supports both full OpenMRS objects (for internal use) and simple UUID references (for REST API).
 */
public class QueueEntry implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// ===== Common fields (both internal and REST API) =====
	
	/**
	 * The UUID of the queue entry Maps to: PatientQueue.uuid or NonPatientQueue.uuid
	 */
	private String uuid;
	
	/**
	 * The ticket number for this queue entry Maps to: PatientQueue.visitNumber or
	 * NonPatientQueue.ticketNumber
	 */
	private String ticketNumber;
	
	/**
	 * The status of this queue entry Maps to: PatientQueue.Status or
	 * NonPatientQueue.NonPatientQueueStatus
	 */
	private String status;
	
	/**
	 * The display name for this queue entry For PatientQueue: Patient name (given_name +
	 * family_name) For NonPatientQueue: displayName field
	 */
	private String displayName;
	
	/**
	 * The type of queue entry Possible values: "PATIENT" or "NON_PATIENT"
	 */
	private String queueType;
	
	/**
	 * The priority of this queue entry Maps to: PatientQueue.priority or NonPatientQueue.priority
	 */
	private Integer priority;
	
	/**
	 * Any additional comments Maps to: PatientQueue.comment or NonPatientQueue.comment
	 */
	private String comment;
	
	/**
	 * The date when this entry was created Maps to: PatientQueue.dateCreated or
	 * NonPatientQueue.dateCreated
	 */
	private Date dateCreated;
	
	// ===== Internal use fields (OpenMRS objects) =====
	
	/**
	 * The destination location (OpenMRS object) Maps to: PatientQueue.locationTo or
	 * NonPatientQueue.locationTo
	 */
	private Location locationTo;
	
	/**
	 * The queue room where this entry is queued (OpenMRS object) Maps to: PatientQueue.queueRoom or
	 * NonPatientQueue.queueRoom
	 */
	private Location queueRoom;
	
	/**
	 * The patient (only for PATIENT type) Maps to: PatientQueue.patient
	 */
	private Patient patient;
	
	/**
	 * The provider assigned to this entry Maps to: PatientQueue.provider or
	 * NonPatientQueue.servedBy/calledBy
	 */
	private Provider provider;
	
	/**
	 * The date when the entry was picked/called Maps to: PatientQueue.datePicked or
	 * NonPatientQueue.calledAt
	 */
	private Date calledAt;
	
	/**
	 * The date when service started Maps to: null or NonPatientQueue.startedAt
	 */
	private Date startedAt;
	
	/**
	 * The date when the entry was completed Maps to: PatientQueue.dateCompleted or
	 * NonPatientQueue.endedAt
	 */
	private Date completedAt;
	
	/**
	 * Whether this is a patient queue entry
	 */
	private Boolean isPatientQueue;
	
	// ===== REST API fields (String UUIDs and additional properties) =====
	
	/**
	 * Helper for updates/debugging Values: PATIENT_QUEUE or NON_PATIENT_QUEUE
	 */
	private String sourceType;
	
	/**
	 * UUID of the destination location (for REST API updates) Used when frontend sends the
	 * destination
	 */
	private String locationToUuid;
	
	/**
	 * UUID of the queue room (for REST API updates) Used when frontend sends the destination
	 */
	private String queueRoomUuid;
	
	/**
	 * Human-readable current location name
	 */
	private String currentLocation;
	
	/**
	 * Human-readable service location name
	 */
	private String serviceLocation;
	
	/**
	 * Identifier for how the patient is identified (e.g., "Patient Record", "Other")
	 */
	private String identifiedBy;
	
	/**
	 * Action to perform (e.g., "FORWARD", "CALL", "COMPLETE") Used for REST API updates
	 */
	private String action;
	
	// ===== Constructors =====
	
	/**
	 * Empty constructor for JSON/serialization
	 */
	public QueueEntry() {
	}
	
	/**
	 * Create a QueueEntry from a PatientQueue
	 */
	public QueueEntry(org.openmrs.module.patientqueueing.model.PatientQueue patientQueue) {
		this.uuid = patientQueue.getUuid();
		this.ticketNumber = patientQueue.getVisitNumber();
		this.status = patientQueue.getStatus() != null ? patientQueue.getStatus().name() : null;
		this.displayName = formatPatientName(patientQueue.getPatient());
		this.queueType = "PATIENT";
		this.locationTo = patientQueue.getLocationTo();
		this.queueRoom = patientQueue.getQueueRoom();
		this.dateCreated = patientQueue.getDateCreated();
		this.priority = patientQueue.getPriority();
		this.comment = patientQueue.getComment();
		this.patient = patientQueue.getPatient();
		this.provider = patientQueue.getProvider();
		this.calledAt = patientQueue.getDatePicked();
		this.completedAt = patientQueue.getDateCompleted();
		this.isPatientQueue = true;
		this.sourceType = "PATIENT_QUEUE";
		
		// Set UUID fields for REST API
		if (this.locationTo != null) {
			this.locationToUuid = this.locationTo.getUuid();
			this.currentLocation = this.locationTo.getName();
		}
		if (this.queueRoom != null) {
			this.queueRoomUuid = this.queueRoom.getUuid();
			this.serviceLocation = this.queueRoom.getName();
		}
		this.identifiedBy = "Patient Record";
	}
	
	/**
	 * Create a QueueEntry from a NonPatientQueue
	 */
	public QueueEntry(org.openmrs.module.patientqueueing.model.NonPatientQueue nonPatientQueue) {
		this.uuid = nonPatientQueue.getUuid();
		this.ticketNumber = nonPatientQueue.getTicketNumber();
		this.status = nonPatientQueue.getStatus() != null ? nonPatientQueue.getStatus().name() : null;
		this.displayName = nonPatientQueue.getDisplayName();
		this.queueType = "NON_PATIENT";
		this.locationTo = nonPatientQueue.getLocationTo();
		this.queueRoom = nonPatientQueue.getQueueRoom();
		this.dateCreated = nonPatientQueue.getDateCreated();
		this.priority = nonPatientQueue.getPriority();
		this.comment = nonPatientQueue.getComment();
		this.provider = nonPatientQueue.getServedBy() != null ? nonPatientQueue.getServedBy() : nonPatientQueue
		        .getCalledBy();
		this.calledAt = nonPatientQueue.getCalledAt();
		this.startedAt = nonPatientQueue.getStartedAt();
		this.completedAt = nonPatientQueue.getEndedAt();
		this.isPatientQueue = false;
		this.sourceType = "NON_PATIENT_QUEUE";
		
		// Set UUID fields for REST API
		if (this.locationTo != null) {
			this.locationToUuid = this.locationTo.getUuid();
			this.currentLocation = this.locationTo.getName();
		}
		if (this.queueRoom != null) {
			this.queueRoomUuid = this.queueRoom.getUuid();
			this.serviceLocation = this.queueRoom.getName();
		}
		this.identifiedBy = "Other";
	}
	
	/**
	 * Format patient name as "GivenName FamilyName"
	 */
	private String formatPatientName(Patient patient) {
		if (patient == null || patient.getPersonName() == null) {
			return null;
		}
		return patient.getPersonName().getGivenName() + " " + patient.getPersonName().getFamilyName();
	}
	
	// ===== Getters and Setters =====
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getTicketNumber() {
		return ticketNumber;
	}
	
	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
	}
	
	/**
	 * Get the queue number. This is now an alias for ticketNumber to maintain backward
	 * compatibility with API clients that expect the queueNumber field.
	 * 
	 * @return the ticket number
	 */
	public String getQueueNumber() {
		return ticketNumber;
	}
	
	/**
	 * Set the queue number. This is now an alias for ticketNumber to maintain backward
	 * compatibility with API clients that expect the queueNumber field.
	 * 
	 * @param queueNumber the queue number (will be stored as ticketNumber)
	 */
	public void setQueueNumber(String queueNumber) {
		this.ticketNumber = queueNumber;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getQueueType() {
		return queueType;
	}
	
	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}
	
	public Location getLocationTo() {
		return locationTo;
	}
	
	public void setLocationTo(Location locationTo) {
		this.locationTo = locationTo;
		if (locationTo != null) {
			this.locationToUuid = locationTo.getUuid();
			this.currentLocation = locationTo.getName();
		}
	}
	
	public Location getQueueRoom() {
		return queueRoom;
	}
	
	public void setQueueRoom(Location queueRoom) {
		this.queueRoom = queueRoom;
		if (queueRoom != null) {
			this.queueRoomUuid = queueRoom.getUuid();
			this.serviceLocation = queueRoom.getName();
		}
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public Date getCalledAt() {
		return calledAt;
	}
	
	public void setCalledAt(Date calledAt) {
		this.calledAt = calledAt;
	}
	
	public Date getStartedAt() {
		return startedAt;
	}
	
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	
	public Date getCompletedAt() {
		return completedAt;
	}
	
	public void setCompletedAt(Date completedAt) {
		this.completedAt = completedAt;
	}
	
	public Boolean getIsPatientQueue() {
		return isPatientQueue;
	}
	
	public void setIsPatientQueue(Boolean isPatientQueue) {
		this.isPatientQueue = isPatientQueue;
	}
	
	public String getSourceType() {
		return sourceType;
	}
	
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	
	public String getLocationToUuid() {
		return locationToUuid;
	}
	
	public void setLocationToUuid(String locationToUuid) {
		this.locationToUuid = locationToUuid;
	}
	
	public String getQueueRoomUuid() {
		return queueRoomUuid;
	}
	
	public void setQueueRoomUuid(String queueRoomUuid) {
		this.queueRoomUuid = queueRoomUuid;
	}
	
	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}
	
	public String getServiceLocation() {
		return serviceLocation;
	}
	
	public void setServiceLocation(String serviceLocation) {
		this.serviceLocation = serviceLocation;
	}
	
	public String getIdentifiedBy() {
		return identifiedBy;
	}
	
	public void setIdentifiedBy(String identifiedBy) {
		this.identifiedBy = identifiedBy;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	// ===== Helper methods =====
	
	/**
	 * Check if this is a patient queue entry
	 */
	public boolean isPatient() {
		return Boolean.TRUE.equals(isPatientQueue);
	}
	
	/**
	 * Check if this is a non-patient queue entry
	 */
	public boolean isNonPatient() {
		return Boolean.FALSE.equals(isPatientQueue);
	}
}
