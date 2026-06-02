package com.bcs.devicemonitor.modules.devices.entities;

import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A single operational health report submitted by a device. Reports are
 * append-only; the most recent one determines a device's current status.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "status_reports", schema = "public")
public class Entity_StatusReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Entity_Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status;

    /** Optional diagnostic or informational message accompanying the report. */
    @Column(name = "message", length = 1000)
    private String message;

    /**
     * When the report was recorded. Defaults to the time of submission but may
     * be supplied by the caller (e.g. when a device batches and forwards reports).
     */
    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;
}
