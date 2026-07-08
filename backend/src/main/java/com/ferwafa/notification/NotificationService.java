package com.ferwafa.notification;

import com.ferwafa.common.NotificationType;
import com.ferwafa.common.UserRole;
import com.ferwafa.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(UserRole role, Long entityId, String title, String message,
                       NotificationType type, String relatedType, Long relatedId) {
        notificationRepository.save(Notification.builder()
                .recipientRole(role)
                .recipientEntityId(entityId)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityType(relatedType)
                .relatedEntityId(relatedId)
                .readFlag(false)
                .build());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> forUser(UserRole role, Long entityId) {
        return notificationRepository
                .findByRecipientRoleAndRecipientEntityIdOrderByCreatedAtDesc(role, entityId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long unreadCount(UserRole role, Long entityId) {
        return notificationRepository.countByRecipientRoleAndRecipientEntityIdAndReadFlagFalse(role, entityId);
    }

    @Transactional
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllRead(UserRole role, Long entityId) {
        notificationRepository.findByRecipientRoleAndRecipientEntityIdOrderByCreatedAtDesc(role, entityId)
                .forEach(n -> {
                    n.setReadFlag(true);
                    notificationRepository.save(n);
                });
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .relatedEntityType(n.getRelatedEntityType())
                .relatedEntityId(n.getRelatedEntityId())
                .read(Boolean.TRUE.equals(n.getReadFlag()))
                .createdAt(n.getCreatedAt())
                .build();
    }
}
