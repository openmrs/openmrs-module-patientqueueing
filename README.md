# openmrs-module-patientqueueing
# Patient Queueing Module

The **Patient Queueing Module** is an API-based OpenMRS module designed to facilitate the management of patient queues within healthcare facilities. It allows providers to efficiently track, manage, and serve patients based on various attributes including location, priority, and status of care.

This module provides functionality to queue patients in a hospital with a health system. It also includes a **Care Provider Dashboard**, enabling providers to:
- View the list of patients currently in the queue
- Message other providers
- Receive messages from other providers regarding patient care

---

## Features

- Queue patients from one location to another
- Assign providers to queued patients
- Track the status of patient queues (e.g., waiting, picked, completed)
- Record priority levels and comments for triage
- Associate queues with encounters and visits
- Track timestamps for when patients are picked and when their queue is completed
- Enable communication between providers through internal messaging features
- View and manage queues from a dedicated care provider dashboard

---

## Related UI Module

The **Patient Queue UI Module** provides a user interface built on the OpenMRS App Framework for managing and visualizing queues. It can be found at:

🔗 [Patient Queue UI GitHub Repository](https://github.com/openmrs/openmrs-module-patientqueueui)

---

## Integration

This module is intended for use as a backend service and can be integrated with other OpenMRS modules or custom applications. The patient queue data model supports flexible queue workflows by linking core OpenMRS entities such as:

- **Patients**  
- **Providers**  
- **Locations (From, To, and Queue Room)**  
- **Encounters**  

Each patient queue entry includes metadata such as:

- Status (enumerated)
- Priority and related comments
- Visit number
- Timestamps for when the patient is picked and when the queue is completed

---

## Usage

To use the Patient Queueing Module:

1. Deploy it on an OpenMRS instance.
2. Use the API to queue and manage patients programmatically.
3. (Optionally) Deploy the [Patient Queue UI](https://github.com/openmrs/openmrs-module-patientqueueui) module for a front-end interface.

---

## License

This module is distributed under the [OpenMRS Public License](https://openmrs.org/license/).

---

## Contributing

We welcome contributions! Please fork the repository, create a feature branch, and submit a pull request. For major changes, please open an issue first to discuss what you would like to change.

---

## Maintainers

This module is maintained by the OpenMRS community. For any issues or feature requests, please file an issue on the GitHub repository.
