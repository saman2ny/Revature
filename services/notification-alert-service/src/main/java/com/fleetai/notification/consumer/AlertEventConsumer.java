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
public class AlertEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "driver.alerts", groupId = "notification-alert-service")
    @SuppressWarnings("unchecked")
    public void onDriverAlert(Object payload) {
        try {
            Map<String, Object> alert = (Map<String, Object>) payload;
            notificationService.handleDriverAlert(
                    String.valueOf(alert.get("driverId")),
                    String.valueOf(alert.get("vehicleId")),
                    alert
            );
        } catch (Exception ex) {
            log.error("Failed to process driver.alerts payload: {}", payload, ex);
        }
    }
}
