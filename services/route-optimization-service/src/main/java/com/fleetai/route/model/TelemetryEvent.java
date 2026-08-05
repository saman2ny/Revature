package com.fleetai.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {
    private String driverId;
    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double headingDegrees;
    private Instant timestamp;
}
