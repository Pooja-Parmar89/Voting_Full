package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.BlockUserRequest;
import com.votingapp.voting.dto.response.*;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.entity.enums.UserStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.exception.ResourceNotFoundException;
import com.votingapp.voting.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;
    private final LoginLogRepository loginLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public DashboardStatsResponse getDashboardStats() {
        long totalVotes = electionRepository.findAll().stream()
                .mapToLong(voteRepository::countByElection)
                .sum();

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .verifiedUsers(userRepository.countByEmailVerifiedTrueAndMobileVerifiedTrue())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .totalElections(electionRepository.count())
                .activeElections(electionRepository.countByStatus(ElectionStatus.ACTIVE))
                .completedElections(electionRepository.countByStatusIn(
                        List.of(ElectionStatus.CLOSED, ElectionStatus.RESULT_DECLARED)))
                .totalVotes(totalVotes)
                .build();
    }

    public Page<UserResponse> searchUsers(UserStatus status, String search, Pageable pageable) {
        return userRepository.search(status, search, pageable).map(UserResponse::from);
    }

    @Transactional
    public void blockUser(Long userId, BlockUserRequest req) {
        User user = getOrThrow(userId);
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        auditService.log("USER_BLOCKED", "USER",
                "User blocked: " + user.getEmail() + (req.getReason() != null ? " | reason: " + req.getReason() : ""),
                user.getId(), null);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = getOrThrow(userId);
        if (!user.isFullyVerified()) {
            throw new BadRequestException("Cannot activate a user who has not completed verification");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        auditService.log("USER_UNBLOCKED", "USER", "User unblocked: " + user.getEmail(), user.getId(), null);
    }

    public Page<LoginLogResponse> getLoginLogs(Pageable pageable) {
        return loginLogRepository.findAllByOrderByLoginTimeDesc(pageable).map(LoginLogResponse::from);
    }

    public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedDateDesc(pageable).map(AuditLogResponse::from);
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
