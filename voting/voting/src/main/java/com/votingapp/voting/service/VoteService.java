package com.votingapp.voting.service;

import com.votingapp.voting.dto.request.VoteRequest;
import com.votingapp.voting.dto.response.VoteResponse;
import com.votingapp.voting.dto.response.VotingStatusResponse;
import com.votingapp.voting.entity.Candidate;
import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.Vote;
import com.votingapp.voting.entity.VoterParticipation;
import com.votingapp.voting.entity.enums.CandidateStatus;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.exception.ConflictException;
import com.votingapp.voting.repository.CandidateRepository;
import com.votingapp.voting.repository.VoteRepository;
import com.votingapp.voting.repository.VoterParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Casts votes using the privacy-oriented design: VoterParticipation records
 * WHO voted (for eligibility + duplicate-prevention), Vote records WHAT was
 * voted (candidate) with no link back to the voter. See entity/Vote.java.
 */
@Service
@RequiredArgsConstructor
public class VoteService {

    private final ElectionService electionService;
    private final CandidateRepository candidateRepository;
    private final VoterParticipationRepository participationRepository;
    private final VoteRepository voteRepository;
    private final AuditService auditService;

    @Transactional
    public VoteResponse castVote(User user, Long electionId, VoteRequest req) {
        Election election = electionService.getOrThrow(electionId);

        if (election.getStatus() != ElectionStatus.ACTIVE) {
            throw new BadRequestException("Voting is not currently open for this election (status: "
                    + election.getStatus() + ")");
        }

        Candidate candidate = candidateRepository.findById(req.getCandidateId())
                .orElseThrow(() -> new BadRequestException("Selected candidate does not exist"));

        if (!candidate.getElection().getId().equals(election.getId()) || candidate.getStatus() != CandidateStatus.ACTIVE) {
            throw new BadRequestException("Selected candidate does not belong to this election");
        }

        VoterParticipation participation = participationRepository.findByUserAndElection(user, election)
                .orElseGet(() -> VoterParticipation.builder().user(user).election(election).hasVoted(false).build());

        if (participation.isHasVoted()) {
            throw new ConflictException("You have already voted in this election");
        }

        participation.setHasVoted(true);
        participation.setVotedAt(LocalDateTime.now());

        try {
            participationRepository.saveAndFlush(participation);
        } catch (DataIntegrityViolationException ex) {
            // Unique constraint on (user_id, election_id) is the final guard against a race
            // between two simultaneous requests from the same voter.
            throw new ConflictException("You have already voted in this election");
        }

        String reference = "VOTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Vote vote = Vote.builder()
                .election(election)
                .candidate(candidate)
                .voteReference(reference)
                .votedAt(LocalDateTime.now())
                .build();
        voteRepository.save(vote);

        // Audit only records THAT this user voted (participation), never the candidate choice.
        auditService.log("VOTE_CAST", "VOTE", "Vote cast in election: " + election.getTitle(), user.getId(), null);

        return VoteResponse.builder()
                .voteReference(reference)
                .electionTitle(election.getTitle())
                .votedAt(vote.getVotedAt())
                .build();
    }

    public VotingStatusResponse votingStatus(User user, Long electionId) {
        Election election = electionService.getOrThrow(electionId);
        boolean hasVoted = participationRepository.findByUserAndElection(user, election)
                .map(VoterParticipation::isHasVoted)
                .orElse(false);
        return VotingStatusResponse.builder()
                .hasVoted(hasVoted)
                .electionOpenForVoting(election.getStatus() == ElectionStatus.ACTIVE)
                .build();
    }
}
