package org.openmrs.module.patientqueueing.customdto;

import java.util.Date;

public class QueueDisplayRowDto {
	
	private String uuid;
	
	private String ticketNumber;
	
	private String status;
	
	private String displayName;
	
	private String queueLocation;
	
	private String serviceLocation;
	
	private Date dateCreated;
	
	private Integer priority;
	
	private Integer priorityScore;
	
	private String priorityReason;
	
	public QueueDisplayRowDto() {
	}
	
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
	
	public String getQueueLocation() {
		return queueLocation;
	}
	
	public void setQueueLocation(String queueLocation) {
		this.queueLocation = queueLocation;
	}
	
	public String getServiceLocation() {
		return serviceLocation;
	}
	
	public void setServiceLocation(String serviceLocation) {
		this.serviceLocation = serviceLocation;
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
	
	public Integer getPriorityScore() {
		return priorityScore;
	}
	
	public void setPriorityScore(Integer priorityScore) {
		this.priorityScore = priorityScore;
	}
	
	public String getPriorityReason() {
		return priorityReason;
	}
	
	public void setPriorityReason(String priorityReason) {
		this.priorityReason = priorityReason;
	}
}
