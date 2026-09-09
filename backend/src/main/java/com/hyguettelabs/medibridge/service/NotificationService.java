package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.AppNotification;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import com.hyguettelabs.medibridge.repository.AppNotificationRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final AppNotificationRepository notifications;

    public NotificationService(AppNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public void push(UserAccount recipient, NotificationType type, String title, String body) {
        AppNotification n = new AppNotification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.NotificationResponse> mine(UserAccount actor) {
        return notifications.findByRecipientIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    public long unread(UserAccount actor) {
        return notifications.countByRecipientIdAndReadFalse(actor.getId());
    }

    @Transactional
    public CoordinationDtos.NotificationResponse markRead(UserAccount actor, UUID id) {
        AppNotification n = notifications.findById(id).orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getRecipient().getId().equals(actor.getId())) {
            throw ApiException.forbidden("You cannot read someone else's notification");
        }
        n.setRead(true);
        return toDto(notifications.save(n));
    }

    private CoordinationDtos.NotificationResponse toDto(AppNotification n) {
        return new CoordinationDtos.NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getBody(), n.isRead(), n.getCreatedAt());
    }
}
