package com.bcs.devicemonitor.modules.devices.controllers;

import com.bcs.devicemonitor.common.utilities.GenericResponse;
import com.bcs.devicemonitor.modules.devices.dtos.request.RegisterDeviceRequest;
import com.bcs.devicemonitor.modules.devices.dtos.request.SubmitStatusReportRequest;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceDetailResponse;
import com.bcs.devicemonitor.modules.devices.dtos.response.DeviceResponse;
import com.bcs.devicemonitor.modules.devices.dtos.response.StatusReportResponse;
import com.bcs.devicemonitor.modules.devices.services.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    /** Register a new device. */
    @PostMapping
    public ResponseEntity<GenericResponse> registerDevice(@Valid @RequestBody RegisterDeviceRequest request) {
        DeviceResponse device = deviceService.registerDevice(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericResponse(HttpStatus.CREATED.value(), "Device registered successfully", device));
    }

    /** List all registered devices with current status, last report time and stale indicator. */
    @GetMapping
    public GenericResponse listDevices() {
        List<DeviceResponse> devices = deviceService.listDevices();
        return new GenericResponse(HttpStatus.OK.value(), "Devices retrieved successfully", devices);
    }

    /** View a single device with its 20 most recent status reports. */
    @GetMapping("/{deviceId}")
    public GenericResponse getDevice(@PathVariable UUID deviceId) {
        DeviceDetailResponse device = deviceService.getDevice(deviceId);
        return new GenericResponse(HttpStatus.OK.value(), "Device retrieved successfully", device);
    }

    /** Submit a status report for a device. */
    @PostMapping("/{deviceId}/status-reports")
    public ResponseEntity<GenericResponse> submitStatusReport(@PathVariable UUID deviceId,
                                                              @Valid @RequestBody SubmitStatusReportRequest request) {
        StatusReportResponse report = deviceService.submitStatusReport(deviceId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericResponse(HttpStatus.CREATED.value(), "Status report recorded successfully", report));
    }
}
