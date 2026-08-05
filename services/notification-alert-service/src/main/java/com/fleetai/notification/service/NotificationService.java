package com.fleetai.notification.service;

import com.fleetai.notification.model.Notification;
import com.fleetai.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Persists every notification for audit/history (the dashboard's alert feed
 * reads from here), then fans it out over WebSocket to whichever audience the
 * Alert Agent decided on. This is the last hop before the diagram's
 * "Supervisor dashboard" and "Driver mobile app" boxes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void handleDriverAlert(String driverId, String vehicleId, Map<String, Object> alertPayload) {
        String severity = String.valueOf(alertPayload.getOrDefault("severity", "INFO"));
        String message = String.valueOf(alertPayload.getOrDefault("message", "New alert"));

        dispatch(driverId, vehicleId, "DRIVER_ALERT", "SUPERVISOR", message, severity, false, "NONE");
    }

    public void handleRouteUpdate(Map<String, Object> routeEvent) {
        String driverId = String.valueOf(routeEvent.get("driverId"));
        String vehicleId = String.valueOf(routeEvent.get("vehicleId"));
        String rationale = String.valueOf(routeEvent.getOrDefault("rationale", "Route update available."));
        boolean requiresApproval = Boolean.TRUE.equals(routeEvent.get("requiresApproval"));

        String approvalStatus = requiresApproval ? "PENDING" : "AUTO_APPROVED";
        String audience = requiresApproval ? "SUPERVISOR" : "BOTH";

        dispatch(driverId, vehicleId, "ROUTE_UPDATE", audience, rationale, "WARNING", requiresApproval, approvalStatus);
    }

    private void dispatch(String driverId, String vehicleId, String sourceEvent, String audience,
                           String message, String severity, boolean requiresApproval, String approvalStatus) {

        if ("SUPPRESSED".equals(audience)) {
            log.debug("Notification suppressed by Alert Agent for driver={}", driverId);
            return;
        }

        Notification notification = Notification.builder()
                .driverId(driverId)
                .vehicleId(vehicleId)
                .sourceEvent(sourceEvent)
                .audience(audience)
                .message(message)
                .severity(severity)
                .requiresApproval(requiresApproval)
                .approvalStatus(approvalStatus)
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);

        if (List.of("SUPERVISOR", "BOTH").contains(audience)) {
            messagingTemplate.convertAndSend("/topic/supervisor-feed", notification);
        }
        if (List.of("DRIVER", "BOTH").contains(audience)) {
            messagingTemplate.convertAndSend("/topic/driver/" + driverId, notification);
        }
    }
}
