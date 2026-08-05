package com.fleetai.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket endpoint the supervisor-dashboard and driver-mobile-app
 * both connect to for real-time push (this is what "Notification & Alert
 * service -> Supervisor dashboard / Driver mobile app" represents in the
 * architecture diagram, as opposed to the request/response REST endpoints).
 *
 * Destinations:
 *   /topic/supervisor-feed        - all SUPERVISOR/BOTH-audience notifications
 *   /topic/driver/{driverId}      - notifications addressed to a specific driver
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tighten to the dashboard/app origins in production
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
