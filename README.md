# MediBridge

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![Next.js](https://img.shields.io/badge/Next.js-14-black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

Privacy-first appointment coordination. Every PHI-adjacent read and write is **audit-logged**. Patients see an **access timeline** of who opened their record. Clinicians cannot read care notes without **consent**.

**This is educational software — not a medical device, not a doctor, and not clinical advice.**

## Problem

Patients book visits without knowing who later opened their file. Clinics coordinate appointments in tools that hide access. Consent is implied instead of recorded.

## Solution

MediBridge is a coordination desk, not a charting system. A patient books a slot, grants or revokes clinician consent, and reads an audit timeline. `PrivacyGuard` plus `AccessAuditService` make the rule explicit: accessing a patient record writes an audit row; notes stay clinician-only.

## Features

- JWT auth with roles: Patient (self-register), Clinician, Admin
- Facilities, clinician availability, book and cancel visits
- Patient dashboard of upcoming visits
- Clinician desk with consented patients and private care notes
- Admin facility and user management
- In-app notifications for bookings, consent, and record access
- Patient audit timeline (`GET /me/audit`) and consented clinician view (`GET /patients/{id}/audit`)
- OpenAPI at `/swagger-ui.html`

## Architecture

See [docs/architecture.md](./docs/architecture.md).

```
Next.js UI  →  Spring Boot API  →  PostgreSQL (Flyway) or H2 (local)
```

## Technology stack

Frontend: Next.js, React, TypeScript, Tailwind CSS  
Backend: Java 17, Spring Boot, Spring Security, JPA, Flyway, springdoc  
Data: PostgreSQL (production), H2 (local demo profile)  
Delivery: Docker Compose, GitHub Actions

## Database

[docs/database.md](./docs/database.md)

## API documentation

[docs/api.md](./docs/api.md) and interactive Swagger when the API is running.

## Screenshots

Add captures under `screenshots/` and link them here after first run.

## Installation

```bash
cp .env.example .env
```

Java 17 and Node 22 are required. PostgreSQL is required for the `postgres` profile; the `local` profile uses H2 so you can run the API without Docker.

## Environment variables

See `.env.example`. Never commit `.env`. Demo seed password is `APP_SEED_PASSWORD` (default `ChangeMe123!`).

Seeded accounts (local/demo only):

| Role | Email | Password |
| --- | --- | --- |
| Patient | patient@medibridge.local | `APP_SEED_PASSWORD` |
| Clinician | clinician@medibridge.local | `APP_SEED_PASSWORD` |
| Admin | admin@medibridge.local | `APP_SEED_PASSWORD` |

## Running locally

API (H2, port 8084):

```bash
cd backend
mvn spring-boot:run
```

UI (port 3004):

```bash
cd frontend
cp ../.env.example .env.local
npm install
npm run dev
```

Open http://localhost:3004 — log in as `patient@medibridge.local`.

## Testing

```bash
cd backend && mvn test
cd frontend && npm test -- --run
```

See [tests/README.md](./tests/README.md).

## Docker

```bash
# Fill DB_PASSWORD and JWT_SECRET in .env first
docker compose up --build
```

## Deployment

[docs/deployment.md](./docs/deployment.md)

## Future improvements

Refresh-token rotation, SMS visit reminders, finer-grained consent scopes, and an export of the patient's own audit timeline as a signed PDF.

## Lessons learned

[docs/lessons-learned.md](./docs/lessons-learned.md)

## Author

Isimbi Hyguette — Hyguette Labs  
isimbihyguette07@gmail.com
