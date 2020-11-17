package org.openmrs.module.patientqueueing.model;

import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Location;
import org.openmrs.Encounter;
import org.openmrs.BaseOpenmrsData;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Entity(name = "patientqueueing.PatientQueue")
@Table(name = "patient_queue")
public class PatientQueue extends BaseOpenmrsData implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patient_queue_id")
	private Integer patientQueueId;
	
	@ManyToOne
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	@ManyToOne
	@JoinColumn(name = "provider_id")
	private Provider provider;
	
	@ManyToOne
	@JoinColumn(name = "location_from")
	private Location locationFrom;
	
	@ManyToOne
	@JoinColumn(name = "location_to")
	private Location locationTo;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id")
	private Encounter encounter;
	
	@Column(name = "status", length = 255)
	@Enumerated(EnumType.STRING)
	private PatientQueue.Status status;
	
	@Column(name = "visit_number", length = 255)
	private String visitNumber;
	
	@Column(name = "priority")
	private Integer priority;
	
	@Column(name = "priority_comment", length = 255)
	private String priorityComment;
	
	@Column(name = "comment", length = 255)
	private String comment;
	
	@ManyToOne
	@JoinColumn(name = "queue_room")
	private Location queueRoom;
	
	public PatientQueue() {
	}
	
	public enum Status {
		PENDING, COMPLETED, PICKED;
	}
	
	public Integer getId() {
		return patientQueueId;
	}
	
	public void setId(Integer integer) {
		patientQueueId = integer;
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
	
	public Location getLocationFrom() {
		return locationFrom;
	}
	
	public void setLocationFrom(Location locationFrom) {
		this.locationFrom = locationFrom;
	}
	
	public Location getLocationTo() {
		return locationTo;
	}
	
	public void setLocationTo(Location locationTo) {
		this.locationTo = locationTo;
	}
	
	public Encounter getEncounter() {
		return encounter;
	}
	
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}
	
	public Integer getPatientQueueId() {
		return patientQueueId;
	}
	
	public void setPatientQueueId(Integer patientQueueId) {
		this.patientQueueId = patientQueueId;
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
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getVisitNumber() {
		return visitNumber;
	}
	
	public void setVisitNumber(String visitNumber) {
		this.visitNumber = visitNumber;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Location getQueueRoom() {
		return queueRoom;
	}
	
	public void setQueueRoom(Location queueRoom) {
		this.queueRoom = queueRoom;
	}
}
