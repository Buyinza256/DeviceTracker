package com.bcs.devicemonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class DeviceMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceMonitorApplication.class, args);
    }
}
