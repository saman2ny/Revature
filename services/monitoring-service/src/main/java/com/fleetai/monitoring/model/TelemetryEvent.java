package com.fleetai.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Mirrors api-gateway's TelemetryEvent — the shared wire contract on "vehicle.telemetry". */
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
    private Double fuelLevelPercent;
    private Double engineTempCelsius;
    private Boolean harshBrakingDetected;
    private Boolean harshAccelerationDetected;
    private Instant timestamp;
}
