package com.fleetai.gateway.controller;

import com.fleetai.gateway.model.TelemetryEvent;
import com.fleetai.gateway.producer.TelemetryProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the ingress point the "Driver Mobile App -> GPS/speed/Telemetry -> API Gateway"
 * arrow in the architecture diagram represents. The driver app POSTs a telemetry sample
 * here (typically every few seconds); the gateway republishes it onto Kafka so every
 * downstream consumer (monitoring-service, route-optimization-service) sees the same
 * ordered stream without being coupled to each other or to the gateway.
 */
@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryIngestController {

    private final TelemetryProducer telemetryProducer;

    @PostMapping
    public ResponseEntity<Void> ingest(@Valid @RequestBody TelemetryEvent event) {
        telemetryProducer.publish(event);
        return ResponseEntity.accepted().build();
    }
}
