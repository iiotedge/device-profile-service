package com.iotmining.services.deviceprofile.service;

import com.iotmining.services.deviceprofile.dto.DeviceProfileRequestDto;
import com.iotmining.services.deviceprofile.entity.DeviceInput;
import com.iotmining.services.deviceprofile.entity.DeviceOutput;
import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import com.iotmining.services.deviceprofile.repository.DeviceProfileRepository;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceProfileServiceImpl implements DeviceProfileService {

    private final DeviceProfileRepository repo;

    private UUID getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Claims claims) {
            Object tIdObj = claims.get("tenantId");
            if (tIdObj == null) {
                tIdObj = claims.get("tenant_id");
            }
            if (tIdObj != null) {
                return UUID.fromString(tIdObj.toString());
            }
        }
        throw new SecurityException("Unauthorized: Missing Tenant ID in security context.");
    }

    private boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Claims claims) {
            return Boolean.TRUE.equals(claims.get("superAdmin", Boolean.class));
        }
        return false;
    }

    @Override
    public DeviceProfile create(DeviceProfileRequestDto dto) {
        UUID targetTenantId;
        if (isSuperAdmin()) {
            if (dto.getTenantId() == null) {
                throw new IllegalArgumentException("Super Admins must explicitly provide a tenantId to create a profile.");
            }
            targetTenantId = dto.getTenantId();
        } else {
            targetTenantId = getCurrentTenantId();
        }

        if (repo.existsByTenantIdAndNameIgnoreCase(targetTenantId, dto.getName())) {
            throw new IllegalArgumentException("Device Profile name already exists in this tenant.");
        }

        DeviceProfile profile = mapDto(dto);
        profile.setTenantId(targetTenantId);

        if (dto.getParentId() != null) {
            DeviceProfile parent = repo.findByIdAndTenantId(dto.getParentId(), targetTenantId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent profile not found or access denied."));
            validateNoCycle(profile, parent);
            profile.setParent(parent);
        }

        return repo.save(profile);
    }

    @Override
    public List<DeviceProfile> getAll() {
        return repo.findAllByTenantId(getCurrentTenantId());
    }

    @Override
    public Page<DeviceProfile> getAll(Pageable pageable, String textSearch) {
        UUID tenantId = getCurrentTenantId();
        if (StringUtils.hasText(textSearch)) {
            return repo.findByTenantIdAndNameContainingIgnoreCase(tenantId, textSearch, pageable);
        }
        return repo.findAllByTenantId(tenantId, pageable);
    }

    @Override
    public Page<DeviceProfile> getAll(Pageable pageable) {
        return getAll(pageable, null);
    }

    @Override
    public DeviceProfile getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device Profile not found with ID: " + id));
    }

    @Override
    public DeviceProfile update(UUID id, DeviceProfileRequestDto dto) {
        DeviceProfile existing = getById(id);
        DeviceProfile updated = mapDto(dto);
        updated.setId(existing.getId());
        updated.setTenantId(existing.getTenantId());

        if (!existing.getName().equalsIgnoreCase(dto.getName())
                && repo.existsByTenantIdAndNameIgnoreCase(existing.getTenantId(), dto.getName())) {
            throw new IllegalArgumentException("Device Profile name already exists in this tenant.");
        }

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new IllegalArgumentException("A profile cannot be its own parent.");
            }
            DeviceProfile parent = repo.findByIdAndTenantId(dto.getParentId(), existing.getTenantId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent profile not found or access denied."));
            validateNoCycle(updated, parent);
            updated.setParent(parent);
        } else {
            updated.setParent(null);
        }

        return repo.save(updated);
    }

    @Override
    public void delete(UUID id) {
        DeviceProfile existing = getById(id);
        repo.delete(existing);
    }

    @Override
    public Map<String, Object> getResolved(UUID id) {
        DeviceProfile profile = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        Set<DeviceInput> resolvedInputs = new LinkedHashSet<>();
        Set<DeviceOutput> resolvedOutputs = new LinkedHashSet<>();
        Set<String> resolvedMsgTypes = new HashSet<>();
        Set<String> resolvedDashboardIds = new HashSet<>();

        collectInheritedData(profile, resolvedInputs, resolvedOutputs, resolvedMsgTypes, resolvedDashboardIds);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", profile.getId());
        result.put("name", profile.getName());
        result.put("type", profile.getType());
        result.put("protocol", profile.getProtocol());
        result.put("parentId", profile.getParent() != null ? profile.getParent().getId() : null);
        result.put("parentName", profile.getParent() != null ? profile.getParent().getName() : null);
        result.put("dashboardIds", resolvedDashboardIds);
        result.put("messageTypes", resolvedMsgTypes);
        result.put("inputs", resolvedInputs);
        result.put("outputs", resolvedOutputs);
        result.put("metadata", profile.getMetadata());
        return result;
    }

    private void collectInheritedData(DeviceProfile profile, Set<DeviceInput> inputs, Set<DeviceOutput> outputs,
                                       Set<String> msgTypes, Set<String> dashboardIds) {
        if (profile == null) {
            return;
        }
        if (profile.getInputs() != null) {
            inputs.addAll(profile.getInputs());
        }
        if (profile.getOutputs() != null) {
            outputs.addAll(profile.getOutputs());
        }
        if (profile.getMessageTypes() != null) {
            msgTypes.addAll(profile.getMessageTypes());
        }
        if (profile.getDashboardIds() != null) {
            dashboardIds.addAll(profile.getDashboardIds());
        }
        collectInheritedData(profile.getParent(), inputs, outputs, msgTypes, dashboardIds);
    }

    private void validateNoCycle(DeviceProfile candidate, DeviceProfile parent) {
        for (DeviceProfile current = parent; current != null; current = current.getParent()) {
            if (current.getId() != null && current.getId().equals(candidate.getId())) {
                throw new IllegalArgumentException("Circular inheritance detected.");
            }
        }
    }

    private DeviceProfile mapDto(DeviceProfileRequestDto dto) {
        DeviceProfile profile = new DeviceProfile();
        profile.setName(dto.getName());
        profile.setType(dto.getType());
        profile.setProtocol(dto.getProtocol());
        profile.setDashboardIds(dto.getDashboardIds());
        profile.setAssignedCustomers(dto.getAssignedCustomers());
        profile.setMessageTypes(dto.getMessageTypes());
        profile.setInputs(dto.getInputs());
        profile.setOutputs(dto.getOutputs());
        profile.setMetadata(dto.getMetadata());
        return profile;
    }

    @Override
    public Map<UUID, Map<String, Object>> getBatchResolved(List<UUID> ids) {
        Map<UUID, Map<String, Object>> result = new HashMap<>();
        for (UUID id : ids) {
            try {
                result.put(id, getResolved(id));
            } catch (Exception e) {
                result.put(id, Map.of("error", e.getMessage()));
            }
        }
        return result;
    }
}
