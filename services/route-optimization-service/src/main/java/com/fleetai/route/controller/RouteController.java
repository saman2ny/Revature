package com.fleetai.route.controller;

import com.fleetai.route.model.RouteRecommendation;
import com.fleetai.route.repository.RouteRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteRecommendationRepository routeRepository;

    @GetMapping("/{driverId}")
    public List<RouteRecommendation> forDriver(@PathVariable String driverId) {
        return routeRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    @PostMapping("/{recommendationId}/apply")
    public RouteRecommendation apply(@PathVariable UUID recommendationId) {
        RouteRecommendation rec = routeRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found: " + recommendationId));
        rec.setStatus("APPLIED");
        return routeRepository.save(rec);
    }

    @PostMapping("/{recommendationId}/reject")
    public RouteRecommendation reject(@PathVariable UUID recommendationId) {
        RouteRecommendation rec = routeRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found: " + recommendationId));
        rec.setStatus("REJECTED");
        return routeRepository.save(rec);
    }
}
