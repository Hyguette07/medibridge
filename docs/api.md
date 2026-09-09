# MediBridge — HTTP API

Base path: `/api/v1`  
Auth: `Authorization: Bearer <jwt>`  
Interactive docs: `/swagger-ui.html` (springdoc)

Envelope:

```json
{ "success": true, "message": "ok", "data": {}, "timestamp": "2026-09-09T10:00:00Z" }
```

Errors use the same envelope with `success: false` and an `error` code.

**Disclaimer:** this API coordinates visits. It is educational software — not a medical device, not a doctor, and not clinical advice.

## Auth

| Method | Path | Access |
| --- | --- | --- |
| POST | `/auth/register` | Public (creates `PATIENT`) |
| POST | `/auth/login` | Public |
| POST | `/auth/logout` | Authenticated |
| GET | `/auth/me` | Authenticated |

## Facilities

| Method | Path | Access |
| --- | --- | --- |
| GET | `/facilities` | Authenticated |
| POST | `/facilities` | ADMIN |
| PUT | `/facilities/{id}` | ADMIN |

## Slots and booking

| Method | Path | Access |
| --- | --- | --- |
| GET | `/slots` | Authenticated (`openOnly`, `clinicianId`, `facilityId`) |
| POST | `/slots` | CLINICIAN |
| DELETE | `/slots/{id}` | CLINICIAN owner |
| POST | `/slots/{id}/book` | PATIENT |

## Appointments

| Method | Path | Access |
| --- | --- | --- |
| GET | `/appointments` | Own visits (admin: all) |
| POST | `/appointments/{id}/cancel` | Patient, clinician, or admin |

## Dashboard, directory

| Method | Path | Access |
| --- | --- | --- |
| GET | `/dashboard` | Authenticated (shape depends on role) |
| GET | `/clinicians` | Authenticated |
| GET | `/patients` | CLINICIAN (consented only) |
| GET | `/patients/{id}` | Self, admin, or clinician with consent (audited) |

## Consents

| Method | Path | Access |
| --- | --- | --- |
| GET | `/consents` | Own grants / grants to clinician |
| POST | `/consents` | PATIENT grant |
| POST | `/consents/{id}/revoke` | PATIENT owner |

## Notes

| Method | Path | Access |
| --- | --- | --- |
| GET | `/notes?patientId=` | CLINICIAN with active consent (audited) |
| POST | `/notes` | CLINICIAN with active consent (audited) |

Care notes are never returned on patient endpoints.

## Notifications

| Method | Path | Access |
| --- | --- | --- |
| GET | `/notifications` | Authenticated |
| PATCH | `/notifications/{id}/read` | Owner |

## Audit

| Method | Path | Access |
| --- | --- | --- |
| GET | `/me/audit` | PATIENT |
| GET | `/patients/{id}/audit` | Clinician with consent, or admin |
| GET | `/audit` | ADMIN |

## Admin

| Method | Path | Access |
| --- | --- | --- |
| GET | `/admin/users` | ADMIN |
| PATCH | `/admin/users/{id}` | ADMIN (`enabled`) |

## Status codes

`200`, `201`, `400` validation, `401`, `403` (including missing consent), `404`, `409` unique/conflict, `429` auth rate limit.
