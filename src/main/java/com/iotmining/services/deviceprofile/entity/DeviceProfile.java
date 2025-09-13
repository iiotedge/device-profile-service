package com.iotmining.services.deviceprofile.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "device_profiles")
@Getter
@Setter
public class DeviceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type; // CAMERA, SENSOR, ACTUATOR

    @Column(nullable = false)
    private String protocol; // MQTT, MODBUS, HTTP, etc.

    @ElementCollection
    @CollectionTable(name = "device_inputs", joinColumns = @JoinColumn(name = "device_profile_id"))
    private List<DeviceInput> inputs = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "device_outputs", joinColumns = @JoinColumn(name = "device_profile_id"))
    private List<DeviceOutput> outputs = new ArrayList<>();

    @ElementCollection
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    @CollectionTable(name = "device_metadata", joinColumns = @JoinColumn(name = "device_profile_id"))
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DeviceProfile parent;

    // Getters and setters omitted for brevity
}

