package com.iotmining.services.deviceprofile.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "device_profiles", uniqueConstraints = {@UniqueConstraint(columnNames = {"tenant_id", "name"})})
public class DeviceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String protocol;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_profile_dashboards", joinColumns = @JoinColumn(name = "device_profile_id"))
    @Column(name = "dashboard_id")
    private Set<String> dashboardIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_profile_assigned_customers", joinColumns = @JoinColumn(name = "device_profile_id"))
    @Column(name = "customer_id")
    private Set<String> assignedCustomers = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_profile_msg_types", joinColumns = @JoinColumn(name = "device_profile_id"))
    @Column(name = "msg_type")
    private Set<String> messageTypes = new HashSet<>();

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
    @ToString.Exclude
    private DeviceProfile parent;

    @PrePersist
    @PreUpdate
    public void ensureMessageType() {
        if (messageTypes == null || messageTypes.isEmpty()) {
            messageTypes = new HashSet<>();
            messageTypes.add("default");
        }
    }
}
