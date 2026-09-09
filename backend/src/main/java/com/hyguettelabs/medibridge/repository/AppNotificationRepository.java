package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    List<AppNotification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    long countByRecipientIdAndReadFalse(UUID recipientId);
}
