package com.bcs.devicemonitor.modules.devices.dtos.request;

import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload for a device submitting an operational status report.
 * <p>
 * {@code reportedAt} is optional; when omitted the server records the time of
 * receipt. This allows devices that buffer reports while offline to forward the
 * original observation time.
 */
public record SubmitStatusReportRequest(

        @NotNull(message = "status is required")
        DeviceStatus status,

        @Size(max = 1000)
        String message,

        Instant reportedAt
) {
}
