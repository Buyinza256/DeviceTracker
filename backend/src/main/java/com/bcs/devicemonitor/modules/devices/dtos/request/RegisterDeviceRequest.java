package com.bcs.devicemonitor.modules.devices.dtos.request;

import com.bcs.devicemonitor.modules.devices.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for registering a new network device.
 */
public record RegisterDeviceRequest(

        @NotBlank(message = "name is required")
        @Size(max = 255)
        String name,

        @NotNull(message = "deviceType is required")
        DeviceType deviceType,

        @NotBlank(message = "hostname is required")
        @Size(max = 255)
        String hostname,

        @Size(max = 255)
        String location
) {
}
