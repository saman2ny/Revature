package com.fleetai.notification.controller;

import com.fleetai.notification.model.Notification;
import com.fleetai.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/driver/{driverId}")
    public List<Notification> forDriver(@PathVariable String driverId) {
        return notificationRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
    }

    @GetMapping("/supervisor/feed")
    public List<Notification> supervisorFeed() {
        return notificationRepository.findByAudienceInOrderByCreatedAtDesc(List.of("SUPERVISOR", "BOTH"));
    }

    @GetMapping("/approvals/pending")
    public List<Notification> pendingApprovals() {
        return notificationRepository.findByApprovalStatusOrderByCreatedAtDesc("PENDING");
    }

    @PostMapping("/approvals/{notificationId}/approve")
    public Notification approve(@PathVariable UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        n.setApprovalStatus("APPROVED");
        return notificationRepository.save(n);
    }

    @PostMapping("/approvals/{notificationId}/reject")
    public Notification reject(@PathVariable UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        n.setApprovalStatus("REJECTED");
        return notificationRepository.save(n);
    }
}
