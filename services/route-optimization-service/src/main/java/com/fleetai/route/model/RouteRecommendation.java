package com.fleetai.route.model;

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
public class RouteRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String driverId;
    private String vehicleId;
    private boolean reroute;
    private boolean requiresApproval;
    private boolean autoApproved;

    @Lob
    private String rationale; // Route Agent narrative, shown to supervisor/driver

    private String status; // PROPOSED | APPROVED | REJECTED | APPLIED
    private Instant createdAt;
}
