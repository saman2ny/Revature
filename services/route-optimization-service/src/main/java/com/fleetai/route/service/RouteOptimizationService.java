package com.fleetai.route.service;

import com.fleetai.route.client.AiAgentClient;
import com.fleetai.route.model.RouteRecommendation;
import com.fleetai.route.model.TelemetryEvent;
import com.fleetai.route.repository.RouteRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Deliberately simple deterministic candidate-generation step (a real deployment
 * would call a routing/traffic API here — Google Directions, HERE, or an
 * in-house solver) followed by the judgment call: is this candidate actually
 * worth rerouting the driver for? That decision is delegated to the Route Agent
 * rather than hardcoded, since it depends on soft constraints (delivery windows,
 * hours-of-service, disruption cost) that don't reduce cleanly to a formula.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final RouteRecommendationRepository routeRepository;
    private final AiAgentClient aiAgentClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void evaluate(TelemetryEvent event) {
        // Placeholder candidate-route generation. Replace with a real traffic/routing
        // API call keyed off event.getLatitude()/getLongitude()/getHeadingDegrees().
        Map<String, Object> candidateRoute = Map.of(
                "estimatedTimeSavingsMinutes", 12,
                "additionalDistanceKm", 4.5,
                "crossesDeliveryWindow", false
        );

        if ((int) candidateRoute.get("estimatedTimeSavingsMinutes") < 5) {
            return; // not worth escalating to the agent for a marginal gain
        }

        Map<String, Object> context = new HashMap<>();
        context.put("currentPosition", Map.of("lat", event.getLatitude(), "lon", event.getLongitude()));
        context.put("candidateRoute", candidateRoute);

        Map<String, Object> agentResult = aiAgentClient.optimizeRoute(
                event.getDriverId(), event.getVehicleId(), context);

        Object dataObj = agentResult.get("data");
        boolean reroute = dataObj instanceof Map<?, ?> m && Boolean.TRUE.equals(m.get("reroute"));

        if (!reroute) {
            return;
        }

        RouteRecommendation recommendation = RouteRecommendation.builder()
                .driverId(event.getDriverId())
                .vehicleId(event.getVehicleId())
                .reroute(true)
                .requiresApproval(Boolean.TRUE.equals(agentResult.get("requiresApproval")))
                .autoApproved(Boolean.TRUE.equals(agentResult.get("autoApproved")))
                .rationale(String.valueOf(agentResult.getOrDefault("summary", "")))
                .status("PROPOSED")
                .createdAt(Instant.now())
                .build();

        routeRepository.save(recommendation);
        kafkaTemplate.send("route.updates", event.getVehicleId(), recommendation);

        log.info("Route recommendation proposed driver={} vehicle={} requiresApproval={}",
                event.getDriverId(), event.getVehicleId(), recommendation.isRequiresApproval());
    }
}
