/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientqueueing.web.resource;

import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.util.OpenmrsUtil;

import java.util.Date;

/**
 * Utility methods for REST resources.
 */
public final class RestResourceUtils {
	
	private RestResourceUtils() {
	}
	
	/**
	 * Parse date range from request context with defaults to today.
	 * 
	 * @param context the request context
	 * @return DateRange with from and to dates
	 */
	public static DateRange parseDateRange(RequestContext context) {
		return parseDateRange(context, null, null);
	}
	
	/**
	 * Parse date range from request context with specified defaults.
	 * 
	 * @param context the request context
	 * @param defaultFrom default from date (null for start of today)
	 * @param defaultTo default to date (null for end of today)
	 * @return DateRange with from and to dates
	 */
	public static DateRange parseDateRange(RequestContext context, Date defaultFrom, Date defaultTo) {
		String dateFromParam = context.getParameter("dateFrom");
		String dateToParam = context.getParameter("dateTo");
		
		Date fromDate = defaultFrom;
		if (fromDate == null) {
			fromDate = OpenmrsUtil.firstSecondOfDay(new Date());
		}
		
		Date toDate = defaultTo;
		if (toDate == null) {
			toDate = OpenmrsUtil.getLastMomentOfDay(new Date());
		}
		
		if (dateFromParam != null && !dateFromParam.isEmpty()) {
			try {
				fromDate = new Date(Long.parseLong(dateFromParam));
			}
			catch (NumberFormatException e) {
				// Use default
			}
		}
		
		if (dateToParam != null && !dateToParam.isEmpty()) {
			try {
				toDate = new Date(Long.parseLong(dateToParam));
			}
			catch (NumberFormatException e) {
				// Use default
			}
		}
		
		return new DateRange(fromDate, toDate);
	}
	
	/**
	 * Parse patient queue status from string.
	 * 
	 * @param status the status string
	 * @return PatientQueue.Status or null if invalid
	 */
	public static PatientQueue.Status parsePatientQueueStatus(String status) {
		if (status == null || status.isEmpty()) {
			return null;
		}
		try {
			return PatientQueue.Status.valueOf(status.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Parse non-patient queue status from string.
	 * 
	 * @param status the status string
	 * @return NonPatientQueue.NonPatientQueueStatus or null if invalid
	 */
	public static NonPatientQueue.NonPatientQueueStatus parseNonPatientQueueStatus(String status) {
		if (status == null || status.isEmpty()) {
			return null;
		}
		try {
			return NonPatientQueue.NonPatientQueueStatus.valueOf(status.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Truncate location name to specified length.
	 * 
	 * @param locationName the location name
	 * @param maxLength maximum length
	 * @param defaultValue default value if locationName is null
	 * @return truncated location name
	 */
	public static String truncateLocationName(String locationName, int maxLength, String defaultValue) {
		if (locationName == null) {
			return defaultValue;
		}
		if (locationName.length() > maxLength) {
			return locationName.substring(0, maxLength);
		}
		return locationName;
	}
	
	/**
	 * Pad ticket number with leading zeros.
	 * 
	 * @param number the number to pad
	 * @return padded number as string
	 */
	public static String padTicketNumber(int number) {
		if (number <= 9) {
			return "00" + number;
		} else if (number < 100) {
			return "0" + number;
		}
		return String.valueOf(number);
	}
	
	/**
	 * Date range holder.
	 */
	public static class DateRange {
		
		private final Date fromDate;
		
		private final Date toDate;
		
		public DateRange(Date fromDate, Date toDate) {
			this.fromDate = fromDate;
			this.toDate = toDate;
		}
		
		public Date getFromDate() {
			return fromDate;
		}
		
		public Date getToDate() {
			return toDate;
		}
	}
}
