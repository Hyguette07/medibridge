package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.Appointment;
import com.hyguettelabs.medibridge.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<Appointment> findByClinicianIdOrderByCreatedAtDesc(UUID clinicianId);
    long countByStatus(AppointmentStatus status);
}
