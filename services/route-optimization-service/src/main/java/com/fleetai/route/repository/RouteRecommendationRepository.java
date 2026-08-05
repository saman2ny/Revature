package com.fleetai.route.repository;

import com.fleetai.route.model.RouteRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RouteRecommendationRepository extends JpaRepository<RouteRecommendation, UUID> {
    List<RouteRecommendation> findByDriverIdOrderByCreatedAtDesc(String driverId);
}
