package com.fleetai.monitoring.controller;

import com.fleetai.monitoring.model.DriverAlert;
import com.fleetai.monitoring.repository.DriverAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final DriverAlertRepository alertRepository;

    @GetMapping("/drivers/{driverId}/alerts")
    public List<DriverAlert> alertsForDriver(@PathVariable String driverId) {
        return alertRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    @GetMapping("/alerts/open")
    public List<DriverAlert> openAlerts() {
        return alertRepository.findByStatusOrderByCreatedAtDesc("OPEN");
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    public DriverAlert acknowledge(@PathVariable java.util.UUID alertId) {
        DriverAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus("ACKNOWLEDGED");
        return alertRepository.save(alert);
    }
}
