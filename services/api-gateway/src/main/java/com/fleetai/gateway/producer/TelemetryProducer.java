package com.fleetai.gateway.producer;

import com.fleetai.gateway.model.TelemetryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryProducer {

    private static final String TOPIC = "vehicle.telemetry";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(TelemetryEvent event) {
        // Keyed by vehicleId so all telemetry for a given vehicle lands on the same
        // partition, preserving per-vehicle ordering for downstream consumers.
        kafkaTemplate.send(TOPIC, event.getVehicleId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish telemetry for vehicle={}", event.getVehicleId(), ex);
                } else {
                    log.debug("Published telemetry vehicle={} partition={} offset={}",
                            event.getVehicleId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
    }
}
