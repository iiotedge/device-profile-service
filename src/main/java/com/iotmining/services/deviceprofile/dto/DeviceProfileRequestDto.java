package com.iotmining.services.deviceprofile.dto;

import com.iotmining.services.deviceprofile.entity.DeviceInput;
import com.iotmining.services.deviceprofile.entity.DeviceOutput;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class DeviceProfileRequestDto {

    private UUID tenantId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Protocol is required")
    private String protocol;

    private Set<String> dashboardIds;
    private Set<String> messageTypes;
    private Set<String> assignedCustomers;
    private List<DeviceInput> inputs;
    private List<DeviceOutput> outputs;
    private Map<String, String> metadata;
    private UUID parentId;
}
