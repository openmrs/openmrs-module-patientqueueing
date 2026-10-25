# Patient Queueing Module - REST API Documentation

**Version**: 3.0.*
**Base Path**: `/ws/rest/v1`

---

## Table of Contents

1. [Check-in APIs](#check-in-apis)
2. [Queue Display APIs](#queue-display-apis)
3. [Non-Patient Queue Management](#non-patient-queue-management)
4. [Data Models](#data-models)
5. [Error Responses](#error-responses)

---

## Check-in APIs

### Generic Check-in

Generic patient check-in with configurable visit type. Supports provider auto-assignment, queue position calculation, and estimated wait time.

**Endpoint**: `POST /ws/rest/v1/patientqueueing/checkin`

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patient | string | Yes | Patient UUID |
| locationTo | string | Yes | Destination Location UUID |
| visitType | string | No | Visit type UUID (overrides default) |
| queueRoom | string | No | Queue room UUID |
| provider | string | No | Provider UUID (skips auto-assignment) |
| priority | int | No | Priority level |
| comment | string | No | Comment for the queue entry |
| autoAssignProvider | boolean | No | Override auto-assignment setting |

**Response**: `CheckInResult`
```json
{
  "patient": {
    "uuid": "patient-uuid",
    "display": "John Doe"
  },
  "visit": null,
  "queueEntry": {
    "uuid": "queue-uuid",
    "ticketNumber": "21/04/2026-LOC-001",
    "status": "PENDING",
    "displayName": "John Doe",
    "queueType": "PATIENT",
    "locationTo": {
      "uuid": "location-uuid",
      "display": "Outpatient Clinic"
    },
    "queueRoom": {
      "uuid": "room-uuid",
      "display": "Room 1"
    },
    "dateCreated": "2026-04-21T10:30:00+00:00",
    "priority": null,
    "comment": null,
    "isPatientQueue": true
  },
  "ticketNumber": "21/04/2026-LOC-001",
  "estimatedWaitMinutes": 25,
  "queuePosition": 5,
  "success": true,
  "errorMessage": null
}
```

**Example**:
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/checkin' \
  -d 'patient=a9e39c4d-1234-5678-9abc-def456789012' \
  -d 'locationTo=b1e39c4d-1234-5678-9abc-def456789013' \
  -d 'visitType=d1e39c4d-1234-5678-9abc-def456789015' \
  -d 'queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

### Self Check-in (Booth/Kiosk)

Self check-in endpoint for booth/kiosk scenarios. Simplified interface for self-service check-in.

**Endpoint**: `GET /ws/rest/v1/patientqueueing/selfcheckin`

**Example**:
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/selfcheckin' \
  -d 'displayName=Jane Smith' \
  -d 'phoneNumber=+1234567890' \
  -d 'location=b1e39c4d-1234-5678-9abc-def456789013' \
  -d 'queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

**Response**: `CheckInResult`
```json
{
  "patient": null,
  "visit": null,
  "queueEntry": {
    "uuid": "queue-uuid",
    "ticketNumber": "21/04/2026-LOC-TYPE-001",
    "status": "WAITING",
    "displayName": "Jane Smith",
    "queueType": "NON_PATIENT",
    "locationTo": {
      "uuid": "location-uuid",
      "display": "Outpatient Clinic"
    },
    "queueRoom": {
      "uuid": "room-uuid",
      "display": "Room 1"
    },
    "dateCreated": "2026-04-21T10:30:00+00:00",
    "priority": null,
    "comment": null,
    "isPatientQueue": false
  },
  "ticketNumber": "21/04/2026-LOC-TYPE-001",
  "estimatedWaitMinutes": 10,
  "queuePosition": 2,
  "success": true,
  "errorMessage": null
}
```

---

### Non-Patient Check-in

Create a non-patient queue entry (for visitors, companions, etc.).

**Endpoint**: `POST /ws/rest/v1/patientqueueing/nonpatientqueue`

**Example**:
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/nonpatientqueue' \
  -d 'displayName=Jane Smith' \
  -d 'phoneNumber=+1234567890' \
  -d 'locationTo=b1e39c4d-1234-5678-9abc-def456789013' \
  -d 'queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

---

## Queue Display APIs

### Get Queue by Ticket Number

Look up a queue entry by ticket number. Searches both patient and non-patient queues.

**Endpoint**: `GET /ws/rest/v1/kiosk`

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| ticketNumber | string | Yes | Ticket number to search |
| dateFrom | long | No | Start date (milliseconds, default: start of today) |
| dateTo | long | No | End date (milliseconds, default: end of today) |

**Response**: `QueueEntry`
```json
{
  "uuid": "queue-uuid",
  "ticketNumber": "21/04/2026-LOC-001",
  "status": "PENDING",
  "displayName": "John Doe",
  "queueType": "PATIENT",
  "locationTo": {
    "uuid": "location-uuid",
    "display": "Outpatient Clinic"
  },
  "queueRoom": {
    "uuid": "room-uuid",
    "display": "Room 1"
  },
  "dateCreated": "2026-04-21T10:30:00+00:00",
  "priority": null,
  "comment": null,
  "isPatientQueue": true
}
```

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/kiosk?ticketNumber=21/04/2026-LOC-001'
```

### Get All Queue Entries

Get all queue entries for a location, including both patient and non-patient queues.

**Endpoint**: `GET /ws/rest/v1/kiosk`

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| location | string | No | Filter by location UUID |
| queueRoom | string | No | Filter by queue room UUID |
| status | string | No | Filter by status (PENDING, WAITING, PICKED, etc.) |
| dateFrom | long | No | Start date (milliseconds) |
| dateTo | long | No | End date (milliseconds) |

**Response**: Array of `QueueEntry`
```json
[
  {
    "uuid": "queue-uuid-1",
    "ticketNumber": "21/04/2026-LOC-001",
    "status": "PENDING",
    "displayName": "John Doe",
    "queueType": "PATIENT",
    "locationTo": {...},
    "queueRoom": {...},
    "dateCreated": "2026-04-21T10:30:00+00:00",
    "isPatientQueue": true
  },
  {
    "uuid": "queue-uuid-2",
    "ticketNumber": "21/04/2026-LOC-TYPE-001",
    "status": "WAITING",
    "displayName": "Jane Smith",
    "queueType": "NON_PATIENT",
    "locationTo": {...},
    "queueRoom": {...},
    "dateCreated": "2026-04-21T10:31:00+00:00",
    "isPatientQueue": false
  }
]
```

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/kiosk?location=b1e39c4d-1234-5678-9abc-def456789013&status=PENDING'
```

### Get Queue Display Data

Get display data for public queue monitors, including "now serving" and "up next" information.

**Endpoint**: `GET /ws/rest/v1/display`

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| location | string | No | Filter by location UUID |
| queueRoom | string | No | Filter by queue room UUID |
| dateFrom | long | No | Start date (milliseconds) |
| dateTo | long | No | End date (milliseconds) |

**Response**:
```json
{
  "location": {
    "uuid": "location-uuid",
    "display": "Outpatient Clinic"
  },
  "queueRoom": {
    "uuid": "room-uuid",
    "display": "Room 1"
  },
  "nowServing": {
    "uuid": "queue-uuid",
    "ticketNumber": "21/04/2026-LOC-001",
    "status": "PICKED",
    "displayName": "John Doe",
    "queueType": "PATIENT",
    "calledAt": "2026-04-21T10:45:00+00:00"
  },
  "upNext": [
    {
      "uuid": "queue-uuid-2",
      "ticketNumber": "21/04/2026-LOC-002",
      "status": "PENDING",
      "displayName": "Jane Smith",
      "queueType": "PATIENT"
    },
    {
      "uuid": "queue-uuid-3",
      "ticketNumber": "21/04/2026-LOC-TYPE-001",
      "status": "WAITING",
      "displayName": "Bob Johnson",
      "queueType": "NON_PATIENT"
    }
  ],
  "statistics": {
    "patientWaiting": 8,
    "patientServing": 2,
    "patientCompleted": 45,
    "nonPatientWaiting": 3,
    "nonPatientServing": 1,
    "nonPatientCompleted": 12,
    "totalWaiting": 11,
    "totalServing": 3,
    "totalCompleted": 57
  },
  "lastUpdated": "2026-04-21T10:50:00+00:00"
}
```

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/display?location=b1e39c4d-1234-5678-9abc-def456789013'
```

### Get Queue Statistics

Get queue statistics for a location.

**Endpoint**: `GET /ws/rest/v1/patientqueueing/display`

**Parameters**: Same as display data

**Response**: `statistics` object (see above)

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/display?location=b1e39c4d-1234-5678-9abc-def456789013'
```

---

## Non-Patient Queue Management

### List All Non-Patient Queues

**Endpoint**: `GET /ws/rest/v1/nonpatientqueue`

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | string | No | Filter by status (WAITING, CALLED, ARRIVED, SERVING, COMPLETED, CANCELLED) |
| queueType | string | No | Filter by queue type Concept UUID |
| locationTo | string | No | Filter by destination location UUID |
| queueRoom | string | No | Filter by queue room UUID |
| fromDate | long | No | Start date (milliseconds) |
| toDate | long | No | End date (milliseconds) |

**Response**: Paged list of `NonPatientQueue`

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue?status=WAITING&queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

### Get Non-Patient Queue by UUID

**Endpoint**: `GET /ws/rest/v1/nonpatientqueue/:uuid`

**Response**: `NonPatientQueue` (full representation)

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue/a1e39c4d-1234-5678-9abc-def456789015'
```

### Create Non-Patient Queue

**Endpoint**: `POST /ws/rest/v1/nonpatientqueue`

**Request Body**:
```json
{
  "displayName": "Jane Smith",
  "phoneNumber": "+1234567890",
  "queueType": "concept-uuid",
  "currentLocation": "location-uuid",
  "locationTo": "location-uuid",
  "queueRoom": "room-uuid",
  "priority": 1,
  "comment": "Urgent case"
}
```

**Response**: Created `NonPatientQueue`

**Example**:
```bash
curl -X POST \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue' \
  -d 'displayName=Jane Smith' \
  -d 'phoneNumber=+1234567890' \
  -d 'locationTo=b1e39c4d-1234-5678-9abc-def456789013' \
  -d 'queueRoom=c1e39c4d-1234-5678-9abc-def456789014'
```

### Update Non-Patient Queue

**Endpoint**: `PUT /ws/rest/v1/nonpatientqueue/:uuid`

**Request Body** (all fields optional):
```json
{
  "displayName": "Jane Smith Updated",
  "phoneNumber": "+0987654321",
  "priority": 2,
  "comment": "Updated priority"
}
```

**Response**: Updated `NonPatientQueue`

**Example**:
```bash
curl -X PUT \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue/a1e39c4d-1234-5678-9abc-def456789015' \
  -d 'priority=2' \
  -d 'comment=Updated priority'
```

### Search Non-Patient Queues

**Endpoint**: `GET /ws/rest/v1/nonpatientqueue?q=search`

**Search Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| ticketNumber | string | Search by ticket number |
| queueRoom | string | Search by queue room UUID |
| status | string | Filter by status |
| fromDate | long | Start date (milliseconds) |
| toDate | long | End date (milliseconds) |

**Response**: Paged list of matching `NonPatientQueue`

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue?q=search&ticketNumber=21/04/2026-LOC-TYPE-001'
```

---

## Data Models

### QueueEntry

Unified representation of both patient and non-patient queues.

| Field | Type | Description |
|-------|------|-------------|
| uuid | string | Queue entry UUID |
| ticketNumber | string | Unique ticket/visit number |
| status | string | Current status |
| displayName | string | Human-readable name |
| queueType | string | "PATIENT" or "NON_PATIENT" |
| locationTo | Location | Destination location |
| queueRoom | Location | Queue room location |
| dateCreated | Date | Creation timestamp |
| priority | Integer | Priority level (optional) |
| comment | string | Additional notes (optional) |
| isPatientQueue | Boolean | True if patient queue |

### CheckInResult

Result of a check-in operation.

| Field | Type | Description |
|-------|------|-------------|
| patient | Patient | Patient (for patient check-in) |
| visit | Visit | Created visit (future) |
| queueEntry | QueueEntry | Created queue entry |
| ticketNumber | string | Assigned ticket number |
| estimatedWaitMinutes | Integer | Estimated wait time |
| queuePosition | Integer | Position in queue |
| success | Boolean | True if successful |
| errorMessage | string | Error message if failed |

### NonPatientQueue

Non-patient queue entry.

| Field | Type | Description |
|-------|------|-------------|
| uuid | string | Queue UUID |
| ticketNumber | string | Unique ticket number |
| displayName | string | Display name |
| phoneNumber | string | Contact phone |
| queueType | Concept | Queue type concept |
| currentLocation | Location | Current location |
| locationTo | Location | Destination location |
| queueRoom | Location | Queue room |
| status | enum | WAITING, CALLED, ARRIVED, SERVING, COMPLETED, CANCELLED |
| priority | Integer | Priority level |
| comment | string | Notes |
| calledBy | Provider | Provider who called |
| calledAt | Date | Call timestamp |
| arrivedAt | Date | Arrival timestamp |
| servedBy | Provider | Serving provider |
| startedAt | Date | Service start timestamp |
| endedAt | Date | End timestamp |

---

## Error Responses

### Validation Error (400)

```json
{
  "error": {
    "message": "patient parameter is required",
    "code": "validation.error"
  }
}
```

### Not Found (404)

```json
{
  "error": {
    "message": "Patient not found",
    "code": "not.found"
  }
}
```

### Server Error (500)

```json
{
  "error": {
    "message": "Check-in failed: Database connection error",
    "code": "server.error"
  }
}
```

---

## Representations

All resources support multiple representations:

- **default** (ref): Basic fields with references
- **full**: All fields including complete objects

Use `v=full` parameter:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue/:uuid?v=full'
```

---

## Pagination

List endpoints support pagination:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| startIndex | int | 0 | Starting index |
| limit | int | 100 | Max results per page |

**Example**:
```bash
curl -X GET \
  'http://localhost:8080/openmrs/ws/rest/v1/nonpatientqueue?startIndex=0&limit=20'
```

---

## Authentication

All endpoints require valid OpenMRS authentication. Include session cookie or basic auth headers:

```bash
# Session authentication
curl -b 'JSESSIONID=your-session-id' \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/kiosk'

# Basic authentication
curl -u 'admin:Admin123' \
  'http://localhost:8080/openmrs/ws/rest/v1/patientqueueing/kiosk'
```

---

## Rate Limiting

No rate limiting is currently enforced. Clients should implement reasonable polling intervals (recommended: 5-10 seconds for display updates).

---

## Best Practices

1. **Always handle errors**: Check the `success` field in CheckInResult
2. **Use polling for displays**: Don't poll more frequently than every 5 seconds
3. **Filter by location**: Always provide location/queueRoom filters when possible
4. **Use date ranges**: Specify dateFrom/dateTo for better performance
5. **Handle queue types**: Be prepared for both PATIENT and NON_PATIENT entries
6. **Respect priority**: Higher priority numbers = higher priority (if used)
7. **Cache display data**: Queue displays can cache data for 5-10 seconds

---

## Changelog

### Version 2.0.2-SNAPSHOT
- ✅ Added self check-in endpoints
- ✅ Added unified queue display APIs
- ✅ Added non-patient queue management
- ✅ Added provider auto-assignment strategies
- ✅ Distribution-agnostic configuration

### Version 2.0.1
- Initial patient queue management
- Basic REST APIs for patient queues
