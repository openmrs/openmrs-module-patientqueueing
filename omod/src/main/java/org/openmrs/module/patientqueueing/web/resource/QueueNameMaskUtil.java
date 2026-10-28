/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.web.resource;

/**
 * Masks patient names for public kiosk and display screens, where anyone could otherwise enumerate
 * patients by typing ticket numbers or phone numbers. Shared by every endpoint that renders a
 * patient name on an unattended screen; keep masking rules in one place.
 */
public final class QueueNameMaskUtil {
	
	private QueueNameMaskUtil() {
	}
	
	/**
	 * Mask patient name for privacy in public queue displays. Examples: "John Doe" → "J. D***",
	 * "Mary Jane Smith" → "M. J. S***"
	 * 
	 * @param fullName the full patient name
	 * @return masked name, or null if input is null
	 */
	public static String maskPatientName(String fullName) {
		if (fullName == null || fullName.trim().isEmpty()) {
			return null;
		}
		
		String[] parts = fullName.trim().split("\\s+");
		StringBuilder masked = new StringBuilder();
		
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.isEmpty()) {
				continue;
			}
			// First character + dot + space (except last part)
			masked.append(part.charAt(0));
			if (i < parts.length - 1) {
				masked.append(". ");
			}
		}
		
		masked.append("***");
		return masked.toString();
	}
}
