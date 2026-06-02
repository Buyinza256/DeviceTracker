package com.bcs.devicemonitor.modules.devices.entities;

import com.bcs.devicemonitor.modules.devices.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * A network asset deployed within the infrastructure (CPE, router, switch, etc.).
 * The registration timestamp is populated automatically by JPA auditing.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devices", schema = "public")
@EntityListeners(AuditingEntityListener.class)
public class Entity_Device {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    /** Hostname or IP address through which the device is reachable. */
    @Column(name = "hostname", nullable = false)
    private String hostname;

    /** Physical location or site where the device is deployed. */
    @Column(name = "location")
    private String location;

    @CreatedDate
    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;
}
