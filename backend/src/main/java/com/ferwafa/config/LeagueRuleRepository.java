package com.ferwafa.config;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LeagueRuleRepository extends JpaRepository<LeagueRule, Long> {
    Optional<LeagueRule> findByRuleKey(String ruleKey);
}
