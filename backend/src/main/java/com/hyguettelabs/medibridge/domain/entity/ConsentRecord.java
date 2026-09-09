package com.hyguettelabs.medibridge.domain.entity;

import com.hyguettelabs.medibridge.domain.enums.ConsentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_records", uniqueConstraints = @UniqueConstraint(name = "consent_pair_uq", columnNames = {"patient_id", "clinician_id"}))
public class ConsentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private UserAccount patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "clinician_id", nullable = false)
    private UserAccount clinician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConsentStatus status = ConsentStatus.GRANTED;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void onCreate() {
        if (grantedAt == null) {
            grantedAt = Instant.now();
        }
    }

    public boolean isActive() {
        return status == ConsentStatus.GRANTED;
    }

    public UUID getId() { return id; }
    public UserAccount getPatient() { return patient; }
    public void setPatient(UserAccount patient) { this.patient = patient; }
    public UserAccount getClinician() { return clinician; }
    public void setClinician(UserAccount clinician) { this.clinician = clinician; }
    public ConsentStatus getStatus() { return status; }
    public void setStatus(ConsentStatus status) { this.status = status; }
    public Instant getGrantedAt() { return grantedAt; }
    public void setGrantedAt(Instant grantedAt) { this.grantedAt = grantedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
