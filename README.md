# Patient Queueing Module

The **Patient Queueing Module** is an API-based OpenMRS module designed to facilitate the management of patient and non-patient queues within healthcare facilities. It enables efficient tracking, management, and service delivery based on location, priority, and status.

## Features

### Core Queue Management
- ✅ **Patient Queueing**: Queue patients from one location to another
- ✅ **Non-Patient Queueing**: Manage walk-in visitors and non-patient queues
- ✅ **Provider Assignment**: Auto-assign providers using configurable strategies
- ✅ **Status Tracking**: Track queue status (waiting, called, serving, completed)
- ✅ **Priority Management**: Record priority levels and comments for triage
- ✅ **Location-Based**: Support multiple locations and queue rooms
- ✅ **Visit Integration**: Associate queues with encounters and visits

### Self Check-in
- ✅ **Patient Self Check-in**: Patients can check themselves in via REST API
- ✅ **Non-Patient Check-in**: Walk-in visitors can obtain queue tickets
- ✅ **Ticket Generation**: Automatic ticket/visit number generation
- ✅ **Queue Position**: Real-time position and estimated wait time
- ✅ **Generic Design**: Distribution-agnostic, no hardcoded identifiers

### Queue Display & Kiosk
- ✅ **Unified Queue View**: Single API for patient and non-patient queues
- ✅ **Ticket Lookup**: Search queue by ticket number
- ✅ **Public Displays**: "Now serving" and "up next" information
- ✅ **Queue Statistics**: Real-time statistics for dashboard displays
- ✅ **FIFO Ordering**: First-in-first-out queue management

### Provider Features
- ✅ **Provider Assignment Strategies**:
  - Least Busy: Assign to provider with fewest active queues
  - Round Robin: Distribute evenly across providers
  - Random: Random provider selection
- ✅ **Configurable**: Strategy selection via global properties

---

## REST API Endpoints

### Check-in APIs
```
POST /ws/rest/v1/patientqueueing/checkin           - Generic patient check-in (configurable visit type)
GET  /ws/rest/v1/patientqueueing/selfcheckin       - Self check-in for booth/kiosk (existing)
```

### Queue Display & Kiosk
```
GET  /ws/rest/v1/kiosk              - Queue lookup by ticket number
GET  /ws/rest/v1/display            - Display data (now serving, up next)
POST /ws/rest/v1/display/statistics - Queue statistics (if implemented)
```

### Non-Patient Queue Management
```
GET    /ws/rest/v1/nonpatientqueue              - List all (with filters)
GET    /ws/rest/v1/nonpatientqueue/:uuid         - Get by UUID
POST   /ws/rest/v1/nonpatientqueue              - Create new
PUT    /ws/rest/v1/nonpatientqueue/:uuid         - Update
GET    /ws/rest/v1/nonpatientqueue?q=search      - Search
```

### Patient Queue Management (Existing)
```
GET    /ws/rest/v1/patientqueue                 - List all (existing)
POST   /ws/rest/v1/patientqueue                 - Create new (existing)
GET    /ws/rest/v1/patientqueue/:uuid           - Get by UUID (existing)
PUT    /ws/rest/v1/patientqueue/:uuid           - Update (existing)
```

---

## Quick Start

### 1. Installation

```bash
mvn clean install
# Copy the .omod file to your OpenMRS modules directory
```

### 2. Configuration

Configure global properties in OpenMRS (Advanced Settings → Patient Queueing):

| Property | Description | Default |
|----------|-------------|---------|
| `patientqueueing.providerAssignmentStrategy` | Provider assignment strategy (leastBusy, roundRobin, random) | leastBusy |

### 3. Privileges & Roles

The module defines the following privileges in its `config.xml`. They are created automatically when the module starts.

| Privilege | Description | Used by |
|-----------|-------------|---------|
| `Task: patientqueueing.viewQueue` | Ability to view queue status and entries | Queue view, kiosk, display, provider dashboard (GET) |
| `Task: patientqueueing.sendPatientToQueue` | Ability to send a patient to the queue | Check-in (save patient queue entry) |
| `Task: patientqueueing.checkIn` | Ability to perform patient check-in | Non-patient check-in |
| `Task: patientqueueing.manageQueue` | Ability to manage queue entries (pick, complete, skip, call) | Provider dashboard actions |
| `Task: patientqueueing.completePatientQueue` | Able to complete a patient queue | Completing queue entries |
| `App: patientqueueing.providerDashboard` | Able to access Provider Dashboard | Provider dashboard app |
| `App: patientqueueing.selfCheckIn` | Ability to access self check-in kiosk | Self check-in kiosk app |
| `App: patientqueueing.queueDisplay` | Ability to access queue display/kiosk view | Public queue display |

#### Managing privileges with Initializer

Distributions using the [Initializer](https://github.com/mekom/openmrs-module-initializer) module can manage these privileges (and their role assignments) through their own iniz configuration instead of manual role editing. A ready-to-use fragment is shipped with the module at:

```
omod/src/main/resources/configuration/privileges/patientqueueing-privileges.csv
```

Copy it into your distribution's iniz configuration folder (e.g. `configuration/privileges/`). The module does not require Initializer — the CSV is provided as a convenience for distributions that use it, and iniz safely skips rows that already exist.

#### Required privileges by role

Grant the following privileges to each role that works with the queue. Role names differ between distributions; the UgandaEMR equivalents are noted so instances setting up there know exactly which roles to touch.

| Role | Required privileges | What users of this role can do | UgandaEMR role |
|------|---------------------|--------------------------------|----------------|
| Reception / Registration clerk | `Task: patientqueueing.viewQueue`<br>`Task: patientqueueing.sendPatientToQueue`<br>`Task: patientqueueing.checkIn` | Check patients in (front desk fragment and self check-in fallback), get a ticket number, and see the day's queue | `Reception` |
| Triage / Nurse | `Task: patientqueueing.viewQueue`<br>`Task: patientqueueing.manageQueue` | See waiting patients, call them in, and forward them between rooms | `Triage` |
| Clinician / Provider | `Task: patientqueueing.viewQueue`<br>`Task: patientqueueing.manageQueue`<br>`Task: patientqueueing.completePatientQueue`<br>`App: patientqueueing.providerDashboard` | Run the provider dashboard: call, start serving, skip, cancel, complete consultations | `OPD Clinic` (users are also members of `Organizational: Clinician`) |
| Kiosk / display device account | `Task: patientqueueing.viewQueue`<br>`App: patientqueueing.queueDisplay`<br>`App: patientqueueing.selfCheckIn`<br>`Task: patientqueueing.checkIn` | Unattended accounts for public display screens and self check-in kiosks: view the display feed, look up ticket status, and self check-in | — (create a dedicated account) |
| System administrator | all of the privileges above | Full configuration, monitoring and troubleshooting of the queue | `Organizational: System Administrator` |

Without these grants the corresponding actions fail with a privilege error — most visibly, a Reception role without `sendPatientToQueue` can no longer check patients in after upgrading from an older module version that did not check privileges.

Assign them by editing the roles in **Admin → Roles**, or by adding the privileges to the roles' entries in your iniz `roles/` configuration (e.g. UgandaEMR's `Role_Privilege.xml` metadata).

### 4. Basic Usage

#### Patient Check-in
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/checkin' \
  -d 'patient=a9e39c4d-1234-5678-9abc-def456789012' \
  -d 'locationTo=b1e39c4d-1234-5678-9abc-def456789013' \
  -d 'visitType=d1e39c4d-1234-5678-9abc-def456789015' \
  -d 'queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

#### Non-Patient Check-in
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/selfcheckin' \
  -d 'displayName=Jane Smith' \
  -d 'location=b1e39c4d-1234-5678-9abc-def456789013'
```

#### Queue Display
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/display?location=b1e39c4d-1234-5678-9abc-def456789013'
```

---

## Data Models

### QueueEntry (Unified)
Represents both patient and non-patient queues:

```json
{
  "uuid": "queue-uuid",
  "ticketNumber": "21/04/2026-LOC-001",
  "status": "PENDING",
  "displayName": "John Doe",
  "queueType": "PATIENT",  // or "NON_PATIENT"
  "locationTo": {...},
  "queueRoom": {...},
  "dateCreated": "2026-04-21T10:30:00+00:00",
  "isPatientQueue": true
}
```

### NonPatientQueue
Walk-in visitor queue entries:

```json
{
  "uuid": "queue-uuid",
  "ticketNumber": "21/04/2026-LOC-TYPE-001",
  "displayName": "Jane Smith",
  "phoneNumber": "+1234567890",
  "queueType": {...},  // Concept
  "status": "WAITING",  // WAITING, CALLED, ARRIVED, SERVING, COMPLETED, CANCELLED
  "locationTo": {...},
  "queueRoom": {...}
}
```

---

## Architecture

### Design Principles

1. **Distribution Agnostic**: No hardcoded concepts or identifier types
2. **Configuration over Code**: All behavior configurable via global properties
3. **Interface-Based Design**: Extensible provider assignment strategies
4. **Unified APIs**: Single endpoints for both patient and non-patient queues
5. **RESTful Design**: Standard HTTP methods and status codes

### Key Components

- **PatientQueueingService**: Core service for queue operations
- **ProviderAssignmentService**: Provider auto-assignment
- **QueueEntry**: Polymorphic DTO for unified queue representation
- **CheckInResult**: Result DTO for check-in operations

---

## Related UI Module

The **Patient Queue UI Module** provides a user interface built on the OpenMRS App Framework for managing and visualizing queues:

🔗 [Patient Queue UI GitHub Repository](https://github.com/openmrs/openmrs-module-patientqueueui)

---

## Documentation

- **[API Documentation](docs/API_DOCUMENTATION.md)**: Complete REST API reference
- **[Configuration Guide](docs/CONFIGURATION.md)**: Global properties and setup

---

## Requirements

- OpenMRS 2.8.x or higher (Platform 2.8.x)
- Java 8 or higher
- Maven 3.x

---

## Building from Source

```bash
git clone https://github.com/openmrs/openmrs-module-patientqueueing.git
cd openmrs-module-patientqueueing
mvn clean install
```

The module artifact (`patientqueueing-2.0.2-SNAPSHOT.omod`) will be created in the `omod/target` directory.

---

## Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify
```

Note: REST layer tests are currently disabled pending migration to REST 3.x testing patterns (see PatientQueueControllerTest).

---

## Configuration

See [CONFIGURATION.md](docs/CONFIGURATION.md) for detailed configuration options including:

- Queue type concepts
- Provider assignment strategies
- Identifier type configuration
- Location and room setup

---

## Migration from UgandaEMR

If migrating from UgandaEMR's self check-in module:

1. Configure queue type concepts in OpenMRS dictionary
2. Set up provider assignment strategy global property
3. Configure default visit type UUID (patientqueueing.defaultVisitTypeUuid)
4. Update client code to use new endpoints:
   - UgandaEMR: `/ws/rest/v1/selfcheckinpatient` 
   - Generic: `/ws/rest/v1/patientqueueing/checkin`
   - Booth/Kiosk: `/ws/rest/v1/patientqueueing/selfcheckin` (existing)
5. Note parameter changes: `location` → `locationTo`, add `visitType` parameter
6. Test in staging environment

---

## License

This module is distributed under the [OpenMRS Public License](https://openmrs.org/license/).

---

## Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch
3. Write tests for new features
4. Ensure all tests pass (`mvn clean install`)
5. Submit a pull request

For major changes, please open an issue first to discuss what you would like to change.

---

## Maintainers

This module is maintained by the OpenMRS community. For any issues or feature requests, please file an issue on the GitHub repository.

---

## Changelog

### Version 2.0.2-SNAPSHOT (Current)
- ✅ Added non-patient queue management
- ✅ Added self check-in endpoints (patient and non-patient)
- ✅ Added unified queue display APIs (kiosk, display, statistics)
- ✅ Added provider auto-assignment strategies
- ✅ Distribution-agnostic configuration
- ✅ Polymorphic QueueEntry DTO
- ✅ Enhanced REST API documentation

### Version 2.0.1
- Initial patient queue management
- Basic REST APIs for patient queues
- Care provider dashboard
