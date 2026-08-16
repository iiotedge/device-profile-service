package com.iotmining.services.deviceprofile.entity;

import jakarta.persistence.Column; // Import this
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class DeviceInput {

    @Column(nullable = false)
    private String key;          // e.g. "lat", "rpm"

    private String dataType;     // e.g. "DOUBLE", "STRING"

    private String format;       // e.g. "%.2f", "JSON"

    private String description;  // e.g. "GPS Latitude"

    // [NEW] Robust Link to Message Type
    // Maps this key to a specific stream (e.g. "g", "e", "default")
    @Column(name = "msg_type")
    private String messageType = "default";
}