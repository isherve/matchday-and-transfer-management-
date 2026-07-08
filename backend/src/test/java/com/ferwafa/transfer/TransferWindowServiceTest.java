package com.ferwafa.transfer;

import com.ferwafa.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferWindowServiceTest {

    @Autowired private TransferWindowService transferWindowService;
    @Autowired private TransferWindowRepository transferWindowRepository;

    @Test
    void allowsTransferWhenWindowOpen() {
        // Seed includes Special Mid-Season Window covering July 2026
        assertThatCode(() -> transferWindowService.assertTransferWindowOpen("2025/26"))
                .doesNotThrowAnyException();
        assertThat(transferWindowService.listOpen()).isNotEmpty();
    }

    @Test
    void blocksTransferWhenNoWindowOpen() {
        transferWindowRepository.findAll().forEach(w -> {
            w.setActive(false);
            transferWindowRepository.save(w);
        });
        // Create only a closed historical window
        transferWindowRepository.save(TransferWindow.builder()
                .season("2099/00").name("Closed")
                .openDate(LocalDate.of(2099, 1, 1))
                .closeDate(LocalDate.of(2099, 1, 2))
                .active(true).build());

        assertThatThrownBy(() -> transferWindowService.assertTransferWindowOpen("2099/00"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No open transfer window");
    }
}
