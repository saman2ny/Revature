package com.fleetai.notification.repository;

import com.fleetai.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Notification> findByAudienceInOrderByCreatedAtDesc(List<String> audiences);
    List<Notification> findByApprovalStatusOrderByCreatedAtDesc(String status);
}
