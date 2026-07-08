package com.ferwafa.notification;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "List notifications for current user")
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.forUser(currentRole(), securityUtils.currentEntityId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count",
                notificationService.unreadCount(currentRole(), securityUtils.currentEntityId())));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(currentRole(), securityUtils.currentEntityId());
        return ResponseEntity.noContent().build();
    }

    private UserRole currentRole() {
        String role = securityUtils.currentRole();
        if (role == null) throw new BusinessException("Not authenticated", HttpStatus.UNAUTHORIZED);
        return UserRole.valueOf(role);
    }
}
