package com.bcs.devicemonitor.modules.devices;

import com.bcs.devicemonitor.common.exceptions.ConflictException;
import com.bcs.devicemonitor.common.exceptions.ResourceNotFoundException;
import com.bcs.devicemonitor.modules.devices.dtos.request.RegisterDeviceRequest;
import com.bcs.devicemonitor.modules.devices.dtos.request.SubmitStatusReportRequest;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceDetailResponse;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceResponse;
import com.bcs.devicemonitor.modules.devices.enums.DeviceStatus;
import com.bcs.devicemonitor.modules.devices.enums.DeviceType;
import com.bcs.devicemonitor.modules.devices.services.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the core domain rules: registration, status reporting, current
 * status derivation, the 20-report cap and staleness detection. Backed by the
 * real schema (Liquibase on H2) and a real transaction manager.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;

    private DeviceResponse register(String name, String hostname) {
        return deviceService.registerDevice(
                new RegisterDeviceRequest(name, DeviceType.ROUTER, hostname, "Nairobi DC"));
    }

    @Test
    void registersDeviceAndMarksItStaleUntilItReports() {
        DeviceResponse device = register("Edge Router 1", "router-" + UUID.randomUUID());

        assertThat(device.id()).isNotNull();
        assertThat(device.registeredAt()).isNotNull();
        assertThat(device.currentStatus()).isNull();
        assertThat(device.lastReportAt()).isNull();
        assertThat(device.stale()).isTrue();
    }

    @Test
    void rejectsDuplicateHostname() {
        String hostname = "dup-" + UUID.randomUUID();
        register("Device A", hostname);

        assertThatThrownBy(() -> register("Device B", hostname))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void currentStatusReflectsMostRecentReport() {
        DeviceResponse device = register("Switch 1", "switch-" + UUID.randomUUID());

        deviceService.submitStatusReport(device.id(),
                new SubmitStatusReportRequest(DeviceStatus.OFFLINE, "boot", Instant.now().minus(2, ChronoUnit.MINUTES)));
        deviceService.submitStatusReport(device.id(),
                new SubmitStatusReportRequest(DeviceStatus.ONLINE, "healthy", Instant.now()));

        DeviceResponse listed = findInList(device.id());
        assertThat(listed.currentStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(listed.stale()).isFalse();
    }

    @Test
    void deviceBecomesStaleWhenLastReportIsOlderThanWindow() {
        DeviceResponse device = register("ONT 1", "ont-" + UUID.randomUUID());

        deviceService.submitStatusReport(device.id(),
                new SubmitStatusReportRequest(DeviceStatus.ONLINE, null, Instant.now().minus(20, ChronoUnit.MINUTES)));

        DeviceResponse listed = findInList(device.id());
        assertThat(listed.currentStatus()).isEqualTo(DeviceStatus.ONLINE);
        assertThat(listed.stale()).isTrue();
    }

    @Test
    void detailViewReturnsAtMost20ReportsNewestFirst() {
        DeviceResponse device = register("AP 1", "ap-" + UUID.randomUUID());

        for (int i = 0; i < 25; i++) {
            deviceService.submitStatusReport(device.id(),
                    new SubmitStatusReportRequest(DeviceStatus.ONLINE, "report " + i,
                            Instant.now().minus(25 - i, ChronoUnit.MINUTES)));
        }

        DeviceDetailResponse detail = deviceService.getDevice(device.id());
        assertThat(detail.recentReports()).hasSize(20);
        // Newest first: each report should be at or after the next one.
        for (int i = 0; i < detail.recentReports().size() - 1; i++) {
            assertThat(detail.recentReports().get(i).reportedAt())
                    .isAfterOrEqualTo(detail.recentReports().get(i + 1).reportedAt());
        }
    }

    @Test
    void submittingReportForUnknownDeviceThrows() {
        assertThatThrownBy(() -> deviceService.submitStatusReport(UUID.randomUUID(),
                new SubmitStatusReportRequest(DeviceStatus.ONLINE, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsFutureReportTimestamp() {
        DeviceResponse device = register("FW 1", "fw-" + UUID.randomUUID());

        assertThatThrownBy(() -> deviceService.submitStatusReport(device.id(),
                new SubmitStatusReportRequest(DeviceStatus.ONLINE, null, Instant.now().plus(1, ChronoUnit.HOURS))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private DeviceResponse findInList(UUID id) {
        List<DeviceResponse> devices = deviceService.listDevices();
        return devices.stream().filter(d -> d.id().equals(id)).findFirst().orElseThrow();
    }
}
