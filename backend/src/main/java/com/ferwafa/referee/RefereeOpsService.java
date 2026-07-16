package com.ferwafa.referee;

import com.ferwafa.common.BusinessException;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureService;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.referee.dto.DiaryRequest;
import com.ferwafa.referee.dto.DiaryResponse;
import com.ferwafa.referee.dto.MatchPrepRequest;
import com.ferwafa.referee.dto.MatchPrepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefereeOpsService {

    private static final int PREP_TOTAL = 7;

    private final RefereeMatchPrepRepository prepRepository;
    private final RefereeDiaryRepository diaryRepository;
    private final FixtureService fixtureService;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public MatchPrepResponse getPrep(Long fixtureId) {
        Long refereeId = requireRefereeId();
        Fixture fixture = fixtureService.getFixture(fixtureId);
        assertAssigned(fixture, refereeId);
        return prepRepository.findByFixtureIdAndRefereeId(fixtureId, refereeId)
                .map(this::toPrepResponse)
                .orElse(emptyPrep(fixtureId, refereeId));
    }

    @Transactional
    public MatchPrepResponse savePrep(Long fixtureId, MatchPrepRequest request) {
        Long refereeId = requireRefereeId();
        Fixture fixture = fixtureService.getFixture(fixtureId);
        assertAssigned(fixture, refereeId);

        RefereeMatchPrep prep = prepRepository.findByFixtureIdAndRefereeId(fixtureId, refereeId)
                .orElse(RefereeMatchPrep.builder().fixture(fixture).refereeId(refereeId).build());
        prep.setPitchChecked(request.isPitchChecked());
        prep.setBallsChecked(request.isBallsChecked());
        prep.setNetsChecked(request.isNetsChecked());
        prep.setCaptainsBriefed(request.isCaptainsBriefed());
        prep.setLineupsReceived(request.isLineupsReceived());
        prep.setMedicalReady(request.isMedicalReady());
        prep.setSecurityOk(request.isSecurityOk());
        prep.setNotes(request.getNotes());
        prep.setUpdatedAt(LocalDateTime.now());
        if (prep.getCreatedAt() == null) {
            prep.setCreatedAt(LocalDateTime.now());
        }
        return toPrepResponse(prepRepository.save(prep));
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> listDiary() {
        Long refereeId = requireRefereeId();
        return diaryRepository.findByRefereeIdOrderByEntryDateDescCreatedAtDesc(refereeId).stream()
                .map(this::toDiaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiaryResponse addDiary(DiaryRequest request) {
        Long refereeId = requireRefereeId();
        RefereeDiary entry = RefereeDiary.builder()
                .refereeId(refereeId)
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .entryDate(request.getEntryDate() != null ? request.getEntryDate() : LocalDate.now())
                .build();
        return toDiaryResponse(diaryRepository.save(entry));
    }

    private Long requireRefereeId() {
        if (!securityUtils.isReferee()) {
            throw new BusinessException("Only referees can access this resource", HttpStatus.FORBIDDEN);
        }
        Long id = securityUtils.currentEntityId();
        if (id == null) {
            throw new BusinessException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return id;
    }

    private void assertAssigned(Fixture fixture, Long refereeId) {
        if (fixture.getReferee() == null || !fixture.getReferee().getRefereeId().equals(refereeId)) {
            throw new BusinessException("You are not assigned to this fixture", HttpStatus.FORBIDDEN);
        }
    }

    private MatchPrepResponse emptyPrep(Long fixtureId, Long refereeId) {
        return MatchPrepResponse.builder()
                .fixtureId(fixtureId)
                .refereeId(refereeId)
                .completedCount(0)
                .totalCount(PREP_TOTAL)
                .build();
    }

    private MatchPrepResponse toPrepResponse(RefereeMatchPrep p) {
        int done = 0;
        if (p.isPitchChecked()) done++;
        if (p.isBallsChecked()) done++;
        if (p.isNetsChecked()) done++;
        if (p.isCaptainsBriefed()) done++;
        if (p.isLineupsReceived()) done++;
        if (p.isMedicalReady()) done++;
        if (p.isSecurityOk()) done++;
        return MatchPrepResponse.builder()
                .id(p.getId())
                .fixtureId(p.getFixture().getId())
                .refereeId(p.getRefereeId())
                .pitchChecked(p.isPitchChecked())
                .ballsChecked(p.isBallsChecked())
                .netsChecked(p.isNetsChecked())
                .captainsBriefed(p.isCaptainsBriefed())
                .lineupsReceived(p.isLineupsReceived())
                .medicalReady(p.isMedicalReady())
                .securityOk(p.isSecurityOk())
                .notes(p.getNotes())
                .completedCount(done)
                .totalCount(PREP_TOTAL)
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private DiaryResponse toDiaryResponse(RefereeDiary d) {
        return DiaryResponse.builder()
                .id(d.getId())
                .title(d.getTitle())
                .body(d.getBody())
                .entryDate(d.getEntryDate())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
