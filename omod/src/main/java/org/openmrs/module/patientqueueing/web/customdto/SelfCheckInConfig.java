package org.openmrs.module.patientqueueing.web.customdto;

import java.io.Serializable;
import java.util.List;

/**
 * Configuration for self check-in options available to kiosk app
 */
public class SelfCheckInConfig implements Serializable {
	
	private String uuid = "self-check-in-config";
	
	private List<IdentifierTypeConfig> identifierTypes;
	
	private List<AttributeTypeConfig> attributeTypes;
	
	public SelfCheckInConfig() {
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public List<IdentifierTypeConfig> getIdentifierTypes() {
		return identifierTypes;
	}
	
	public void setIdentifierTypes(List<IdentifierTypeConfig> identifierTypes) {
		this.identifierTypes = identifierTypes;
	}
	
	public List<AttributeTypeConfig> getAttributeTypes() {
		return attributeTypes;
	}
	
	public void setAttributeTypes(List<AttributeTypeConfig> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}
	
	public static class IdentifierTypeConfig implements Serializable {
		
		private String uuid;
		
		private String name;
		
		private String display;
		
		private String description;
		
		private String inputType; // "text", "numeric", etc.
		
		public IdentifierTypeConfig() {
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDisplay() {
			return display;
		}
		
		public void setDisplay(String display) {
			this.display = display;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getInputType() {
			return inputType;
		}
		
		public void setInputType(String inputType) {
			this.inputType = inputType;
		}
	}
	
	public static class AttributeTypeConfig implements Serializable {
		
		private String uuid;
		
		private String name;
		
		private String display;
		
		private String description;
		
		private String inputHint; // e.g. "Enter phone number"
		
		public AttributeTypeConfig() {
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDisplay() {
			return display;
		}
		
		public void setDisplay(String display) {
			this.display = display;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getInputHint() {
			return inputHint;
		}
		
		public void setInputHint(String inputHint) {
			this.inputHint = inputHint;
		}
	}
}
