package com.fleetai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Edge entrypoint for the Fleet AI Platform.
 *
 * Responsibilities:
 *  1. Reverse-proxies REST calls from the driver mobile app / supervisor dashboard
 *     to the internal microservices (see {@link com.fleetai.gateway.config.GatewayConfig}).
 *  2. Terminates and validates JWT auth at the edge (see filter package).
 *  3. Accepts raw GPS/speed/telemetry pings from the driver app and republishes them
 *     onto the "vehicle.telemetry" Kafka topic (see producer package), which is the
 *     entry point into the event-driven backbone shown in the architecture diagram.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
