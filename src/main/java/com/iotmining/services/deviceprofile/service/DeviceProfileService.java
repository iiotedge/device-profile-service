package com.iotmining.services.deviceprofile.service;

import com.iotmining.services.deviceprofile.dto.DeviceProfileRequestDto;
import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DeviceProfileService {
    DeviceProfile create(DeviceProfileRequestDto dto);
    List<DeviceProfile> getAll();
    DeviceProfile getById(UUID id);
    DeviceProfile update(UUID id, DeviceProfileRequestDto dto);
    void delete(UUID id);
    Map<String, Object> getResolved(UUID id);
    Page<DeviceProfile> getAll(Pageable pageable);
    Map<UUID, Map<String, Object>> getBatchResolved(List<UUID> ids);

}
