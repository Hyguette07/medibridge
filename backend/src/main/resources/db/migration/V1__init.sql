-- MediBridge PostgreSQL schema (used with SPRING_PROFILES_ACTIVE=postgres)

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    phone VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    token_version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT users_role_chk CHECK (role IN ('PATIENT', 'CLINICIAN', 'ADMIN'))
);

CREATE TABLE patient_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    date_of_birth DATE,
    preferred_language VARCHAR(40),
    city VARCHAR(80),
    emergency_contact_name VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE facilities (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(32) NOT NULL,
    address VARCHAR(200),
    city VARCHAR(80),
    phone VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT facilities_type_chk CHECK (type IN ('CLINIC', 'HOSPITAL', 'COMMUNITY'))
);

CREATE TABLE clinician_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    facility_id UUID REFERENCES facilities (id),
    specialty VARCHAR(80) NOT NULL,
    credentials VARCHAR(80),
    bio VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE availability_slots (
    id UUID PRIMARY KEY,
    clinician_id UUID NOT NULL REFERENCES users (id),
    facility_id UUID NOT NULL REFERENCES facilities (id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    booked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX slots_clinician_start_idx ON availability_slots (clinician_id, starts_at);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES users (id),
    clinician_id UUID NOT NULL REFERENCES users (id),
    facility_id UUID NOT NULL REFERENCES facilities (id),
    slot_id UUID NOT NULL UNIQUE REFERENCES availability_slots (id),
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT appointments_status_chk CHECK (status IN ('BOOKED', 'CANCELLED', 'COMPLETED'))
);
CREATE INDEX appt_patient_idx ON appointments (patient_id);
CREATE INDEX appt_clinician_idx ON appointments (clinician_id);

CREATE TABLE care_notes (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES users (id),
    clinician_id UUID NOT NULL REFERENCES users (id),
    appointment_id UUID REFERENCES appointments (id),
    body VARCHAR(4000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX notes_patient_idx ON care_notes (patient_id);

CREATE TABLE consent_records (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES users (id),
    clinician_id UUID NOT NULL REFERENCES users (id),
    status VARCHAR(32) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT consent_pair_uq UNIQUE (patient_id, clinician_id),
    CONSTRAINT consent_status_chk CHECK (status IN ('GRANTED', 'REVOKED'))
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX notif_user_read_idx ON notifications (recipient_id, read_flag);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_email VARCHAR(255),
    actor_role VARCHAR(32),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80),
    entity_id VARCHAR(64),
    subject_patient_id UUID,
    detail VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX audit_subject_idx ON audit_logs (subject_patient_id, created_at);
