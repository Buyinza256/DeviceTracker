package com.bcs.devicemonitor.modules.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full HTTP round-trips through the real controller, service and database
 * (Liquibase on H2), covering the four required capabilities and error paths.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerSubmitListAndViewDevice() throws Exception {
        String hostname = "edge-" + UUID.randomUUID();

        // Register
        String registerBody = """
                {"name":"Edge Router","deviceType":"ROUTER","hostname":"%s","location":"Mombasa"}
                """.formatted(hostname);

        String created = mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnObject.stale", is(true)))
                .andExpect(jsonPath("$.returnObject.currentStatus").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String deviceId = objectMapper.readTree(created).at("/returnObject/id").asText();

        // Submit a status report
        mockMvc.perform(post("/api/devices/{id}/status-reports", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONLINE\",\"message\":\"all good\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnObject.status", is("ONLINE")));

        // List
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnObject").isArray());

        // View one
        mockMvc.perform(get("/api/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnObject.currentStatus", is("ONLINE")))
                .andExpect(jsonPath("$.returnObject.stale", is(false)))
                .andExpect(jsonPath("$.returnObject.recentReports.length()", is(1)));
    }

    @Test
    void registerValidationFailsWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"location\":\"nowhere\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Failed")));
    }

    @Test
    void viewingUnknownDeviceReturns404() throws Exception {
        mockMvc.perform(get("/api/devices/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
