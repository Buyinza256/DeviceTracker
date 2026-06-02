package com.bcs.devicemonitor.modules.devices.services;

import com.bcs.devicemonitor.common.exceptions.ConflictException;
import com.bcs.devicemonitor.common.exceptions.ResourceNotFoundException;
import com.bcs.devicemonitor.modules.devices.dtos.request.RegisterDeviceRequest;
import com.bcs.devicemonitor.modules.devices.dtos.request.SubmitStatusReportRequest;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceDetailResponse;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceResponse;
import com.bcs.devicemonitor.modules.devices.dtos.response.StatusReportResponse;
import com.bcs.devicemonitor.modules.devices.entities.Entity_Device;
import com.bcs.devicemonitor.modules.devices.entities.Entity_StatusReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core domain logic for the device monitoring service: device registration,
 * status reporting, and the derivation of current status / staleness.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    /** A device is stale if it has not reported within this many minutes. */
    @Value("${monitoring.stale-after-minutes:15}")
    private long staleAfterMinutes;

    private final com.bcs.devicemonitor.modules.devices.repositories.DeviceRepository deviceRepository;
    private final com.bcs.devicemonitor.modules.devices.repositories.StatusReportRepository statusReportRepository;

    private static final int MAX_RECENT_REPORTS = 20;

    @Transactional
    public DeviceResponse registerDevice(RegisterDeviceRequest request) {
        if (deviceRepository.existsByHostnameIgnoreCase(request.hostname())) {
            throw new ConflictException("A device with hostname '" + request.hostname() + "' is already registered");
        }

        Entity_Device device = Entity_Device.builder()
                .name(request.name())
                .deviceType(request.deviceType())
                .hostname(request.hostname())
                .location(request.location())
                .build();

        device = deviceRepository.save(device);
        log.info("Registered device {} ({})", device.getId(), device.getHostname());

        // A freshly registered device has no reports yet, so it is stale by definition.
        return DeviceResponse.from(device, null, true);
    }

    @Transactional
    public StatusReportResponse submitStatusReport(UUID deviceId, SubmitStatusReportRequest request) {
        Entity_Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));

        Instant reportedAt = request.reportedAt() != null ? request.reportedAt() : Instant.now();
        if (reportedAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("reportedAt cannot be in the future");
        }

        Entity_StatusReport report = Entity_StatusReport.builder()
                .device(device)
                .status(request.status())
                .message(request.message())
                .reportedAt(reportedAt)
                .build();

        report = statusReportRepository.save(report);
        log.info("Device {} reported {}", deviceId, request.status());

        return StatusReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices() {
        List<Entity_Device> devices = deviceRepository.findAll();

        // Resolve the latest report for every device in one query to avoid N+1 lookups.
        Map<UUID, Entity_StatusReport> latestByDevice = statusReportRepository.findLatestReportPerDevice()
                .stream()
                .collect(Collectors.toMap(r -> r.getDevice().getId(), Function.identity()));

        Instant now = Instant.now();
        return devices.stream()
                .sorted(Comparator.comparing(Entity_Device::getRegisteredAt))
                .map(device -> {
                    Entity_StatusReport latest = latestByDevice.get(device.getId());
                    boolean stale = isStale(latest, now);
                    return DeviceResponse.from(device, latest, stale);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceDetailResponse getDevice(UUID deviceId) {
        Entity_Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + deviceId));

        List<StatusReportResponse> recentReports = statusReportRepository
                .findByDeviceIdOrderByReportedAtDescIdDesc(deviceId, PageRequest.of(0, MAX_RECENT_REPORTS))
                .stream()
                .map(StatusReportResponse::from)
                .toList();

        Instant lastReportAt = recentReports.isEmpty() ? null : recentReports.get(0).reportedAt();
        boolean stale = isStale(lastReportAt, Instant.now());

        return DeviceDetailResponse.from(device, stale, recentReports);
    }

    /** A device is stale when it has never reported, or its last report predates the window. */
    private boolean isStale(Entity_StatusReport latest, Instant now) {
        return isStale(latest == null ? null : latest.getReportedAt(), now);
    }

    private boolean isStale(Instant lastReportAt, Instant now) {
        if (lastReportAt == null) {
            return true;
        }
        return lastReportAt.isBefore(now.minus(Duration.ofMinutes(staleAfterMinutes)));
    }
}
