package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.ElectionRequest;
import com.votingapp.voting.dto.response.ElectionResponse;
import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.CandidateStatus;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.exception.ResourceNotFoundException;
import com.votingapp.voting.repository.CandidateRepository;
import com.votingapp.voting.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final AuditService auditService;

    private static final List<ElectionStatus> PUBLIC_STATUSES =
            List.of(ElectionStatus.UPCOMING, ElectionStatus.ACTIVE, ElectionStatus.CLOSED, ElectionStatus.RESULT_DECLARED);

    public List<ElectionResponse> listPublic() {
        return electionRepository.findByStatusInOrderByStartDateTimeDesc(PUBLIC_STATUSES).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ElectionResponse> listAllForAdmin() {
        return electionRepository.findAll().stream()
                .sorted((a, b) -> b.getStartDateTime().compareTo(a.getStartDateTime()))
                .map(this::toResponse)
                .toList();
    }

    public ElectionResponse getPublic(Long id) {
        Election election = getOrThrow(id);
        if (!PUBLIC_STATUSES.contains(election.getStatus())) {
            throw new ResourceNotFoundException("Election not found");
        }
        return toResponse(election);
    }

    public ElectionResponse getForAdmin(Long id) {
        return toResponse(getOrThrow(id));
    }

    public Election getOrThrow(Long id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found with id: " + id));
    }

    @Transactional
    public ElectionResponse create(ElectionRequest req) {
        validateDates(req.getStartDateTime(), req.getEndDateTime());

        Election election = Election.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .electionType(req.getElectionType())
                .startDateTime(req.getStartDateTime())
                .endDateTime(req.getEndDateTime())
                .resultVisibility(req.getResultVisibility())
                .status(ElectionStatus.DRAFT)
                .build();
        election = electionRepository.save(election);
        auditService.log("ELECTION_CREATED", "ELECTION", "Election created: " + election.getTitle(), null, null);
        return toResponse(election);
    }

    @Transactional
    public ElectionResponse update(Long id, ElectionRequest req) {
        Election election = getOrThrow(id);
        if (election.getStatus() != ElectionStatus.DRAFT && election.getStatus() != ElectionStatus.UPCOMING) {
            throw new BadRequestException("Election details can only be edited while it is in DRAFT or UPCOMING status");
        }
        validateDates(req.getStartDateTime(), req.getEndDateTime());

        election.setTitle(req.getTitle());
        election.setDescription(req.getDescription());
        election.setElectionType(req.getElectionType());
        election.setStartDateTime(req.getStartDateTime());
        election.setEndDateTime(req.getEndDateTime());
        election.setResultVisibility(req.getResultVisibility());
        election = electionRepository.save(election);

        auditService.log("ELECTION_UPDATED", "ELECTION", "Election updated: " + election.getTitle(), null, null);
        return toResponse(election);
    }

    @Transactional
    public ElectionResponse publish(Long id) {
        Election election = getOrThrow(id);
        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new BadRequestException("Only a DRAFT election can be published");
        }
        if (candidateRepository.countByElectionAndStatus(election, CandidateStatus.ACTIVE) < 2) {
            throw new BadRequestException("Add at least 2 candidates before publishing an election");
        }
        election.setStatus(ElectionStatus.UPCOMING);
        electionRepository.save(election);
        auditService.log("ELECTION_UPDATED", "ELECTION", "Election published: " + election.getTitle(), null, null);
        return toResponse(election);
    }

    @Transactional
    public ElectionResponse cancel(Long id) {
        Election election = getOrThrow(id);
        if (election.getStatus() == ElectionStatus.CLOSED || election.getStatus() == ElectionStatus.RESULT_DECLARED) {
            throw new BadRequestException("A closed or result-declared election cannot be cancelled");
        }
        election.setStatus(ElectionStatus.CANCELLED);
        electionRepository.save(election);
        auditService.log("ELECTION_UPDATED", "ELECTION", "Election cancelled: " + election.getTitle(), null, null);
        return toResponse(election);
    }

    @Transactional
    public ElectionResponse declareResults(Long id) {
        Election election = getOrThrow(id);
        if (election.getStatus() != ElectionStatus.CLOSED) {
            throw new BadRequestException("Results can only be declared for a CLOSED election");
        }
        election.setStatus(ElectionStatus.RESULT_DECLARED);
        electionRepository.save(election);
        auditService.log("ELECTION_CLOSED", "ELECTION", "Results declared for: " + election.getTitle(), null, null);
        return toResponse(election);
    }

    @Transactional
    public void delete(Long id) {
        Election election = getOrThrow(id);
        if (election.getStatus() != ElectionStatus.DRAFT) {
            throw new BadRequestException("Only a DRAFT election (no candidates published, no votes) can be deleted");
        }
        electionRepository.delete(election);
        auditService.log("ELECTION_UPDATED", "ELECTION", "Election deleted: " + election.getTitle(), null, null);
    }

    private void validateDates(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (end == null || start == null || !end.isAfter(start)) {
            throw new BadRequestException("End date/time must be after the start date/time");
        }
    }

    private ElectionResponse toResponse(Election e) {
        long count = candidateRepository.countByElectionAndStatus(e, CandidateStatus.ACTIVE);
        return ElectionResponse.from(e, count);
    }
}
