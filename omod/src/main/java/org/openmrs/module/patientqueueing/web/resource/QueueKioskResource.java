package org.openmrs.module.patientqueueing.web.resource;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientqueueing.api.PatientQueueingService;
import org.openmrs.module.patientqueueing.web.customdto.QueueEntry;
import org.openmrs.module.patientqueueing.model.NonPatientQueue;
import org.openmrs.module.patientqueueing.model.PatientQueue;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.util.OpenmrsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST resource for queue kiosk displays Provides unified access to both patient and non-patient
 * queues Supports ticket lookup by ticket number Handles both our clean API and
 * UgandaEMR-compatible requests for backward compatibility
 */
@Resource(name = RestConstants.VERSION_1 + "/patientqueueing/kiosk", supportedClass = SimpleObject.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class QueueKioskResource extends DelegatingCrudResource<SimpleObject> {
	
	private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	
	private static final Pattern DDMMYYYY_PATTERN = Pattern.compile("^(\\d{2})\\/(\\d{2})\\/(\\d{4})-");
	
	private static final Pattern YYYYMMDD_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})-");
	
	/**
	 * Cache for queue room statistics to avoid N+1 queries
	 */
	private static class QueueRoomStats {
		
		private final List<PatientQueue> waitingPatients;
		
		private final List<PatientQueue> completedToday;
		
		private final List<NonPatientQueue> waitingNonPatients;
		
		private final List<NonPatientQueue> completedNonPatientsToday;
		
		private final Map<String, Integer> positionCache;
		
		private final Map<String, Integer> queueLengthCache;
		
		private final Map<String, Integer> avgServiceTimeCache;
		
		QueueRoomStats(List<PatientQueue> waitingPatients, List<PatientQueue> completedToday,
		    List<NonPatientQueue> waitingNonPatients, List<NonPatientQueue> completedNonPatientsToday) {
			this.waitingPatients = waitingPatients != null ? waitingPatients : new ArrayList<PatientQueue>();
			this.completedToday = completedToday != null ? completedToday : new ArrayList<PatientQueue>();
			this.waitingNonPatients = waitingNonPatients != null ? waitingNonPatients : new ArrayList<NonPatientQueue>();
			this.completedNonPatientsToday = completedNonPatientsToday != null ? completedNonPatientsToday
			        : new ArrayList<NonPatientQueue>();
			this.positionCache = new HashMap<String, Integer>();
			this.queueLengthCache = new HashMap<String, Integer>();
			this.avgServiceTimeCache = new HashMap<String, Integer>();
			
			// Pre-calculate queue lengths
			queueLengthCache.put("PATIENT", this.waitingPatients.size());
			queueLengthCache.put("NON_PATIENT", this.waitingNonPatients.size());
			
			// Pre-calculate average service time (in minutes)
			if (!this.completedToday.isEmpty()) {
				long totalTime = 0;
				int count = 0;
				for (PatientQueue pq : this.completedToday) {
					if (pq.getDateCreated() != null && pq.getDateChanged() != null) {
						totalTime += pq.getDateChanged().getTime() - pq.getDateCreated().getTime();
						count++;
					}
				}
				if (count > 0) {
					avgServiceTimeCache.put("PATIENT", (int) ((totalTime / count) / (1000 * 60)));
				}
			}
			if (!this.completedNonPatientsToday.isEmpty()) {
				long totalTime = 0;
				int count = 0;
				for (NonPatientQueue npq : this.completedNonPatientsToday) {
					if (npq.getDateCreated() != null && npq.getDateChanged() != null) {
						totalTime += npq.getDateChanged().getTime() - npq.getDateCreated().getTime();
						count++;
					}
				}
				if (count > 0) {
					avgServiceTimeCache.put("NON_PATIENT", (int) ((totalTime / count) / (1000 * 60)));
				}
			}
		}
		
		public Integer getQueueLength(String type) {
			return queueLengthCache.get(type);
		}
		
		public Integer getPosition(String uuid, Date dateCreated, String type) {
			if (positionCache.containsKey(uuid)) {
				return positionCache.get(uuid);
			}
			
			List<?> queues = "PATIENT".equals(type) ? waitingPatients : waitingNonPatients;
			if (dateCreated == null) {
				return null;
			}
			
			int position = 1;
			for (Object queue : queues) {
				Date otherDate;
				String otherUuid;
				if (queue instanceof PatientQueue) {
					otherDate = ((PatientQueue) queue).getDateCreated();
					otherUuid = ((PatientQueue) queue).getUuid();
				} else {
					otherDate = ((NonPatientQueue) queue).getDateCreated();
					otherUuid = ((NonPatientQueue) queue).getUuid();
				}
				
				if (otherUuid != null && !otherUuid.equals(uuid) && otherDate != null && otherDate.before(dateCreated)) {
					position++;
				}
			}
			
			positionCache.put(uuid, position);
			return position;
		}
		
		public Integer getAverageServiceTime(String type) {
			return avgServiceTimeCache.get(type);
		}
	}
	
	@Override
	public SimpleObject newDelegate() {
		return new SimpleObject();
	}
	
	@Override
	public SimpleObject save(SimpleObject simpleObject) {
		return simpleObject;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(
	        org.openmrs.module.webservices.rest.web.representation.Representation representation) {
		return null;
	}
	
	@Override
	public SimpleObject getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void delete(SimpleObject simpleObject, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(SimpleObject simpleObject, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * Format date to ISO 8601 string
	 */
	private String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATE_FORMAT);
		return sdf.format(date);
	}
	
	/**
	 * Extract date from ticket number if present. Supports formats: DD/MM/YYYY-XXX or
	 * YYYY-MM-DD-XXX Returns null if no date found in ticket number.
	 * 
	 * @param ticketNumber the ticket number (may contain date prefix)
	 * @return the date extracted from ticket number, or null if not found
	 */
	private Date extractDateFromTicketNumber(String ticketNumber) {
		if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
			return null;
		}
		
		// Pattern for DD/MM/YYYY-XXX format
		Matcher ddmmyyyyMatcher = DDMMYYYY_PATTERN.matcher(ticketNumber);
		if (ddmmyyyyMatcher.find()) {
			try {
				int day = Integer.parseInt(ddmmyyyyMatcher.group(1));
				int month = Integer.parseInt(ddmmyyyyMatcher.group(2));
				int year = Integer.parseInt(ddmmyyyyMatcher.group(3));
				return createDateAtMidnight(year, month, day);
			}
			catch (NumberFormatException e) {
				// Invalid date format - fall through to next pattern (ticket may have different format)
			}
		}
		
		// Pattern for YYYY-MM-DD-XXX format
		Matcher yyyymmddMatcher = YYYYMMDD_PATTERN.matcher(ticketNumber);
		if (yyyymmddMatcher.find()) {
			try {
				int year = Integer.parseInt(yyyymmddMatcher.group(1));
				int month = Integer.parseInt(yyyymmddMatcher.group(2));
				int day = Integer.parseInt(yyyymmddMatcher.group(3));
				return createDateAtMidnight(year, month, day);
			}
			catch (NumberFormatException e) {
				// Invalid date format - fall through (ticket may have different format)
			}
		}
		
		return null;
	}
	
	/**
	 * Get date range for searching queues. If ticket number contains a date, use that date.
	 * Otherwise, use today's date.
	 * 
	 * @param ticketNumber the ticket number (may contain date prefix)
	 * @return array of [fromDate, toDate] for the search range
	 */
	private Date[] getDateRangeForTicket(String ticketNumber) {
		Date ticketDate = extractDateFromTicketNumber(ticketNumber);
		
		if (ticketDate != null) {
			// Use the date from the ticket number
			return new Date[] { OpenmrsUtil.firstSecondOfDay(ticketDate), OpenmrsUtil.getLastMomentOfDay(ticketDate) };
		}
		
		// Fall back to today
		Date today = new Date();
		return new Date[] { OpenmrsUtil.firstSecondOfDay(today), OpenmrsUtil.getLastMomentOfDay(today) };
	}
	
	/**
	 * Build queue room stats cache for all locations in the given queues This avoids N+1 queries by
	 * fetching all stats in bulk
	 */
	private Map<Location, QueueRoomStats> buildQueueRoomStats(List<PatientQueue> patientQueues,
	        List<NonPatientQueue> nonPatientQueues, PatientQueueingService service) {
		Map<Location, QueueRoomStats> statsMap = new HashMap<Location, QueueRoomStats>();
		
		Date today = new Date();
		Date fromDate = OpenmrsUtil.firstSecondOfDay(today);
		Date toDate = OpenmrsUtil.getLastMomentOfDay(today);
		
		// Collect all unique queue rooms
		Set<Location> queueRooms = new HashSet<Location>();
		for (PatientQueue pq : patientQueues) {
			if (pq.getQueueRoom() != null) {
				queueRooms.add(pq.getQueueRoom());
			}
		}
		for (NonPatientQueue npq : nonPatientQueues) {
			if (npq.getQueueRoom() != null) {
				queueRooms.add(npq.getQueueRoom());
			}
		}
		
		// For each queue room, fetch stats in bulk
		for (Location queueRoom : queueRooms) {
			List<PatientQueue> waitingPatients = service.getPatientQueueList(null, fromDate, toDate, null, null, null,
			    PatientQueue.Status.PENDING, queueRoom);
			List<PatientQueue> completedToday = service.getPatientQueueList(null, fromDate, toDate, null, null, null,
			    PatientQueue.Status.COMPLETED, queueRoom);
			List<NonPatientQueue> waitingNonPatients = service.getNonPatientQueues(
			    NonPatientQueue.NonPatientQueueStatus.WAITING, null, null, queueRoom, fromDate, toDate);
			List<NonPatientQueue> completedNonPatientsToday = service.getNonPatientQueues(
			    NonPatientQueue.NonPatientQueueStatus.COMPLETED, null, null, queueRoom, fromDate, toDate);
			
			statsMap.put(queueRoom, new QueueRoomStats(waitingPatients, completedToday, waitingNonPatients,
			        completedNonPatientsToday));
		}
		
		return statsMap;
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) throws ResponseException {
		// Check for journey endpoint request
		String journeyParam = context.getParameter("journey");
		if (journeyParam != null && "true".equalsIgnoreCase(journeyParam)) {
			return new NeedsPaging<SimpleObject>(Collections.singletonList(getPatientJourney(context)), context);
		}
		
		// Detect UgandaEMR request by parameter names
		String visitNumber = context.getParameter("visitNumber");
		String ticketNumber = context.getParameter("ticketNumber");
		
		if (visitNumber != null) {
			// UgandaEMR format request - return wrapped response
			return new NeedsPaging<SimpleObject>(Collections.singletonList(getKioskStatusUgandaEMRStyle(context)), context);
		} else {
			// Our clean API format - return QueueEntry directly
			QueueEntry entry = getQueueByTicketNumber(context);
			SimpleObject result = new SimpleObject();
			result.put("ticketNumber", entry.getTicketNumber());
			result.put("status", entry.getStatus());
			// Privacy: Mask patient name for kiosk display
			result.put("displayName", maskPatientName(entry.getDisplayName()));
			result.put("dateCreated", entry.getDateCreated());
			return new NeedsPaging<SimpleObject>(Collections.singletonList(result), context);
		}
	}
	
	/**
	 * UgandaEMR-compatible kiosk status lookup Returns wrapped response with results array
	 */
	private SimpleObject getKioskStatusUgandaEMRStyle(RequestContext context) {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		
		// Support both parameter names for compatibility
		String visitNumber = context.getParameter("visitNumber");
		String ticketNumberParam = context.getParameter("ticketNumber");
		
		String ticket = (visitNumber != null) ? visitNumber : ticketNumberParam;
		
		if (ticket == null || ticket.isEmpty()) {
			throw new IllegalArgumentException("visitNumber or ticketNumber parameter required");
		}
		
		// Extract date from ticket number if present, otherwise use today
		Date[] dateRange = getDateRangeForTicket(ticket);
		Date fromDate = dateRange[0];
		Date toDate = dateRange[1];
		
		// Search in PatientQueue
		List<PatientQueue> patientQueues = service.getPatientQueueByVisitNumber(ticket, fromDate, toDate);
		
		// Search in NonPatientQueue
		List<NonPatientQueue> nonPatientQueues = service.getNonPatientQueueByTicketNumber(ticket, fromDate, toDate);
		
		// Build stats cache once for all queue rooms to avoid N+1 queries
		Map<Location, QueueRoomStats> statsMap = buildQueueRoomStats(patientQueues, nonPatientQueues, service);
		
		// Combine results into unified list
		List<Object> results = new ArrayList<Object>();
		
		for (PatientQueue pq : patientQueues) {
			results.add(createKioskStatusFromPatientQueue(pq, statsMap));
		}
		
		for (NonPatientQueue npq : nonPatientQueues) {
			results.add(createKioskStatusFromNonPatientQueue(npq, statsMap));
		}
		
		// Wrap in results array like UgandaEMR
		SimpleObject response = new SimpleObject();
		response.put("results", results);
		
		return response;
	}
	
	/**
	 * Search for a queue entry by ticket number. This searches both patient and non-patient queues.
	 * <p>
	 * Request parameters:
	 * <ul>
	 * <li>ticketNumber (required): the ticket number to search for</li>
	 * <li>dateFrom (optional): start date as timestamp in milliseconds</li>
	 * <li>dateTo (optional): end date as timestamp in milliseconds</li>
	 * </ul>
	 * 
	 * @param context the request context containing the parameters
	 * @return the QueueEntry if found
	 */
	public QueueEntry getQueueByTicketNumber(RequestContext context) {
		String ticketNumber = context.getParameter("ticketNumber");
		
		if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
			throw new IllegalArgumentException("ticketNumber parameter is required");
		}
		
		// Parse date parameters
		Long dateFromMs = null;
		Long dateToMs = null;
		String dateFromParam = context.getParameter("dateFrom");
		String dateToParam = context.getParameter("dateTo");
		
		if (dateFromParam != null && !dateFromParam.isEmpty()) {
			try {
				dateFromMs = Long.parseLong(dateFromParam);
			}
			catch (NumberFormatException e) {
				// If invalid, use start of today
				dateFromMs = OpenmrsUtil.firstSecondOfDay(new Date()).getTime();
			}
		} else {
			// Default to start of today
			dateFromMs = OpenmrsUtil.firstSecondOfDay(new Date()).getTime();
		}
		
		if (dateToParam != null && !dateToParam.isEmpty()) {
			try {
				dateToMs = Long.parseLong(dateToParam);
			}
			catch (NumberFormatException e) {
				// If invalid, use end of today
				dateToMs = OpenmrsUtil.getLastMomentOfDay(new Date()).getTime();
			}
		} else {
			// Default to end of today
			dateToMs = OpenmrsUtil.getLastMomentOfDay(new Date()).getTime();
		}
		
		Date fromDate = new Date(dateFromMs);
		Date toDate = new Date(dateToMs);
		
		PatientQueueingService service = Context.getService(PatientQueueingService.class);
		
		// Search in patient queues
		List<PatientQueue> patientQueues = service.getPatientQueueByVisitNumber(ticketNumber, fromDate, toDate);
		
		// Search in non-patient queues
		List<NonPatientQueue> nonPatientQueues = service.getNonPatientQueueByTicketNumber(ticketNumber, fromDate, toDate);
		
		// Check results
		if (!patientQueues.isEmpty() && !nonPatientQueues.isEmpty()) {
			// Both found - this shouldn't happen with proper ticket numbering
			throw new IllegalArgumentException("Multiple queue entries found for ticket number: " + ticketNumber);
		}
		
		if (!patientQueues.isEmpty()) {
			// Return patient queue entry
			return new QueueEntry(patientQueues.get(0));
		}
		
		if (!nonPatientQueues.isEmpty()) {
			// Return non-patient queue entry
			return new QueueEntry(nonPatientQueues.get(0));
		}
		
		// Not found
		throw new IllegalArgumentException("No queue entry found for ticket number: " + ticketNumber);
	}
	
	/**
	 * Get all queue entries for a specific location (facility or room) Returns both patient and
	 * non-patient queues, sorted by creation date
	 * 
	 * @param context the request context
	 * @return list of QueueEntry objects
	 */
	
	/**
	 * Parse patient queue status string to enum
	 */
	private PatientQueue.Status getPatientQueueStatus(String status) {
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
	 * Parse non-patient queue status string to enum
	 */
	private NonPatientQueue.NonPatientQueueStatus getNonPatientQueueStatus(String status) {
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
	 * Create UgandaEMR-compatible kiosk status from PatientQueue
	 */
	private Map<String, Object> createKioskStatusFromPatientQueue(PatientQueue pq, Map<Location, QueueRoomStats> statsMap) {
		Map<String, Object> status = new HashMap<String, Object>();
		status.put("uuid", pq.getUuid());
		status.put("ticketNumber", pq.getVisitNumber());
		status.put("visitNumber", pq.getVisitNumber());
		status.put("status", pq.getStatus().name());
		status.put("queueLocation", pq.getLocationTo() != null ? pq.getLocationTo().getDisplayString() : null);
		status.put("serviceLocation", pq.getQueueRoom() != null ? pq.getQueueRoom().getDisplayString() : null);
		status.put("checkedInAt", formatDate(pq.getDateCreated()));
		status.put("datePicked", formatDate(pq.getDateChanged()));
		status.put("dateCreated", formatDate(pq.getDateCreated()));
		status.put("dateCompleted", pq.getStatus() == PatientQueue.Status.COMPLETED ? formatDate(pq.getDateChanged()) : null);
		
		// Add nested objects like UgandaEMR
		status.put("locationTo", createLocationRef(pq.getLocationTo()));
		status.put("queueRoom", createLocationRef(pq.getQueueRoom()));
		// Privacy: Mask patient name for kiosk display (e.g., "John Doe" → "J. D***")
		String fullName = pq.getPatient() != null && pq.getPatient().getPersonName() != null ? pq.getPatient()
		        .getPersonName().getFullName() : null;
		status.put("displayName", maskPatientName(fullName));
		status.put("queueType", "PATIENT");
		
		// Add queue position information for waiting patients using cached stats
		if (pq.getStatus() == PatientQueue.Status.PENDING && pq.getQueueRoom() != null && statsMap != null) {
			QueueRoomStats stats = statsMap.get(pq.getQueueRoom());
			if (stats != null) {
				status.put("position", stats.getPosition(pq.getUuid(), pq.getDateCreated(), "PATIENT"));
				status.put("queueLength", stats.getQueueLength("PATIENT"));
				Integer avgTime = stats.getAverageServiceTime("PATIENT");
				if (avgTime != null) {
					Integer position = stats.getPosition(pq.getUuid(), pq.getDateCreated(), "PATIENT");
					if (position != null && position > 1) {
						status.put("estimatedWaitMinutes", position * avgTime);
					} else {
						status.put("estimatedWaitMinutes", 0);
					}
				} else {
					status.put("estimatedWaitMinutes", null);
				}
			} else {
				status.put("position", null);
				status.put("queueLength", null);
				status.put("estimatedWaitMinutes", null);
			}
		} else {
			status.put("position", null);
			status.put("queueLength", null);
			status.put("estimatedWaitMinutes", null);
		}
		
		return status;
	}
	
	/**
	 * Create UgandaEMR-compatible kiosk status from NonPatientQueue
	 */
	private Map<String, Object> createKioskStatusFromNonPatientQueue(NonPatientQueue npq,
	        Map<Location, QueueRoomStats> statsMap) {
		Map<String, Object> status = new HashMap<String, Object>();
		status.put("uuid", npq.getUuid());
		status.put("ticketNumber", npq.getTicketNumber());
		status.put("status", npq.getStatus().name());
		status.put("queueLocation", npq.getLocationTo() != null ? npq.getLocationTo().getDisplayString() : null);
		status.put("serviceLocation", npq.getQueueRoom() != null ? npq.getQueueRoom().getDisplayString() : null);
		status.put("checkedInAt", formatDate(npq.getDateCreated()));
		status.put("dateCreated", formatDate(npq.getDateCreated()));
		status.put("dateCompleted",
		    npq.getStatus() == NonPatientQueue.NonPatientQueueStatus.COMPLETED ? formatDate(npq.getDateChanged()) : null);
		status.put("displayName", npq.getDisplayName());
		status.put("queueType", "NON_PATIENT");
		
		// Add nested objects like UgandaEMR
		status.put("locationTo", createLocationRef(npq.getLocationTo()));
		status.put("queueRoom", createLocationRef(npq.getQueueRoom()));
		
		// Add queue position information for waiting patients using cached stats
		if (npq.getStatus() == NonPatientQueue.NonPatientQueueStatus.WAITING && npq.getQueueRoom() != null
		        && statsMap != null) {
			QueueRoomStats stats = statsMap.get(npq.getQueueRoom());
			if (stats != null) {
				status.put("position", stats.getPosition(npq.getUuid(), npq.getDateCreated(), "NON_PATIENT"));
				status.put("queueLength", stats.getQueueLength("NON_PATIENT"));
				Integer avgTime = stats.getAverageServiceTime("NON_PATIENT");
				if (avgTime != null) {
					Integer position = stats.getPosition(npq.getUuid(), npq.getDateCreated(), "NON_PATIENT");
					if (position != null && position > 1) {
						status.put("estimatedWaitMinutes", position * avgTime);
					} else {
						status.put("estimatedWaitMinutes", 0);
					}
				} else {
					status.put("estimatedWaitMinutes", null);
				}
			} else {
				status.put("position", null);
				status.put("queueLength", null);
				status.put("estimatedWaitMinutes", null);
			}
		} else {
			status.put("position", null);
			status.put("queueLength", null);
			status.put("estimatedWaitMinutes", null);
		}
		
		return status;
	}
	
	/**
	 * Create location reference object for UgandaEMR compatibility
	 */
	private Map<String, Object> createLocationRef(Location location) {
		if (location == null) {
			return null;
		}
		
		Map<String, Object> ref = new HashMap<String, Object>();
		ref.put("uuid", location.getUuid());
		ref.put("display", location.getDisplayString());
		ref.put("name", location.getName());
		return ref;
	}
	
	/**
	 * Get complete patient journey through the facility Shows completed services, current service,
	 * and upcoming services GET /ws/rest/v1/kiosk?journey=true&ticketNumber=XXX
	 * 
	 * @param context request context with ticketNumber parameter
	 * @return SimpleObject with journey details
	 */
	public SimpleObject getPatientJourney(RequestContext context) {
		PatientQueueingService service = Context.getService(PatientQueueingService.class);

		String ticketNumber = context.getParameter("ticketNumber");
		if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
			throw new IllegalArgumentException("ticketNumber parameter is required");
		}

		// Extract date from ticket number if present, otherwise use today
		Date[] dateRange = getDateRangeForTicket(ticketNumber);
		Date fromDate = dateRange[0];
		Date toDate = dateRange[1];

		// Get all patient queues for this ticket number
		List<PatientQueue> patientQueues = service.getPatientQueueByVisitNumber(ticketNumber, fromDate, toDate);

		// Get all non-patient queues for this ticket number
		List<NonPatientQueue> nonPatientQueues = service.getNonPatientQueueByTicketNumber(ticketNumber, fromDate, toDate);

		// Build stats cache for all queue rooms to avoid N+1 queries
		Map<Location, QueueRoomStats> statsMap = buildQueueRoomStats(patientQueues, nonPatientQueues, service);

		// Create a map of UUID to queue objects for quick lookup (O(1) instead of O(n))
		Map<String, PatientQueue> patientQueueMap = new HashMap<String, PatientQueue>();
		for (PatientQueue pq : patientQueues) {
			patientQueueMap.put(pq.getUuid(), pq);
		}
		Map<String, NonPatientQueue> nonPatientQueueMap = new HashMap<String, NonPatientQueue>();
		for (NonPatientQueue npq : nonPatientQueues) {
			nonPatientQueueMap.put(npq.getUuid(), npq);
		}

		SimpleObject journey = new SimpleObject();

		// Combine and sort by date created to determine sequence
		List<Map<String, Object>> allServices = new ArrayList<>();

		for (PatientQueue pq : patientQueues) {
			Map<String, Object> serviceInfo = new HashMap<String, Object>();
			serviceInfo.put("uuid", pq.getUuid());
			serviceInfo.put("service", pq.getLocationTo() != null ? pq.getLocationTo().getDisplayString() : "Service");
			serviceInfo.put("room", pq.getQueueRoom() != null ? pq.getQueueRoom().getDisplayString() : "");
			serviceInfo.put("roomUuid", pq.getQueueRoom() != null ? pq.getQueueRoom().getUuid() : null);
			serviceInfo.put("status", pq.getStatus().name());
			serviceInfo.put("checkedInAt", pq.getDateCreated());
			serviceInfo.put("type", "PATIENT");
			serviceInfo.put("dateCreated", pq.getDateCreated());
			serviceInfo.put("queueRoom", pq.getQueueRoom());
			allServices.add(serviceInfo);
		}

		for (NonPatientQueue npq : nonPatientQueues) {
			Map<String, Object> serviceInfo = new HashMap<String, Object>();
			serviceInfo.put("uuid", npq.getUuid());
			serviceInfo.put("service", npq.getQueueType() != null ? npq.getQueueType().getName() : "Service");
			serviceInfo.put("room", npq.getQueueRoom() != null ? npq.getQueueRoom().getDisplayString() : "");
			serviceInfo.put("roomUuid", npq.getQueueRoom() != null ? npq.getQueueRoom().getUuid() : null);
			serviceInfo.put("status", npq.getStatus().name());
			serviceInfo.put("checkedInAt", npq.getDateCreated());
			serviceInfo.put("type", "NON_PATIENT");
			serviceInfo.put("dateCreated", npq.getDateCreated());
			serviceInfo.put("queueRoom", npq.getQueueRoom());
			allServices.add(serviceInfo);
		}

		// Sort by check-in time
		Collections.sort(allServices, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> s1, Map<String, Object> s2) {
				Date d1 = (Date) s1.get("dateCreated");
				Date d2 = (Date) s2.get("dateCreated");
				if (d1 == null && d2 == null) {
					return 0;
				}
				if (d1 == null) {
					return 1;
				}
				if (d2 == null) {
					return -1;
				}
				return d1.compareTo(d2);
			}
		});

		// Categorize services
		List<Map<String, Object>> completedServices = new ArrayList<>();
		Map<String, Object> currentService = null;
		List<Map<String, Object>> nextServices = new ArrayList<>();

		boolean foundCurrent = false;
		for (Map<String, Object> serviceInfo : allServices) {
			String status = (String) serviceInfo.get("status");
			serviceInfo.remove("dateCreated");  // Don't include in JSON response
			serviceInfo.remove("queueRoom");    // Don't include in JSON response

			// Format checkedInAt for JSON using ISO 8601
			Date checkedInAt = (Date) serviceInfo.get("checkedInAt");
			serviceInfo.put("checkedInAt", formatDate(checkedInAt));

			// Use enum comparison instead of string matching
			boolean isCompleted = PatientQueue.Status.COMPLETED.name().equals(status)
					|| NonPatientQueue.NonPatientQueueStatus.COMPLETED.name().equals(status);
			boolean isWaiting = PatientQueue.Status.PENDING.name().equals(status)
					|| NonPatientQueue.NonPatientQueueStatus.WAITING.name().equals(status);

			if (isCompleted) {
				// Calculate duration for completed services using quick lookup
				Date completedAt = null;
				String uuid = (String) serviceInfo.get("uuid");
				String type = (String) serviceInfo.get("type");

				// Quick lookup from map instead of iteration
				if ("PATIENT".equals(type)) {
					PatientQueue pq = patientQueueMap.get(uuid);
					if (pq != null) {
						completedAt = pq.getDateChanged();
					}
				} else {
					NonPatientQueue npq = nonPatientQueueMap.get(uuid);
					if (npq != null) {
						completedAt = npq.getDateChanged();
					}
				}

				if (completedAt != null && checkedInAt != null) {
					long durationMs = completedAt.getTime() - checkedInAt.getTime();
					int durationMinutes = (int) (durationMs / (1000 * 60));
					serviceInfo.put("completedAt", formatDate(completedAt));
					serviceInfo.put("durationMinutes", durationMinutes);
				}

				completedServices.add(serviceInfo);
			} else if (!foundCurrent) {
				// This is the current service - add position and wait time info
				currentService = serviceInfo;
				foundCurrent = true;

				// Add position and wait time for waiting services using cached stats
				if (isWaiting) {
					String uuid = (String) serviceInfo.get("uuid");
					String type = (String) serviceInfo.get("type");
					Location queueRoom = (Location) serviceInfo.get("queueRoom");

					if (queueRoom != null && statsMap != null) {
						QueueRoomStats stats = statsMap.get(queueRoom);
						if (stats != null) {
							currentService.put("position", stats.getPosition(uuid, checkedInAt, type));
							currentService.put("queueLength", stats.getQueueLength(type));
							Integer avgTime = stats.getAverageServiceTime(type);
							if (avgTime != null) {
								Integer position = stats.getPosition(uuid, checkedInAt, type);
								if (position != null && position > 1) {
									currentService.put("estimatedWaitMinutes", position * avgTime);
								} else {
									currentService.put("estimatedWaitMinutes", 0);
								}
							} else {
								currentService.put("estimatedWaitMinutes", null);
							}
						}
					}
				}
			} else {
				// These are upcoming services (already in queue)
				nextServices.add(serviceInfo);
			}
		}

		// Build response
		if (!patientQueues.isEmpty()) {
			PatientQueue firstQueue = patientQueues.get(0);
			// Privacy: Mask patient name for kiosk display (e.g., "John Doe" → "J. D***")
			String fullName = firstQueue.getPatient() != null && firstQueue.getPatient().getPersonName() != null
			        ? firstQueue.getPatient().getPersonName().getFullName()
			        : null;
			journey.put("displayName", maskPatientName(fullName));
			// Security: Don't expose patientUuid in kiosk endpoint to prevent further patient data enumeration
			journey.put("patientUuid", null);
		} else if (!nonPatientQueues.isEmpty()) {
			journey.put("displayName", nonPatientQueues.get(0).getDisplayName());
			journey.put("patientUuid", null);
		} else {
			throw new IllegalArgumentException("No queue entry found for ticket number: " + ticketNumber);
		}

		journey.put("ticketNumber", ticketNumber);
		journey.put("overallStatus", currentService == null ? "COMPLETED" : "IN_PROGRESS");
		journey.put("completedServices", completedServices);
		journey.put("currentService", currentService);
		journey.put("nextServices", nextServices);

		return journey;
	}
	
	/**
	 * Mask patient name for privacy in kiosk display. Examples: "John Doe" → "J. D***",
	 * "Mary Jane Smith" → "M. J. S***"
	 * 
	 * @param fullName the full patient name
	 * @return masked name, or null if input is null
	 */
	
	/**
	 * Creates a Date at midnight (00:00:00.000) for the given year, month, and day.
	 * 
	 * @param year the year (e.g., 2024)
	 * @param month the month (1-12, will be converted to 0-11 for Calendar)
	 * @param day the day of month
	 * @return Date at midnight of the specified day
	 */
	private Date createDateAtMidnight(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, 0, 0, 0);
		cal.clear(Calendar.MILLISECOND);
		return cal.getTime();
	}
	
	private String maskPatientName(String fullName) {
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
			if (part.length() > 0) {
				masked.append(part.charAt(0));
				if (i < parts.length - 1) {
					masked.append(". ");
				}
			}
		}
		
		masked.append("***");
		return masked.toString();
	}
}
