package com.ferwafa.referee;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;

@Entity
@Table(name = "referee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referee_id")
    private Long refereeId;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private String lname;

    private String image;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "access_code_hash", nullable = false)
    private String accessCodeHash;
}
