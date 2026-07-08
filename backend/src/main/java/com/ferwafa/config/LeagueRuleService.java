package com.ferwafa.config;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeagueRuleService {

    private final LeagueRuleRepository leagueRuleRepository;

    public int getInt(String key, int defaultValue) {
        return leagueRuleRepository.findByRuleKey(key)
                .map(r -> {
                    try {
                        return Integer.parseInt(r.getRuleValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    public void assertSquadSizeAllowsNewPlayer(long currentPlayerCount) {
        int max = getInt("MAX_SQUAD_SIZE", 30);
        if (currentPlayerCount >= max) {
            throw new BusinessException("Squad size limit reached (" + max + " players). Cannot register more players.");
        }
    }

    public void assertLineupSize(int selectedCount) {
        int max = getInt("MAX_LINEUP_SIZE", 11);
        if (selectedCount > max) {
            throw new BusinessException("Lineup cannot exceed " + max + " players");
        }
        if (selectedCount < 1) {
            throw new BusinessException("Lineup must include at least one player");
        }
    }

    public MemberRole playerRole() {
        return MemberRole.PLAYER;
    }
}
