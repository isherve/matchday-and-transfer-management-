package com.ferwafa.referee;

import com.ferwafa.common.BusinessException;
import com.ferwafa.referee.dto.RefereeRequest;
import com.ferwafa.referee.dto.RefereeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefereeService {

    private final RefereeRepository refereeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<RefereeResponse> findAll() {
        return refereeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public RefereeResponse create(RefereeRequest request) {
        if (request.getAccessCode() == null || request.getAccessCode().isBlank()) {
            throw new BusinessException("Access code is required");
        }
        if (refereeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email already registered");
        }
        Referee referee = Referee.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .image(request.getImage())
                .email(request.getEmail())
                .accessCodeHash(passwordEncoder.encode(request.getAccessCode()))
                .build();
        return toResponse(refereeRepository.save(referee));
    }

    @Transactional
    public RefereeResponse update(Long id, RefereeRequest request) {
        Referee referee = getReferee(id);
        referee.setFname(request.getFname());
        referee.setLname(request.getLname());
        referee.setImage(request.getImage());
        referee.setEmail(request.getEmail());
        if (request.getAccessCode() != null && !request.getAccessCode().isBlank()) {
            referee.setAccessCodeHash(passwordEncoder.encode(request.getAccessCode()));
        }
        return toResponse(refereeRepository.save(referee));
    }

    public Referee getReferee(Long id) {
        return refereeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Referee not found", HttpStatus.NOT_FOUND));
    }

    private RefereeResponse toResponse(Referee referee) {
        return RefereeResponse.builder()
                .refereeId(referee.getRefereeId())
                .fname(referee.getFname())
                .lname(referee.getLname())
                .image(referee.getImage())
                .email(referee.getEmail())
                .build();
    }
}
