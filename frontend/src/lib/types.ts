export type Role = "PATIENT" | "CLINICIAN" | "ADMIN";

export const DISCLAIMER =
  "This is educational software — not a medical device, not a doctor, and not clinical advice.";

export type AuthUser = {
  token: string;
  userId: string;
  email: string;
  role: Role;
  firstName: string;
  lastName: string;
  disclaimer?: string;
};

export type Facility = {
  id: string;
  name: string;
  type: string;
  address?: string;
  city?: string;
  phone?: string;
  active: boolean;
};

export type Slot = {
  id: string;
  clinicianId: string;
  clinicianName: string;
  facilityId: string;
  facilityName: string;
  startsAt: string;
  endsAt: string;
  booked: boolean;
};

export type Appointment = {
  id: string;
  patientId: string;
  patientName: string;
  clinicianId: string;
  clinicianName: string;
  facilityId: string;
  facilityName: string;
  slotId: string;
  startsAt: string;
  endsAt: string;
  status: string;
  reason?: string;
  createdAt: string;
};

export type Consent = {
  id: string;
  patientId: string;
  patientName: string;
  clinicianId: string;
  clinicianName: string;
  status: "GRANTED" | "REVOKED";
  grantedAt: string;
  revokedAt?: string;
};

export type AuditEntry = {
  id: string;
  actorEmail: string;
  actorRole?: string;
  action: string;
  entityType?: string;
  entityId?: string;
  subjectPatientId?: string;
  detail?: string;
  createdAt: string;
};

export type Clinician = {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  specialty: string;
  credentials?: string;
  bio?: string;
  facilityId?: string;
  facilityName?: string;
};

export type PatientSummary = {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  city?: string;
  preferredLanguage?: string;
  dateOfBirth?: string;
  consentActive: boolean;
};

export type CareNote = {
  id: string;
  patientId: string;
  patientName: string;
  clinicianId: string;
  clinicianName: string;
  appointmentId?: string;
  body: string;
  createdAt: string;
  disclaimer: string;
};

export type PatientDashboard = {
  disclaimer: string;
  upcoming: Appointment[];
  unreadNotifications: number;
  recentAccessCount: number;
  activeConsents: number;
};

export type ClinicianDashboard = {
  disclaimer: string;
  todayAppointments: Appointment[];
  openSlotCount: number;
  consentedPatientCount: number;
  unreadNotifications: number;
};

export type AdminDashboard = {
  disclaimer: string;
  userCount: number;
  facilityCount: number;
  appointmentCount: number;
  consentCount: number;
};

export type NotificationItem = {
  id: string;
  type: string;
  title: string;
  body: string;
  read: boolean;
  createdAt: string;
};
