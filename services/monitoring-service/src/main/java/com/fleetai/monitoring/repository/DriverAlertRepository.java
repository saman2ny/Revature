package com.fleetai.monitoring.repository;

import com.fleetai.monitoring.model.DriverAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverAlertRepository extends JpaRepository<DriverAlert, UUID> {
    List<DriverAlert> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<DriverAlert> findByStatusOrderByCreatedAtDesc(String status);

    long countByDriverIdAndAlertTypeAndCreatedAtAfter(
            String driverId, String alertType, java.time.Instant since);
}
