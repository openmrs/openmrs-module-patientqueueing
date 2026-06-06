/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Provider;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "patientqueueing.NonPatientQueue")
@Table(name = "non_patient_queue")
public class NonPatientQueue extends BaseOpenmrsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "non_patient_queue_id")
	private Integer nonPatientQueueId;
	
	@Column(name = "ticket_number", length = 50)
	private String ticketNumber;
	
	@Column(name = "display_name", length = 255)
	private String displayName;
	
	@Column(name = "phone_number", length = 50)
	private String phoneNumber;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "queue_type_concept_id")
	private Concept queueType;
	
	@Column(name = "status", length = 50)
	@Enumerated(EnumType.STRING)
	private NonPatientQueueStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "current_location")
	private Location currentLocation;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_to")
	private Location locationTo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "queue_room")
	private Location queueRoom;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "called_by")
	private Provider calledBy;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "served_by")
	private Provider servedBy;
	
	@Column(name = "priority")
	private Integer priority;
	
	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;
	
	@Column(name = "called_at")
	private Date calledAt;
	
	@Column(name = "arrived_at")
	private Date arrivedAt;
	
	@Column(name = "started_at")
	private Date startedAt;
	
	@Column(name = "ended_at")
	private Date endedAt;
	
	public NonPatientQueue() {
	}
	
	public enum NonPatientQueueStatus {
		WAITING, CALLED, ARRIVED, SERVING, COMPLETED, SKIPPED, CANCELLED
	}
	
	@Override
	public Integer getId() {
		return nonPatientQueueId;
	}
	
	@Override
	public void setId(Integer id) {
		this.nonPatientQueueId = id;
	}
	
	public Integer getNonPatientQueueId() {
		return nonPatientQueueId;
	}
	
	public void setNonPatientQueueId(Integer nonPatientQueueId) {
		this.nonPatientQueueId = nonPatientQueueId;
	}
	
	public String getTicketNumber() {
		return ticketNumber;
	}
	
	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
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
	
	public Concept getQueueType() {
		return queueType;
	}
	
	public void setQueueType(Concept queueType) {
		this.queueType = queueType;
	}
	
	public NonPatientQueueStatus getStatus() {
		return status;
	}
	
	public void setStatus(NonPatientQueueStatus status) {
		this.status = status;
	}
	
	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}
	
	public Location getLocationTo() {
		return locationTo;
	}
	
	public void setLocationTo(Location locationTo) {
		this.locationTo = locationTo;
	}
	
	public Location getQueueRoom() {
		return queueRoom;
	}
	
	public void setQueueRoom(Location queueRoom) {
		this.queueRoom = queueRoom;
	}
	
	public Provider getCalledBy() {
		return calledBy;
	}
	
	public void setCalledBy(Provider calledBy) {
		this.calledBy = calledBy;
	}
	
	public Provider getServedBy() {
		return servedBy;
	}
	
	public void setServedBy(Provider servedBy) {
		this.servedBy = servedBy;
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
	
	public Date getCalledAt() {
		return calledAt;
	}
	
	public void setCalledAt(Date calledAt) {
		this.calledAt = calledAt;
	}
	
	public Date getArrivedAt() {
		return arrivedAt;
	}
	
	public void setArrivedAt(Date arrivedAt) {
		this.arrivedAt = arrivedAt;
	}
	
	public Date getStartedAt() {
		return startedAt;
	}
	
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	
	public Date getEndedAt() {
		return endedAt;
	}
	
	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}
	
	/**
	 * Builder for creating NonPatientQueue instances.
	 */
	public static class Builder {
		
		private final NonPatientQueue queue;
		
		public Builder() {
			this.queue = new NonPatientQueue();
		}
		
		public Builder displayName(String displayName) {
			queue.setDisplayName(displayName);
			return this;
		}
		
		public Builder phoneNumber(String phoneNumber) {
			queue.setPhoneNumber(phoneNumber);
			return this;
		}
		
		public Builder queueType(Concept queueType) {
			queue.setQueueType(queueType);
			return this;
		}
		
		public Builder currentLocation(Location currentLocation) {
			queue.setCurrentLocation(currentLocation);
			return this;
		}
		
		public Builder locationTo(Location locationTo) {
			queue.setLocationTo(locationTo);
			return this;
		}
		
		public Builder queueRoom(Location queueRoom) {
			queue.setQueueRoom(queueRoom);
			return this;
		}
		
		public Builder priority(Integer priority) {
			queue.setPriority(priority);
			return this;
		}
		
		public Builder comment(String comment) {
			queue.setComment(comment);
			return this;
		}
		
		public Builder status(NonPatientQueueStatus status) {
			queue.setStatus(status);
			return this;
		}
		
		public Builder ticketNumber(String ticketNumber) {
			queue.setTicketNumber(ticketNumber);
			return this;
		}
		
		public NonPatientQueue build() {
			if (queue.getStatus() == null) {
				queue.setStatus(NonPatientQueueStatus.WAITING);
			}
			if (queue.getDateCreated() == null) {
				queue.setDateCreated(new java.util.Date());
			}
			return queue;
		}
	}
}
