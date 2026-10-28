package org.openmrs.module.patientqueueing.customdto;

public class QueueDisplayContextDto {
	
	private String type;
	
	private String locationUuid;
	
	private String locationName;
	
	public QueueDisplayContextDto() {
	}
	
	public QueueDisplayContextDto(String type, String locationUuid, String locationName) {
		this.type = type;
		this.locationUuid = locationUuid;
		this.locationName = locationName;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLocationUuid() {
		return locationUuid;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public String getLocationName() {
		return locationName;
	}
	
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
}
