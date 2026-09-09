package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Component;

/**
 * Memorable access gate: clinicians never see care notes without an active consent.
 * Patients never receive notes. Admin does not read clinical text.
 */
@Component
public class PrivacyGuard {

    public boolean clinicianMayReadNotes(boolean activeConsent) {
        return activeConsent;
    }

    public void assertClinicianMayReadNotes(boolean activeConsent) {
        if (!activeConsent) {
            throw ApiException.forbidden("Patient consent is required before viewing care notes");
        }
    }

    public void assertClinicianMayReadNotes(UserAccount actor, boolean activeConsent) {
        if (actor.getRole() != Role.CLINICIAN) {
            throw ApiException.forbidden("Care notes are clinician-only and never public");
        }
        assertClinicianMayReadNotes(activeConsent);
    }

    public void assertCanAccessPatientRecord(UserAccount actor, UserAccount patient, boolean activeConsent) {
        if (actor.getRole() == Role.PATIENT && actor.getId().equals(patient.getId())) {
            return;
        }
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() == Role.CLINICIAN && activeConsent) {
            return;
        }
        throw ApiException.forbidden("You cannot access this patient record without consent");
    }

    public void assertPatient(UserAccount actor) {
        if (actor.getRole() != Role.PATIENT) {
            throw ApiException.forbidden("Only patients can perform this action");
        }
    }

    public void assertClinician(UserAccount actor) {
        if (actor.getRole() != Role.CLINICIAN) {
            throw ApiException.forbidden("Only clinicians can perform this action");
        }
    }

    public void assertAdmin(UserAccount actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("Only administrators can perform this action");
        }
    }
}
