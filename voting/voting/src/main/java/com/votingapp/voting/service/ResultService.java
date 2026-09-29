package com.votingapp.voting.service;

import com.votingapp.voting.dto.response.CandidateResultResponse;
import com.votingapp.voting.dto.response.ElectionResultResponse;
import com.votingapp.voting.entity.Candidate;
import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.CandidateStatus;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.entity.enums.ResultVisibility;
import com.votingapp.voting.entity.enums.UserStatus;
import com.votingapp.voting.exception.BadRequestException;
import com.votingapp.voting.repository.CandidateRepository;
import com.votingapp.voting.repository.UserRepository;
import com.votingapp.voting.repository.VoteRepository;
import com.votingapp.voting.repository.VoterParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final ElectionService electionService;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final VoterParticipationRepository participationRepository;
    private final UserRepository userRepository;

    public ElectionResultResponse getResults(Long electionId, boolean isAdmin) {
        Election election = electionService.getOrThrow(electionId);

        if (!isAdmin && !isVisibleToPublic(election)) {
            throw new BadRequestException("Results for this election have not been made available yet");
        }

        List<Candidate> candidates = candidateRepository
                .findByElectionAndStatusOrderByIdAsc(election, CandidateStatus.ACTIVE);
        long totalVotes = voteRepository.countByElection(election);
        long totalParticipants = participationRepository.countByElectionAndHasVotedTrue(election);
        long eligibleVoters = userRepository.countByStatus(UserStatus.ACTIVE);

        List<CandidateResultResponse> results = candidates.stream()
                .map(c -> {
                    long count = voteRepository.countByElectionAndCandidate(election, c);
                    double pct = totalVotes == 0 ? 0.0 : Math.round((count * 10000.0) / totalVotes) / 100.0;
                    return CandidateResultResponse.builder()
                            .candidateId(c.getId())
                            .candidateName(c.getName())
                            .party(c.getParty())
                            .totalVotes(count)
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparingLong(CandidateResultResponse::getTotalVotes).reversed())
                .toList();

        double participationPct = eligibleVoters == 0 ? 0.0
                : Math.round((totalParticipants * 10000.0) / eligibleVoters) / 100.0;

        return ElectionResultResponse.builder()
                .electionId(election.getId())
                .electionTitle(election.getTitle())
                .totalVotesCast(totalVotes)
                .totalEligibleParticipants(eligibleVoters)
                .participationPercentage(participationPct)
                .candidates(results)
                .build();
    }

    private boolean isVisibleToPublic(Election election) {
        if (election.getResultVisibility() == ResultVisibility.ALWAYS) {
            return election.getStatus() == ElectionStatus.ACTIVE
                    || election.getStatus() == ElectionStatus.CLOSED
                    || election.getStatus() == ElectionStatus.RESULT_DECLARED;
        }
        return election.getStatus() == ElectionStatus.CLOSED || election.getStatus() == ElectionStatus.RESULT_DECLARED;
    }
}
