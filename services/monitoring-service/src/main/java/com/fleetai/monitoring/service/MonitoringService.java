package com.fleetai.monitoring.service;

import com.fleetai.monitoring.client.AiAgentClient;
import com.fleetai.monitoring.model.DriverAlert;
import com.fleetai.monitoring.model.TelemetryEvent;
import com.fleetai.monitoring.repository.DriverAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Two-tier detection, deliberately cheap-first:
 *  1. Fast, deterministic rule checks run on every single telemetry sample
 *     (speeding, harsh braking/acceleration, engine temp) — no LLM call, no
 *     added latency, catches the obvious cases.
 *  2. Anything borderline or already rule-flagged gets escalated to the AI
 *     Agent Layer for pattern-level judgment across the recent window (see
 *     AiAgentClient), which is where "three harsh-braking events in nine
 *     minutes" becomes a fatigue signal instead of three isolated log lines.
 *
 * This mirrors how the resume's "Automated Root Cause Analysis Engine" project
 * was structured: cheap rules triage volume, the AI layer earns its cost only
 * on the subset that actually needs judgment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private static final double SPEED_LIMIT_KMH = 110.0;
    private static final String LATEST_STATE_KEY_PREFIX = "vehicle:latest:";

    private final DriverAlertRepository alertRepository;
    private final AiAgentClient aiAgentClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(TelemetryEvent event) {
        cacheLatestState(event);

        boolean ruleFlagged = event.getSpeedKmh() != null && event.getSpeedKmh() > SPEED_LIMIT_KMH
                || Boolean.TRUE.equals(event.getHarshBrakingDetected())
                || Boolean.TRUE.equals(event.getHarshAccelerationDetected())
                || (event.getEngineTempCelsius() != null && event.getEngineTempCelsius() > 115.0);

        if (!ruleFlagged) {
            return; // normal telemetry, nothing to escalate
        }

        long recentSimilarAlerts = alertRepository.countByDriverIdAndAlertTypeAndCreatedAtAfter(
                event.getDriverId(), "AI_FLAGGED", Instant.now().minus(1, ChronoUnit.HOURS));

        Map<String, Object> context = new HashMap<>();
        context.put("speedKmh", event.getSpeedKmh());
        context.put("harshBraking", event.getHarshBrakingDetected());
        context.put("harshAcceleration", event.getHarshAccelerationDetected());
        context.put("engineTempCelsius", event.getEngineTempCelsius());
        context.put("recentSimilarAlertCount", recentSimilarAlerts);

        Map<String, Object> agentResult = aiAgentClient.analyzeAndAlert(
                event.getDriverId(), event.getVehicleId(), context);

        String severity = String.valueOf(agentResult.getOrDefault("severity", "WARNING"));
        String summary = String.valueOf(agentResult.getOrDefault("summary", "Rule-based anomaly detected."));

        DriverAlert alert = DriverAlert.builder()
                .driverId(event.getDriverId())
                .vehicleId(event.getVehicleId())
                .alertType("AI_FLAGGED")
                .severity(severity)
                .message(summary)
                .agentInsight(summary)
                .status("OPEN")
                .createdAt(Instant.now())
                .build();

        alertRepository.save(alert);
        kafkaTemplate.send("driver.alerts", event.getVehicleId(), alert);

        log.info("Alert raised driver={} vehicle={} severity={}", event.getDriverId(), event.getVehicleId(), severity);
    }

    private void cacheLatestState(TelemetryEvent event) {
        redisTemplate.opsForValue().set(LATEST_STATE_KEY_PREFIX + event.getVehicleId(), event);
    }
}
