package com.fleetai.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wire format published to the "vehicle.telemetry" Kafka topic.
 * This is the canonical event contract shared (by convention) with
 * monitoring-service and route-optimization-service, which both consume it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryEvent {

    @NotBlank
    private String driverId;

    @NotBlank
    private String vehicleId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Double speedKmh;

    private Double headingDegrees;
    private Double fuelLevelPercent;
    private Double engineTempCelsius;
    private Boolean harshBrakingDetected;
    private Boolean harshAccelerationDetected;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Builder.Default
    private Instant timestamp = Instant.now();
}
