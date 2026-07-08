package com.ferwafa.notification;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.UserRole;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", nullable = false)
    private UserRole recipientRole;

    @Column(name = "recipient_entity_id", nullable = false)
    private Long recipientEntityId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "related_entity_type")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private Boolean readFlag = false;
}
