package com.fleetai.monitoring.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetai.monitoring.model.TelemetryEvent;
import com.fleetai.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryConsumer {

    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "vehicle.telemetry", groupId = "monitoring-service")
    public void onTelemetry(Object payload) {
        try {
            TelemetryEvent event = objectMapper.convertValue(payload, TelemetryEvent.class);
            monitoringService.process(event);
        } catch (Exception ex) {
            log.error("Failed to process telemetry payload: {}", payload, ex);
            // In production, route to a dead-letter topic instead of swallowing.
        }
    }
}
