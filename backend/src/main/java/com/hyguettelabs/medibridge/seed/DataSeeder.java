package com.hyguettelabs.medibridge.seed;

import com.hyguettelabs.medibridge.config.MediBridgeProperties;
import com.hyguettelabs.medibridge.domain.entity.*;
import com.hyguettelabs.medibridge.domain.enums.*;
import com.hyguettelabs.medibridge.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final MediBridgeProperties properties;
    private final UserAccountRepository users;
    private final PatientProfileRepository patients;
    private final ClinicianProfileRepository clinicians;
    private final FacilityRepository facilities;
    private final AvailabilitySlotRepository slots;
    private final AppointmentRepository appointments;
    private final ConsentRecordRepository consents;
    private final CareNoteRepository notes;
    private final AppNotificationRepository notifications;
    private final AuditLogRepository auditLogs;
    private final PasswordEncoder encoder;

    public DataSeeder(MediBridgeProperties properties,
                      UserAccountRepository users,
                      PatientProfileRepository patients,
                      ClinicianProfileRepository clinicians,
                      FacilityRepository facilities,
                      AvailabilitySlotRepository slots,
                      AppointmentRepository appointments,
                      ConsentRecordRepository consents,
                      CareNoteRepository notes,
                      AppNotificationRepository notifications,
                      AuditLogRepository auditLogs,
                      PasswordEncoder encoder) {
        this.properties = properties;
        this.users = users;
        this.patients = patients;
        this.clinicians = clinicians;
        this.facilities = facilities;
        this.slots = slots;
        this.appointments = appointments;
        this.consents = consents;
        this.notes = notes;
        this.notifications = notifications;
        this.auditLogs = auditLogs;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.getSeed().isEnabled() || users.count() > 0) {
            return;
        }
        String password = properties.getSeed().getPassword();

        Facility clinic = facility("Kigali Wellness Clinic", FacilityType.CLINIC, "KN 5 Ave", "Kigali");
        Facility community = facility("Nyamirambo Community Desk", FacilityType.COMMUNITY, "Nyamirambo", "Kigali");

        UserAccount admin = user("admin@medibridge.local", password, Role.ADMIN, "Amina", "Niyonzima");
        UserAccount clinician = user("clinician@medibridge.local", password, Role.CLINICIAN, "Jean", "Mutabazi");
        UserAccount patient = user("patient@medibridge.local", password, Role.PATIENT, "Grace", "Uwase");

        ClinicianProfile clinicianProfile = new ClinicianProfile();
        clinicianProfile.setUser(clinician);
        clinicianProfile.setFacility(clinic);
        clinicianProfile.setSpecialty("Family medicine");
        clinicianProfile.setCredentials("MD (demo)");
        clinicianProfile.setBio("Coordination desk clinician for educational visits — not a practising licence.");
        clinicians.save(clinicianProfile);

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(patient);
        patientProfile.setCity("Kigali");
        patientProfile.setPreferredLanguage("English");
        patientProfile.setDateOfBirth(LocalDate.of(1994, 4, 12));
        patientProfile.setEmergencyContactName("Diane Uwase");
        patients.save(patientProfile);

        Instant start = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        AvailabilitySlot booked = slot(clinician, clinic, start, start.plus(30, ChronoUnit.MINUTES), true);
        slot(clinician, clinic, start.plus(2, ChronoUnit.HOURS), start.plus(150, ChronoUnit.MINUTES), false);
        slot(clinician, community, start.plus(1, ChronoUnit.DAYS), start.plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES), false);

        Appointment visit = new Appointment();
        visit.setPatient(patient);
        visit.setClinician(clinician);
        visit.setFacility(clinic);
        visit.setSlot(booked);
        visit.setStatus(AppointmentStatus.BOOKED);
        visit.setReason("Coordination check-in");
        appointments.save(visit);

        ConsentRecord consent = new ConsentRecord();
        consent.setPatient(patient);
        consent.setClinician(clinician);
        consent.setStatus(ConsentStatus.GRANTED);
        consent.setGrantedAt(Instant.now().minus(3, ChronoUnit.DAYS));
        consents.save(consent);

        CareNote note = new CareNote();
        note.setPatient(patient);
        note.setClinician(clinician);
        note.setAppointment(visit);
        note.setBody("Educational coordination note: visit scheduled. Not a diagnosis or treatment plan.");
        notes.save(note);

        notify(patient, NotificationType.APPOINTMENT, "Visit confirmed",
                "Your check-in at Kigali Wellness Clinic is booked.");
        notify(clinician, NotificationType.APPOINTMENT, "New visit booked",
                "Grace Uwase booked a coordination visit.");
        notify(patient, NotificationType.ACCESS, "Record accessed",
                "Jean Mutabazi viewed your coordination profile (seeded demo).");

        auditLogs.save(AuditLog.of(clinician.getEmail(), Role.CLINICIAN.name(), "VIEW_PATIENT",
                "PatientProfile", patient.getId().toString(), patient.getId(), "seeded demo access"));
        auditLogs.save(AuditLog.of(clinician.getEmail(), Role.CLINICIAN.name(), "WRITE_NOTE",
                "CareNote", note.getId().toString(), patient.getId(), "clinician wrote a care note (body not stored in audit)"));
        auditLogs.save(AuditLog.of(patient.getEmail(), Role.PATIENT.name(), "CONSENT_GRANT",
                "ConsentRecord", consent.getId().toString(), patient.getId(), "granted to " + clinician.getEmail()));
        auditLogs.save(AuditLog.of(admin.getEmail(), Role.ADMIN.name(), "CREATE_FACILITY",
                "Facility", clinic.getId().toString(), null, clinic.getName()));

        log.info("Seeded demo accounts patient/clinician/admin @medibridge.local (password from APP_SEED_PASSWORD).");
    }

    private UserAccount user(String email, String password, Role role, String first, String last) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setFirstName(first);
        user.setLastName(last);
        user.setPhone("+250780000000");
        return users.save(user);
    }

    private Facility facility(String name, FacilityType type, String address, String city) {
        Facility facility = new Facility();
        facility.setName(name);
        facility.setType(type);
        facility.setAddress(address);
        facility.setCity(city);
        facility.setPhone("+250788000000");
        facility.setActive(true);
        return facilities.save(facility);
    }

    private AvailabilitySlot slot(UserAccount clinician, Facility facility, Instant start, Instant end, boolean booked) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setClinician(clinician);
        slot.setFacility(facility);
        slot.setStartsAt(start);
        slot.setEndsAt(end);
        slot.setBooked(booked);
        return slots.save(slot);
    }

    private void notify(UserAccount recipient, NotificationType type, String title, String body) {
        AppNotification n = new AppNotification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        notifications.save(n);
    }
}
