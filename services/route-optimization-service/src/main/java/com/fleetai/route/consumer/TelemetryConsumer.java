package com.fleetai.route.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetai.route.model.TelemetryEvent;
import com.fleetai.route.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryConsumer {

    private final RouteOptimizationService routeOptimizationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "vehicle.telemetry", groupId = "route-optimization-service")
    public void onTelemetry(Object payload) {
        try {
            TelemetryEvent event = objectMapper.convertValue(payload, TelemetryEvent.class);
            routeOptimizationService.evaluate(event);
        } catch (Exception ex) {
            log.error("Failed to process telemetry payload: {}", payload, ex);
        }
    }
}
