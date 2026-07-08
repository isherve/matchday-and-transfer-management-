package com.ferwafa.transfer;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.TransferStatus;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.member.MemberService;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.notification.NotificationService;
import com.ferwafa.team.TeamService;
import com.ferwafa.transfer.dto.TransferRequest;
import com.ferwafa.transfer.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final String DEFAULT_SEASON = "2025/26";

    private final TransferRepository transferRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberService memberService;
    private final TeamService teamService;
    private final SecurityUtils securityUtils;
    private final TransferWindowService transferWindowService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<TransferResponse> findAll() {
        if (securityUtils.isTeam()) {
            return transferRepository.findByTeamInvolved(securityUtils.currentEntityId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return transferRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransferResponse create(TransferRequest request) {
        transferWindowService.assertTransferWindowOpen(DEFAULT_SEASON);

        var member = memberService.getMember(request.getMemberId());
        var teamFrom = teamService.getTeam(request.getTeamFromId());
        var teamTo = teamService.getTeam(request.getTeamToId());

        if (!member.getTeam().getTeamId().equals(request.getTeamFromId())) {
            throw new BusinessException("Player does not belong to the source team");
        }

        if (securityUtils.isTeam()) {
            Long currentTeamId = securityUtils.currentEntityId();
            if (!currentTeamId.equals(request.getTeamFromId()) && !currentTeamId.equals(request.getTeamToId())) {
                throw new BusinessException("You can only initiate transfers involving your team", HttpStatus.FORBIDDEN);
            }
        }

        Transfer transfer = Transfer.builder()
                .member(member)
                .teamFrom(teamFrom)
                .teamTo(teamTo)
                .post(request.getPost() != null ? request.getPost() : member.getPost())
                .requestDate(LocalDate.now())
                .status(TransferStatus.REQUESTED)
                .build();
        Transfer saved = transferRepository.save(transfer);

        notificationService.notify(UserRole.TEAM, teamTo.getTeamId(),
                "Incoming transfer request",
                member.getFname() + " " + member.getLname() + " from " + teamFrom.getName(),
                NotificationType.TRANSFER_REQUESTED, "TRANSFER", saved.getId());
        notificationService.notify(UserRole.ADMIN, 1L,
                "New transfer request",
                member.getFname() + " " + member.getLname() + ": "
                        + teamFrom.getName() + " → " + teamTo.getName(),
                NotificationType.TRANSFER_REQUESTED, "TRANSFER", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public TransferResponse approve(Long id) {
        Transfer transfer = getTransfer(id);
        assertReceivingTeam(transfer);
        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new BusinessException("Only requested transfers can be approved");
        }
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovalDate(LocalDate.now());
        Transfer saved = transferRepository.save(transfer);

        notificationService.notify(UserRole.TEAM, saved.getTeamFrom().getTeamId(),
                "Transfer approved",
                saved.getMember().getFname() + " " + saved.getMember().getLname()
                        + " transfer to " + saved.getTeamTo().getName() + " was approved",
                NotificationType.TRANSFER_APPROVED, "TRANSFER", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public TransferResponse reject(Long id) {
        Transfer transfer = getTransfer(id);
        assertReceivingTeam(transfer);
        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new BusinessException("Only requested transfers can be rejected");
        }
        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setRejectedDate(LocalDate.now());
        Transfer saved = transferRepository.save(transfer);

        notificationService.notify(UserRole.TEAM, saved.getTeamFrom().getTeamId(),
                "Transfer rejected",
                saved.getMember().getFname() + " " + saved.getMember().getLname()
                        + " transfer to " + saved.getTeamTo().getName() + " was rejected",
                NotificationType.TRANSFER_REJECTED, "TRANSFER", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public TransferResponse complete(Long id) {
        Transfer transfer = getTransfer(id);
        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new BusinessException("Only approved transfers can be completed");
        }
        if (securityUtils.isTeam() && !securityUtils.isAdmin()) {
            throw new BusinessException("Only admin can complete transfers", HttpStatus.FORBIDDEN);
        }
        var member = transfer.getMember();
        member.setTeam(transfer.getTeamTo());
        teamMemberRepository.save(member);
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedDate(LocalDate.now());
        Transfer saved = transferRepository.save(transfer);

        notificationService.notify(UserRole.TEAM, saved.getTeamFrom().getTeamId(),
                "Transfer completed",
                member.getFname() + " " + member.getLname() + " has transferred to "
                        + saved.getTeamTo().getName(),
                NotificationType.TRANSFER_COMPLETED, "TRANSFER", saved.getId());
        notificationService.notify(UserRole.TEAM, saved.getTeamTo().getTeamId(),
                "Transfer completed",
                member.getFname() + " " + member.getLname() + " has joined your club",
                NotificationType.TRANSFER_COMPLETED, "TRANSFER", saved.getId());

        return toResponse(saved);
    }

    private void assertReceivingTeam(Transfer transfer) {
        if (securityUtils.isAdmin()) return;
        if (securityUtils.isTeam() && transfer.getTeamTo().getTeamId().equals(securityUtils.currentEntityId())) return;
        throw new BusinessException("Only the receiving team can approve/reject", HttpStatus.FORBIDDEN);
    }

    private Transfer getTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Transfer not found", HttpStatus.NOT_FOUND));
    }

    private TransferResponse toResponse(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .memberId(transfer.getMember().getMemberId())
                .memberName(transfer.getMember().getFname() + " " + transfer.getMember().getLname())
                .teamFromId(transfer.getTeamFrom().getTeamId())
                .teamFromName(transfer.getTeamFrom().getName())
                .teamToId(transfer.getTeamTo().getTeamId())
                .teamToName(transfer.getTeamTo().getName())
                .post(transfer.getPost())
                .requestDate(transfer.getRequestDate())
                .approvalDate(transfer.getApprovalDate())
                .rejectedDate(transfer.getRejectedDate())
                .completedDate(transfer.getCompletedDate())
                .status(transfer.getStatus())
                .build();
    }
}
