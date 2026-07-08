package com.ferwafa.transfer;

import com.ferwafa.common.BusinessException;
import com.ferwafa.transfer.dto.TransferWindowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferWindowService {

    private final TransferWindowRepository transferWindowRepository;

    @Transactional(readOnly = true)
    public void assertTransferWindowOpen(String season) {
        LocalDate today = LocalDate.now();
        boolean open = transferWindowRepository.findOpenWindowForSeason(season, today).isPresent()
                || !transferWindowRepository.findOpenWindows(today).isEmpty();
        // Prefer season-specific, but fall back to any open window for the current date
        var seasonWindow = transferWindowRepository.findOpenWindowForSeason(season, today);
        if (seasonWindow.isEmpty()) {
            // Also allow if there's an open window that matches season OR if any window for season is currently open
            var anyOpen = transferWindowRepository.findOpenWindows(today).stream()
                    .filter(w -> w.getSeason().equals(season))
                    .findFirst();
            if (anyOpen.isEmpty()) {
                throw new BusinessException(
                        "No open transfer window for season " + season +
                                ". Transfers can only be requested during an active FERWAFA transfer window.");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TransferWindowResponse> list(String season) {
        LocalDate today = LocalDate.now();
        return transferWindowRepository.findBySeasonOrderByOpenDateAsc(season).stream()
                .map(w -> toResponse(w, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferWindowResponse> listOpen() {
        LocalDate today = LocalDate.now();
        return transferWindowRepository.findOpenWindows(today).stream()
                .map(w -> toResponse(w, today))
                .collect(Collectors.toList());
    }

    private TransferWindowResponse toResponse(TransferWindow w, LocalDate today) {
        boolean currentlyOpen = Boolean.TRUE.equals(w.getActive())
                && !today.isBefore(w.getOpenDate())
                && !today.isAfter(w.getCloseDate());
        return TransferWindowResponse.builder()
                .id(w.getId())
                .season(w.getSeason())
                .name(w.getName())
                .openDate(w.getOpenDate())
                .closeDate(w.getCloseDate())
                .active(Boolean.TRUE.equals(w.getActive()))
                .currentlyOpen(currentlyOpen)
                .build();
    }
}
