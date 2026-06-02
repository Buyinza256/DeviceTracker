package com.bcs.devicemonitor.modules.devices.dtos.response;

import com.bcs.devicemonitor.modules.devices.entities.Entity_StatusReport;
import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;

import java.time.Instant;

public record StatusReportResponse(
        Long id,
        DeviceStatus status,
        String message,
        Instant reportedAt
) {
    public static StatusReportResponse from(Entity_StatusReport report) {
        return new StatusReportResponse(
                report.getId(),
                report.getStatus(),
                report.getMessage(),
                report.getReportedAt()
        );
    }
}
