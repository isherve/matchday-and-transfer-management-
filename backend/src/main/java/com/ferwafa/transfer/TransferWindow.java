package com.ferwafa.transfer;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "transfer_window")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferWindow extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String season;

    @Column(nullable = false)
    private String name;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(name = "close_date", nullable = false)
    private LocalDate closeDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
