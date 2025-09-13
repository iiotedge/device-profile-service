package com.iotmining.services.deviceprofile.repository;

import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceProfileRepository extends JpaRepository<DeviceProfile, UUID> {
    boolean existsByName(String name);
}
