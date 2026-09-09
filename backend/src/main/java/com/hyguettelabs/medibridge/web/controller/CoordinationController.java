package com.hyguettelabs.medibridge.web.controller;

import com.hyguettelabs.medibridge.security.CurrentUser;
import com.hyguettelabs.medibridge.service.ConsentService;
import com.hyguettelabs.medibridge.service.DirectoryService;
import com.hyguettelabs.medibridge.service.NoteService;
import com.hyguettelabs.medibridge.service.NotificationService;
import com.hyguettelabs.medibridge.web.dto.ApiResponse;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CoordinationController {

    private final DirectoryService directory;
    private final ConsentService consents;
    private final NoteService notes;
    private final NotificationService notifications;

    public CoordinationController(DirectoryService directory,
                                  ConsentService consents,
                                  NoteService notes,
                                  NotificationService notifications) {
        this.directory = directory;
        this.consents = consents;
        this.notes = notes;
        this.notifications = notifications;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Object> dashboard() {
        return ApiResponse.ok(directory.dashboard(CurrentUser.require()));
    }

    @GetMapping("/clinicians")
    public ApiResponse<List<CoordinationDtos.ClinicianResponse>> clinicians() {
        return ApiResponse.ok(directory.listClinicians());
    }

    @GetMapping("/patients")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ApiResponse<List<CoordinationDtos.PatientSummaryResponse>> patients() {
        return ApiResponse.ok(directory.consentedPatients(CurrentUser.require()));
    }

    @GetMapping("/patients/{id}")
    public ApiResponse<CoordinationDtos.PatientSummaryResponse> patient(@PathVariable UUID id) {
        return ApiResponse.ok(directory.getPatient(CurrentUser.require(), id));
    }

    @GetMapping("/consents")
    public ApiResponse<List<CoordinationDtos.ConsentResponse>> consents() {
        return ApiResponse.ok(consents.mine(CurrentUser.require()));
    }

    @PostMapping("/consents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<CoordinationDtos.ConsentResponse> grant(@Valid @RequestBody CoordinationDtos.ConsentGrantRequest request) {
        return ApiResponse.ok("Consent granted", consents.grant(CurrentUser.require(), request));
    }

    @PostMapping("/consents/{id}/revoke")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<CoordinationDtos.ConsentResponse> revoke(@PathVariable UUID id) {
        return ApiResponse.ok("Consent revoked", consents.revoke(CurrentUser.require(), id));
    }

    @GetMapping("/notes")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ApiResponse<List<CoordinationDtos.NoteResponse>> notes(@RequestParam UUID patientId) {
        return ApiResponse.ok(notes.list(CurrentUser.require(), patientId));
    }

    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLINICIAN')")
    public ApiResponse<CoordinationDtos.NoteResponse> createNote(@Valid @RequestBody CoordinationDtos.NoteRequest request) {
        return ApiResponse.ok("Note recorded", notes.create(CurrentUser.require(), request));
    }

    @GetMapping("/notifications")
    public ApiResponse<List<CoordinationDtos.NotificationResponse>> notifications() {
        return ApiResponse.ok(notifications.mine(CurrentUser.require()));
    }

    @PatchMapping("/notifications/{id}/read")
    public ApiResponse<CoordinationDtos.NotificationResponse> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(notifications.markRead(CurrentUser.require(), id));
    }

    @GetMapping("/me/audit")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<CoordinationDtos.AuditEntryResponse>> myAudit() {
        return ApiResponse.ok(directory.myAudit(CurrentUser.require()));
    }

    @GetMapping("/patients/{id}/audit")
    public ApiResponse<List<CoordinationDtos.AuditEntryResponse>> patientAudit(@PathVariable UUID id) {
        return ApiResponse.ok(directory.patientAudit(CurrentUser.require(), id));
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CoordinationDtos.AuditEntryResponse>> adminAudit() {
        return ApiResponse.ok(directory.adminAudit(CurrentUser.require()));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CoordinationDtos.UserAdminRow>> users() {
        return ApiResponse.ok(directory.listUsers(CurrentUser.require()));
    }

    @PatchMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CoordinationDtos.UserAdminRow> enable(@PathVariable UUID id,
                                                            @RequestBody CoordinationDtos.UserEnabledRequest request) {
        return ApiResponse.ok(directory.setEnabled(CurrentUser.require(), id, request.enabled()));
    }
}
