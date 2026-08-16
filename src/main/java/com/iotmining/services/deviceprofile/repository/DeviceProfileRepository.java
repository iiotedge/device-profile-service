package com.iotmining.services.deviceprofile.repository;

import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceProfileRepository extends JpaRepository<DeviceProfile, UUID> {

    boolean existsByName(String name);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

    Optional<DeviceProfile> findByIdAndTenantId(UUID id, UUID tenantId);

    List<DeviceProfile> findAllByTenantId(UUID tenantId);

    Page<DeviceProfile> findAllByTenantId(UUID tenantId, Pageable pageable);

    Page<DeviceProfile> findByTenantIdAndNameContainingIgnoreCase(UUID tenantId, String name, Pageable pageable);

    Page<DeviceProfile> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
