package com.votingapp.voting.service;

import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Moves elections through UPCOMING -> ACTIVE -> CLOSED automatically based on
 * their configured start/end date-time. DRAFT, CANCELLED and RESULT_DECLARED
 * are never touched here - those require an explicit admin action.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElectionSchedulerService {

    private final ElectionRepository electionRepository;
    private final AuditService auditService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void transitionElectionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        List<Election> toActivate = electionRepository
                .findByStatusAndStartDateTimeLessThanEqual(ElectionStatus.UPCOMING, now);
        for (Election e : toActivate) {
            e.setStatus(ElectionStatus.ACTIVE);
            electionRepository.save(e);
            auditService.log("ELECTION_STARTED", "ELECTION", "Election auto-started: " + e.getTitle());
            log.info("Election '{}' (id={}) is now ACTIVE", e.getTitle(), e.getId());
        }

        List<Election> toClose = electionRepository
                .findByStatusAndEndDateTimeLessThanEqual(ElectionStatus.ACTIVE, now);
        for (Election e : toClose) {
            e.setStatus(ElectionStatus.CLOSED);
            electionRepository.save(e);
            auditService.log("ELECTION_CLOSED", "ELECTION", "Election auto-closed: " + e.getTitle());
            log.info("Election '{}' (id={}) is now CLOSED", e.getTitle(), e.getId());
        }
    }
}
