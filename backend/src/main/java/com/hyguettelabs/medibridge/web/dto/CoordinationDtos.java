package com.hyguettelabs.medibridge.web.dto;

import com.hyguettelabs.medibridge.domain.enums.AppointmentStatus;
import com.hyguettelabs.medibridge.domain.enums.ConsentStatus;
import com.hyguettelabs.medibridge.domain.enums.FacilityType;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CoordinationDtos {
    private CoordinationDtos() {}

    public record FacilityRequest(
            @NotBlank @Size(max = 160) String name,
            @NotNull FacilityType type,
            @Size(max = 200) String address,
            @Size(max = 80) String city,
            @Size(max = 32) String phone,
            Boolean active
    ) {}

    public record FacilityResponse(
            UUID id,
            String name,
            FacilityType type,
            String address,
            String city,
            String phone,
            boolean active
    ) {}

    public record SlotRequest(
            @NotNull UUID facilityId,
            @NotNull Instant startsAt,
            @NotNull Instant endsAt
    ) {}

    public record SlotResponse(
            UUID id,
            UUID clinicianId,
            String clinicianName,
            UUID facilityId,
            String facilityName,
            Instant startsAt,
            Instant endsAt,
            boolean booked
    ) {}

    public record BookRequest(@Size(max = 200) String reason) {}

    public record AppointmentResponse(
            UUID id,
            UUID patientId,
            String patientName,
            UUID clinicianId,
            String clinicianName,
            UUID facilityId,
            String facilityName,
            UUID slotId,
            Instant startsAt,
            Instant endsAt,
            AppointmentStatus status,
            String reason,
            Instant createdAt
    ) {}

    public record ConsentGrantRequest(@NotNull UUID clinicianId) {}

    public record ConsentResponse(
            UUID id,
            UUID patientId,
            String patientName,
            UUID clinicianId,
            String clinicianName,
            ConsentStatus status,
            Instant grantedAt,
            Instant revokedAt
    ) {}

    public record NoteRequest(
            @NotNull UUID patientId,
            UUID appointmentId,
            @NotBlank @Size(max = 4000) String body
    ) {}

    public record NoteResponse(
            UUID id,
            UUID patientId,
            String patientName,
            UUID clinicianId,
            String clinicianName,
            UUID appointmentId,
            String body,
            Instant createdAt,
            String disclaimer
    ) {}

    public record NotificationResponse(
            UUID id,
            NotificationType type,
            String title,
            String body,
            boolean read,
            Instant createdAt
    ) {}

    public record AuditEntryResponse(
            UUID id,
            String actorEmail,
            String actorRole,
            String action,
            String entityType,
            String entityId,
            UUID subjectPatientId,
            String detail,
            Instant createdAt
    ) {}

    public record ClinicianResponse(
            UUID userId,
            String firstName,
            String lastName,
            String email,
            String specialty,
            String credentials,
            String bio,
            UUID facilityId,
            String facilityName
    ) {}

    public record PatientSummaryResponse(
            UUID userId,
            String firstName,
            String lastName,
            String email,
            String city,
            String preferredLanguage,
            LocalDate dateOfBirth,
            boolean consentActive
    ) {}

    public record PatientDashboard(
            String disclaimer,
            List<AppointmentResponse> upcoming,
            long unreadNotifications,
            long recentAccessCount,
            long activeConsents
    ) {}

    public record ClinicianDashboard(
            String disclaimer,
            List<AppointmentResponse> todayAppointments,
            long openSlotCount,
            long consentedPatientCount,
            long unreadNotifications
    ) {}

    public record AdminDashboard(
            String disclaimer,
            long userCount,
            long facilityCount,
            long appointmentCount,
            long consentCount
    ) {}

    public record UserAdminRow(
            UUID id,
            String email,
            String role,
            String firstName,
            String lastName,
            boolean enabled
    ) {}

    public record UserEnabledRequest(boolean enabled) {}
}
