package com.bcs.devicemonitor.modules.devices.dtos.response;

import com.bcs.devicemonitor.modules.devices.entities.Entity_Device;
import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;
import com.bcs.devicemonitor.modules.devices.enums.DeviceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A single device with its operational state and its most recent status reports
 * (capped at 20, newest first).
 */
public record DeviceDetailResponse(
        UUID id,
        String name,
        DeviceType deviceType,
        String hostname,
        String location,
        Instant registeredAt,
        DeviceStatus currentStatus,
        Instant lastReportAt,
        boolean stale,
        List<StatusReportResponse> recentReports
) {
    public static DeviceDetailResponse from(Entity_Device device,
                                            boolean stale,
                                            List<StatusReportResponse> recentReports) {
        StatusReportResponse latest = recentReports.isEmpty() ? null : recentReports.get(0);
        return new DeviceDetailResponse(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getHostname(),
                device.getLocation(),
                device.getRegisteredAt(),
                latest == null ? null : latest.status(),
                latest == null ? null : latest.reportedAt(),
                stale,
                recentReports
        );
    }
}
