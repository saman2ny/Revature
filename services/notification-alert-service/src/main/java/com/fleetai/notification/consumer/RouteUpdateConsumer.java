package com.fleetai.notification.consumer;

import com.fleetai.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouteUpdateConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "route.updates", groupId = "notification-alert-service")
    @SuppressWarnings("unchecked")
    public void onRouteUpdate(Object payload) {
        try {
            notificationService.handleRouteUpdate((Map<String, Object>) payload);
        } catch (Exception ex) {
            log.error("Failed to process route.updates payload: {}", payload, ex);
        }
    }
}
