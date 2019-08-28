package org.openmrs.module.patientqueueing.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.openmrs.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "patientqueueing.PatientQueue")
@Table(name = "patient_queue")
public class PatientQueue extends BaseOpenmrsData implements Serializable {
	
	@Id
	@GeneratedValue
	@Column(name = "patient_queue_id")
	@JsonBackReference
	private Integer patientQueueId;
	
	@ManyToOne
	@JoinColumn(name = "patient_id")
	@JsonBackReference
	private Patient patient;
	
	@ManyToOne
	@JoinColumn(name = "provider_id")
	@JsonBackReference
	private Provider provider;
	
	@ManyToOne
	@JoinColumn(name = "location_from")
	@JsonBackReference
	private Location locationFrom;
	
	@ManyToOne
	@JoinColumn(name = "location_to")
	@JsonBackReference
	private Location locationTo;
	
	@ManyToOne
	@JoinColumn(name = "encounter_id")
	@JsonBackReference
	private Encounter encounter;
	
	@Column(name = "status", length = 255)
	private String status;
	
	public PatientQueue() {
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
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public User getCreator() {
		return creator;
	}
	
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
}
