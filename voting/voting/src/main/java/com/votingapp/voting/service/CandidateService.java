package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.CandidateRequest;
import com.votingapp.voting.dto.response.CandidateResponse;
import com.votingapp.voting.entity.Candidate;
import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.CandidateStatus;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.exception.ResourceNotFoundException;
import com.votingapp.voting.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionService electionService;
    private final AuditService auditService;

    public List<CandidateResponse> listPublic(Long electionId) {
        Election election = electionService.getOrThrow(electionId);
        return candidateRepository.findByElectionAndStatusOrderByIdAsc(election, CandidateStatus.ACTIVE).stream()
                .map(CandidateResponse::from)
                .toList();
    }

    public List<CandidateResponse> listForAdmin(Long electionId) {
        Election election = electionService.getOrThrow(electionId);
        return candidateRepository.findByElectionOrderByIdAsc(election).stream()
                .map(CandidateResponse::from)
                .toList();
    }

    @Transactional
    public CandidateResponse create(Long electionId, CandidateRequest req) {
        Election election = electionService.getOrThrow(electionId);
        assertEditable(election);

        Candidate candidate = Candidate.builder()
                .election(election)
                .name(req.getName())
                .description(req.getDescription())
                .party(req.getParty())
                .symbolUrl(req.getSymbolUrl())
                .imageUrl(req.getImageUrl())
                .status(CandidateStatus.ACTIVE)
                .build();
        candidate = candidateRepository.save(candidate);
        auditService.log("CANDIDATE_CREATED", "CANDIDATE",
                "Candidate '" + candidate.getName() + "' added to election '" + election.getTitle() + "'", null, null);
        return CandidateResponse.from(candidate);
    }

    @Transactional
    public CandidateResponse update(Long candidateId, CandidateRequest req) {
        Candidate candidate = getOrThrow(candidateId);
        assertEditable(candidate.getElection());

        candidate.setName(req.getName());
        candidate.setDescription(req.getDescription());
        candidate.setParty(req.getParty());
        candidate.setSymbolUrl(req.getSymbolUrl());
        candidate.setImageUrl(req.getImageUrl());
        candidate = candidateRepository.save(candidate);

        auditService.log("CANDIDATE_UPDATED", "CANDIDATE", "Candidate updated: " + candidate.getName(), null, null);
        return CandidateResponse.from(candidate);
    }

    @Transactional
    public void remove(Long candidateId) {
        Candidate candidate = getOrThrow(candidateId);
        assertEditable(candidate.getElection());
        candidate.setStatus(CandidateStatus.REMOVED);
        candidateRepository.save(candidate);
        auditService.log("CANDIDATE_DELETED", "CANDIDATE", "Candidate removed: " + candidate.getName(), null, null);
    }

    private Candidate getOrThrow(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
    }

    private void assertEditable(Election election) {
        if (election.getStatus() != ElectionStatus.DRAFT && election.getStatus() != ElectionStatus.UPCOMING) {
            throw new BadRequestException("Candidates can only be modified while the election is DRAFT or UPCOMING");
        }
    }
}
