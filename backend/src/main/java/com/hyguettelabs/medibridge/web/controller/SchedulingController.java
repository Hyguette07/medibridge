package com.hyguettelabs.medibridge.web.controller;

import com.hyguettelabs.medibridge.security.CurrentUser;
import com.hyguettelabs.medibridge.service.AppointmentService;
import com.hyguettelabs.medibridge.service.SlotService;
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
public class SchedulingController {

    private final SlotService slots;
    private final AppointmentService appointments;

    public SchedulingController(SlotService slots, AppointmentService appointments) {
        this.slots = slots;
        this.appointments = appointments;
    }

    @GetMapping("/slots")
    public ApiResponse<List<CoordinationDtos.SlotResponse>> slots(
            @RequestParam(required = false) UUID clinicianId,
            @RequestParam(required = false) UUID facilityId,
            @RequestParam(defaultValue = "true") boolean openOnly) {
        return ApiResponse.ok(slots.list(clinicianId, facilityId, openOnly));
    }

    @PostMapping("/slots")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLINICIAN')")
    public ApiResponse<CoordinationDtos.SlotResponse> createSlot(@Valid @RequestBody CoordinationDtos.SlotRequest request) {
        return ApiResponse.ok("Slot published", slots.create(CurrentUser.require(), request));
    }

    @DeleteMapping("/slots/{id}")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ApiResponse<Void> deleteSlot(@PathVariable UUID id) {
        slots.delete(CurrentUser.require(), id);
        return ApiResponse.ok("Slot removed", null);
    }

    @PostMapping("/slots/{id}/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<CoordinationDtos.AppointmentResponse> book(@PathVariable UUID id,
                                                                  @RequestBody(required = false) CoordinationDtos.BookRequest request) {
        return ApiResponse.ok("Visit booked", appointments.book(CurrentUser.require(), id,
                request == null ? new CoordinationDtos.BookRequest(null) : request));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<CoordinationDtos.AppointmentResponse>> appointments() {
        return ApiResponse.ok(appointments.mine(CurrentUser.require()));
    }

    @PostMapping("/appointments/{id}/cancel")
    public ApiResponse<CoordinationDtos.AppointmentResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.ok("Visit cancelled", appointments.cancel(CurrentUser.require(), id));
    }
}
