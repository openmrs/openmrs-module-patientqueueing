package org.openmrs.module.patientqueueing.model;


import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Location;
import org.openmrs.Encounter;
import org.openmrs.BaseOpenmrsData;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import java.io.Serializable;

@Entity(name = "patientqueueing.PatientQueue")
@Table(name = "patient_queue")
public class PatientQueue extends BaseOpenmrsData implements Serializable {

    @Id
    @GeneratedValue
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
    private String status;

    @Column(name = "queue_number", length = 255)
    private String queueNumber;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "priority_comment", length = 255)
    private String priorityComment;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(String queueNumber) {
        this.queueNumber = queueNumber;
    }
}
