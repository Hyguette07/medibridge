package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrivacyGuardTest {

    private final PrivacyGuard guard = new PrivacyGuard();

    @Test
    void deniesNotesWithoutConsent() {
        assertThat(guard.clinicianMayReadNotes(false)).isFalse();
        assertThatThrownBy(() -> guard.assertClinicianMayReadNotes(false))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("consent");
        assertThatThrownBy(() -> guard.assertClinicianMayReadNotes(clinician(), false))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("consent");
    }

    @Test
    void allowsNotesWithConsent() {
        assertThat(guard.clinicianMayReadNotes(true)).isTrue();
        guard.assertClinicianMayReadNotes(true);
        guard.assertClinicianMayReadNotes(clinician(), true);
    }

    @Test
    void neverExposesNotesToPatients() {
        assertThatThrownBy(() -> guard.assertClinicianMayReadNotes(patient(), true))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("never public");
    }

    @Test
    void clinicianCannotAccessPatientWithoutConsent() {
        assertThatThrownBy(() -> guard.assertCanAccessPatientRecord(clinician(), patient(), false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void clinicianCanAccessPatientWithConsent() {
        guard.assertCanAccessPatientRecord(clinician(), patient(), true);
    }

    private UserAccount clinician() {
        UserAccount user = new UserAccount();
        user.setRole(Role.CLINICIAN);
        return user;
    }

    private UserAccount patient() {
        UserAccount user = new UserAccount();
        user.setRole(Role.PATIENT);
        return user;
    }
}
