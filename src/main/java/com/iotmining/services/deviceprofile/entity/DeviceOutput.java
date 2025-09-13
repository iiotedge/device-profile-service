package com.iotmining.services.deviceprofile.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Embeddable
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class DeviceOutput {
    private String action;        // e.g., "snapshot", "stream", "push_rtmp"
    private String targetType;    // e.g., "JPEG", "HLS", "JSON"
    private String destination;   // e.g., path, URL, service
    private String description;   // Optional
}
