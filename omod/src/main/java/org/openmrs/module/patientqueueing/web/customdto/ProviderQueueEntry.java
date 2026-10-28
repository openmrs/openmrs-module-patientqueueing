package org.openmrs.module.patientqueueing.web.customdto;

/**
 * Alias for QueueEntry used in ProviderQueueResource. This class exists for backward compatibility
 * and follows the UgandaEMR naming convention. All functionality is provided by the parent
 * QueueEntry class. QueueEntry serves as a unified DTO that supports both: 1. Kiosk/check-in flows
 * (original QueueEntry purpose) 2. Provider dashboard REST API (ProviderQueueEntry purpose)
 */
public class ProviderQueueEntry extends QueueEntry {
	
	private static final long serialVersionUID = 1L;
	
	// All fields and methods are inherited from QueueEntry
	// This class provides the UgandaEMR-compatible naming while
	// leveraging all the unified functionality
	
	public ProviderQueueEntry() {
		super();
	}
	
	/**
	 * Create a ProviderQueueEntry from a PatientQueue
	 */
	public ProviderQueueEntry(org.openmrs.module.patientqueueing.model.PatientQueue patientQueue) {
		super(patientQueue);
	}
	
	/**
	 * Create a ProviderQueueEntry from a NonPatientQueue
	 */
	public ProviderQueueEntry(org.openmrs.module.patientqueueing.model.NonPatientQueue nonPatientQueue) {
		super(nonPatientQueue);
	}
}
