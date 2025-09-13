package com.iotmining.services.deviceprofile.dto;

import com.iotmining.services.deviceprofile.entity.DeviceInput;
import com.iotmining.services.deviceprofile.entity.DeviceOutput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DeviceProfileRequestDto(
        @NotBlank String name,
        @NotBlank String type,
        @NotBlank String protocol,
        @NotEmpty List<DeviceInput> inputs,
        @NotEmpty List<DeviceOutput> outputs,
        Map<String, String> metadata,
        UUID parentId
) {}
