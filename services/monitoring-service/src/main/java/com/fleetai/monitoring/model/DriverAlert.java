package com.fleetai.monitoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String driverId;
    private String vehicleId;
    private String alertType;      // SPEEDING | HARSH_BRAKING | FATIGUE_PATTERN | ENGINE_DIAGNOSTIC | AI_FLAGGED
    private String severity;       // INFO | WARNING | CRITICAL

    @Lob
    private String message;

    @Lob
    private String agentInsight;   // raw Monitoring Agent narrative, for audit/explainability

    private String status;         // OPEN | ACKNOWLEDGED | RESOLVED
    private Instant createdAt;
}
