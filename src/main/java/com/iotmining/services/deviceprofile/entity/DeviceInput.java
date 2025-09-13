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
public class DeviceInput {
    private String key;
    private String dataType;
    private String format;
    private String description;

}
