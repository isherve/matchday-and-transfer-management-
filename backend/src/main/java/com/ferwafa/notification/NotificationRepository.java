package com.ferwafa.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ferwafa.common.UserRole;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientRoleAndRecipientEntityIdOrderByCreatedAtDesc(
            UserRole role, Long entityId);

    long countByRecipientRoleAndRecipientEntityIdAndReadFlagFalse(UserRole role, Long entityId);
}
