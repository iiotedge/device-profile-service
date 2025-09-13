package com.iotmining.services.deviceprofile.controller;

import com.iotmining.services.deviceprofile.dto.DeviceProfileRequestDto;
import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import com.iotmining.services.deviceprofile.service.DeviceProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/device-profiles")
@RequiredArgsConstructor
//@Tag(name = "Device Profiles", description = "Manage device profiles with input/output structure and inheritance")
public class DeviceProfileController {

    private final DeviceProfileService service;

    @PostMapping
    public ResponseEntity<DeviceProfile> create(@Valid @RequestBody DeviceProfileRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

//    @GetMapping
//    public List<DeviceProfile> list() {
//        return service.getAll();
//    }

    @GetMapping
    public ResponseEntity<Page<DeviceProfile>> getDeviceProfiles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
            // Add filter params here if needed (e.g., @RequestParam UUID tenantId)
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceProfile> profiles = service.getAll(pageable);
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public DeviceProfile getById(@PathVariable("id") UUID id) {
        return service.getById(id);
    }

    @GetMapping("/{id}/resolved")
    public Map<String, Object> getResolved(@PathVariable("id") UUID id) {
        return service.getResolved(id);
    }

    @PutMapping("/{id}")
    public DeviceProfile update(@PathVariable("id") UUID id, @Valid @RequestBody DeviceProfileRequestDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }


    @PostMapping("/batch-resolved")
    public Map<UUID, Map<String, Object>> getBatchResolved(@RequestBody List<UUID> ids) {
        return service.getBatchResolved(ids);
    }


}
