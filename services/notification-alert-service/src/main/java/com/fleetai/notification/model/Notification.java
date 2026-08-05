package com.fleetai.notification.model;

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
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String driverId;
    private String vehicleId;
    private String sourceEvent;   // DRIVER_ALERT | ROUTE_UPDATE
    private String audience;      // DRIVER | SUPERVISOR | BOTH | SUPPRESSED

    @Lob
    private String message;

    private String severity;
    private boolean requiresApproval;
    private String approvalStatus; // NONE | PENDING | AUTO_APPROVED | APPROVED | REJECTED
    private Instant createdAt;
}
