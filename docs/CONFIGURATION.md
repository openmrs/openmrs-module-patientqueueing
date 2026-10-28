# Patient Queueing Module Configuration

This document describes the configuration options for the Patient Queueing Module, including self check-in features.

## Global Properties

### Provider Assignment

#### `patientqueueing.providerAssignmentStrategy`

**Description**: Strategy to use when automatically assigning providers to queues

**Valid Values**:
- `leastBusy` - Assign the provider with the fewest active queues (default)
- `roundRobin` - Rotate through available providers in order
- `random` - Randomly assign a provider
- `manual` - No automatic assignment; provider must be specified manually

**Example**:
```properties
patientqueueing.providerAssignmentStrategy=leastBusy
```

---

### Visit Management

#### `patientqueueing.defaultVisitTypeUuid`

**Description**: UUID of the default visit type to use when auto-creating visits

**Valid Values**: Any valid visit type UUID

**Example**:
```properties
patientqueueing.defaultVisitTypeUuid=7b0f5697-27e3-4c28-ab42-4d4b4e9b6f6c
```

---

### Patient Identification

#### `patientqueueing.phoneNumberAttributeTypeUuid`

**Description**: UUID of the person attribute type used for phone number lookup

**Valid Values**: Any valid person attribute type UUID

**Example**:
```properties
patientqueueing.phoneNumberAttributeTypeUuid=14d4f066-15f5-102d-96e4-000c29c2a5d7
```

---

## Setting Global Properties

### Via Admin UI

1. Navigate to **Administration** → **System Administration**
2. Click on **Global Properties**
3. Search for properties starting with `patientqueueing.`
4. Edit and save

### Via SQL

```sql
INSERT INTO global_property (property, property_value, datatype, uuid, description)
VALUES ('patientqueueing.providerAssignmentStrategy', 'leastBusy', 'java.lang.String', UUID(), 'Provider assignment strategy for patient queues');
```

### Via Module Configuration File

Add to your OpenMRS runtime properties file:

```properties
# Provider Assignment
patientqueueing.providerAssignmentStrategy=leastBusy

# Visit Management
patientqueueing.defaultVisitTypeUuid=7b0f5697-27e3-4c28-ab42-4d4b4e9b6f6c

# Patient Identification
patientqueueing.phoneNumberAttributeTypeUuid=14d4f066-15f5-102d-96e4-000c29c2a5d7
```

---

## Provider Assignment Strategies

### Least Busy (Default)

Assigns the provider with the fewest active patient queues.

**Best for**:
- Balancing workload evenly among providers
- Minimizing patient wait times
- High-volume clinics

**Configuration**:
```properties
patientqueueing.providerAssignmentStrategy=leastBusy
```

### Round Robin

Rotates through available providers in order.

**Best for**:
- Fair distribution regardless of queue length
- Predictable assignment patterns
- When all providers have similar capacity

**Configuration**:
```properties
patientqueueing.providerAssignmentStrategy=roundRobin
```

### Random

Randomly selects from available providers.

**Best for**:
- Simple load distribution
- When provider capacity is similar
- Testing and development

**Configuration**:
```properties
patientqueueing.providerAssignmentStrategy=random
```

### Manual

No automatic assignment. Provider must be specified explicitly when creating queues.

**Best for**:
- Complete control over provider assignment
- Specialized provider assignments
- When providers have specific expertise

**Configuration**:
```properties
patientqueueing.providerAssignmentStrategy=manual
```

---

## Patient Identification Configuration

The self check-in feature supports flexible patient identification through multiple identifier sources.

### Supported Identifier Sources

1. **Patient Identifiers**: Primary identifiers, medical record numbers, national IDs, etc.
2. **Person Attributes**: Phone numbers, email addresses, etc.

### Lookup Process

During self check-in, the system searches for patients using configured identifier types and person attributes:

1. **Primary Identifiers**: Searches configured patient identifier type UUIDs
2. **Person Attributes**: Falls back to configured person attributes (e.g., phone number)
3. **Multiple Matches**: Returns error if multiple patients match
4. **No Match**: Returns error if no patient found

---

## Migration from UgandaEMR

If you're migrating from the UgandaEMR module's self check-in feature, update your configuration:

### Before (UgandaEMR)

Hardcoded in code:
- Phone Number attribute UUID: `14d4f066-15f5-102d-96e4-000c29c2a5d7`
- National ID identifier UUID: `f0c16a6d-dc5f-4118-a803-616d0075d282`
- Patient ID identifier UUID: `e1731641-30ab-102d-7a5022ba4115`

### After (Patient Queueing)

Configure via global properties:

```properties
# Patient Identifiers (generic, configurable)
patientqueueing.selfCheckInIdentifierTypeUuids=f0c16a6d-dc5f-4118-a803-616d0075d282,e1731641-30ab-102d-7a5022ba4115
patientqueueing.selfCheckInPersonAttributeTypeUuids=14d4f066-15f5-102d-96e4-000c29c2a5d7
```

### Migration Steps

1. **Update Global Properties**: Configure the new property names
2. **Update API Endpoints**: Change from `/ws/rest/v1/selfcheckinpatient` to `/ws/rest/v1/patientqueueing/checkin`
3. **Test Thoroughly**: Verify patient lookup and queue creation works as expected

---

## Troubleshooting

### Provider Assignment Not Working

**Problem**: Providers are not being assigned automatically

**Solutions**:
1. Check global property: `patientqueueing.providerAssignmentStrategy`
2. Verify providers are associated with the location
3. Ensure providers are not retired
4. Check logs for errors

### Patient Lookup Failing

**Problem**: Patients cannot be found during self check-in

**Solutions**:
1. Verify identifier type UUIDs are correct
2. Check that patients have the specified identifiers
3. Ensure identifiers are not voided
4. Verify person attribute type UUID is correct

---

## API Reference

For detailed API documentation, see:
- [Self Check-in API](docs/API.md#self-check-in)
- [Non-Patient Queue API](docs/API.md#non-patient-queue)
- [Provider Assignment API](docs/API.md#provider-assignment)

---

## Additional Resources

- [OpenMRS Global Properties](https://wiki.openmrs.org/display/docs/Global+Properties)
- [OpenMRS Concept Dictionary](https://wiki.openmrs.org/display/docs/Concept+Source)
- [Patient Queueing Module README](../README.md)
- [Self Check-in Migration Plan](SELF_CHECKIN_MIGRATION_PLAN.md)
