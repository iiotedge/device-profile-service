package com.iotmining.services.deviceprofile.service;

import com.iotmining.services.deviceprofile.dto.DeviceProfileRequestDto;
import com.iotmining.services.deviceprofile.entity.DeviceInput;
import com.iotmining.services.deviceprofile.entity.DeviceOutput;
import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import com.iotmining.services.deviceprofile.repository.DeviceProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DeviceProfileServiceImpl implements DeviceProfileService {

    private final DeviceProfileRepository repo;

    @Override
    public DeviceProfile create(DeviceProfileRequestDto dto) {
        if (repo.existsByName(dto.name()))
            throw new IllegalArgumentException("Device Profile name already exists");

        DeviceProfile profile = mapDto(dto);
        if (dto.parentId() != null) {
            DeviceProfile parent = repo.findById(dto.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
            validateNoCycle(profile, parent);
            profile.setParent(parent);
        }
        return repo.save(profile);
    }

    @Override
    public List<DeviceProfile> getAll() {
        return repo.findAll();
    }

    @Override
    public Page<DeviceProfile> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public DeviceProfile getById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Profile not found"));
    }

    @Override
    public DeviceProfile update(UUID id, DeviceProfileRequestDto dto) {
        DeviceProfile existing = getById(id);
        DeviceProfile updated = mapDto(dto);
        updated.setId(existing.getId());

        if (dto.parentId() != null) {
            DeviceProfile parent = repo.findById(dto.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
            validateNoCycle(updated, parent);
            updated.setParent(parent);
        }

        return repo.save(updated);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id))
            throw new EntityNotFoundException("Profile not found");
        repo.deleteById(id);
    }

    @Override
    public Map<String, Object> getResolved(UUID id) {
        DeviceProfile profile = getById(id);

        Set<DeviceInput> resolvedInputs = new LinkedHashSet<>();
        Set<DeviceOutput> resolvedOutputs = new LinkedHashSet<>();
        collectInheritedIO(profile, resolvedInputs, resolvedOutputs);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", profile.getId());
        result.put("name", profile.getName());
        result.put("type", profile.getType());
        result.put("protocol", profile.getProtocol());
        result.put("inputs", resolvedInputs);
        result.put("outputs", resolvedOutputs);
        result.put("metadata", profile.getMetadata());

        return result;
    }

    private void collectInheritedIO(DeviceProfile profile,
                                    Set<DeviceInput> inputs,
                                    Set<DeviceOutput> outputs) {
        if (profile == null) return;
        inputs.addAll(profile.getInputs());
        outputs.addAll(profile.getOutputs());
        collectInheritedIO(profile.getParent(), inputs, outputs);
    }

    private void validateNoCycle(DeviceProfile candidate, DeviceProfile parent) {
        DeviceProfile current = parent;
        while (current != null) {
            if (current.getId() != null && current.getId().equals(candidate.getId())) {
                throw new IllegalArgumentException("Circular inheritance detected");
            }
            current = current.getParent();
        }
    }

    private DeviceProfile mapDto(DeviceProfileRequestDto dto) {
        DeviceProfile profile = new DeviceProfile();
        profile.setName(dto.name());
        profile.setType(dto.type());
        profile.setProtocol(dto.protocol());
        profile.setInputs(dto.inputs());
        profile.setOutputs(dto.outputs());
        profile.setMetadata(dto.metadata());
        return profile;
    }

    public Map<UUID, Map<String, Object>> getBatchResolved(List<UUID> ids) {
        Map<UUID, Map<String, Object>> result = new HashMap<>();
        for (UUID id : ids) {
            try {
                result.put(id, getResolved(id)); // You may want to cache or optimize internally!
            } catch (Exception e) {
                result.put(id, Map.of("error", e.getMessage()));
            }
        }
        return result;
    }

}
