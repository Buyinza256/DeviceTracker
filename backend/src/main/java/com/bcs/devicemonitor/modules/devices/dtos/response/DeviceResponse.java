package com.bcs.devicemonitor.modules.devices.dtos.response;

import com.bcs.devicemonitor.modules.devices.entities.Entity_Device;
import com.bcs.devicemonitor.modules.devices.entities.Entity_StatusReport;
import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;
import com.bcs.devicemonitor.modules.devices.enums.DeviceType;

import java.time.Instant;
import java.util.UUID;

/**
 * A device together with its derived operational state, as shown in the device
 * list. {@code currentStatus} and {@code lastReportAt} come from the most recent
 * status report; both are null when the device has never reported. {@code stale}
 * is true when no report has been received within the staleness window.
 */
public record DeviceResponse(
        UUID id,
        String name,
        DeviceType deviceType,
        String hostname,
        String location,
        Instant registeredAt,
        DeviceStatus currentStatus,
        Instant lastReportAt,
        boolean stale
) {
    public static DeviceResponse from(Entity_Device device, Entity_StatusReport latestReport, boolean stale) {
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getHostname(),
                device.getLocation(),
                device.getRegisteredAt(),
                latestReport == null ? null : latestReport.getStatus(),
                latestReport == null ? null : latestReport.getReportedAt(),
                stale
        );
    }
}
