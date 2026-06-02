package com.bcs.devicemonitor.modules.devices.repositories;

import com.bcs.devicemonitor.modules.devices.entities.Entity_Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Entity_Device, UUID> {

    boolean existsByHostnameIgnoreCase(String hostname);
}
