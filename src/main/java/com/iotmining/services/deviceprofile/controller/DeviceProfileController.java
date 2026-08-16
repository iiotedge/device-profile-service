package com.iotmining.services.deviceprofile.controller;

import com.iotmining.services.deviceprofile.dto.DeviceProfileRequestDto;
import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import com.iotmining.services.deviceprofile.service.DeviceProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/device-profile")
public class DeviceProfileController {

    private final DeviceProfileService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'TENANT_ADMIN', 'ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<DeviceProfile> create(@Valid @RequestBody DeviceProfileRequestDto dto) {
        log.info("Creating new device profile: {}", dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<DeviceProfile>> getDeviceProfiles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "textSearch", required = false) String textSearch) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<DeviceProfile> profiles = service.getAll(pageable, textSearch);
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DeviceProfile>> getProfileList() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@deviceProfileSecurity.canAccessProfile(authentication, #id)")
    public ResponseEntity<DeviceProfile> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'TENANT_ADMIN', 'ROLE_ADMIN', 'ADMIN') and @deviceProfileSecurity.canAccessProfile(authentication, #id)")
    public ResponseEntity<DeviceProfile> update(@PathVariable("id") UUID id, @Valid @RequestBody DeviceProfileRequestDto dto) {
        log.info("Updating device profile ID: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_TENANT_ADMIN', 'TENANT_ADMIN', 'ROLE_ADMIN', 'ADMIN') and @deviceProfileSecurity.canAccessProfile(authentication, #id)")
    public void delete(@PathVariable("id") UUID id) {
        log.warn("Deleting device profile ID: {}", id);
        service.delete(id);
    }

    @GetMapping("/{id}/resolved")
    @PreAuthorize("@deviceProfileSecurity.canAccessProfile(authentication, #id)")
    public ResponseEntity<Map<String, Object>> getResolved(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getResolved(id));
    }

    @PostMapping("/batch-resolved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<UUID, Map<String, Object>>> getBatchResolved(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(service.getBatchResolved(ids));
    }
}
