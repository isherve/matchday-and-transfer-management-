package com.ferwafa.config;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;

@Entity
@Table(name = "league_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", nullable = false, unique = true)
    private String ruleKey;

    @Column(name = "rule_value", nullable = false)
    private String ruleValue;

    private String description;
}
