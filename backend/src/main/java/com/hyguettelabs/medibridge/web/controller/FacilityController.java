package com.hyguettelabs.medibridge.web.controller;

import com.hyguettelabs.medibridge.security.CurrentUser;
import com.hyguettelabs.medibridge.service.FacilityService;
import com.hyguettelabs.medibridge.web.dto.ApiResponse;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facilities")
public class FacilityController {

    private final FacilityService facilities;

    public FacilityController(FacilityService facilities) {
        this.facilities = facilities;
    }

    @GetMapping
    public ApiResponse<List<CoordinationDtos.FacilityResponse>> list() {
        return ApiResponse.ok(facilities.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CoordinationDtos.FacilityResponse> create(@Valid @RequestBody CoordinationDtos.FacilityRequest request) {
        return ApiResponse.ok("Facility created", facilities.create(CurrentUser.require(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CoordinationDtos.FacilityResponse> update(@PathVariable UUID id,
                                                                 @Valid @RequestBody CoordinationDtos.FacilityRequest request) {
        return ApiResponse.ok(facilities.update(CurrentUser.require(), id, request));
    }
}
