package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResultResponse {
    private Long electionId;
    private String electionTitle;
    private long totalVotesCast;
    private long totalEligibleParticipants;
    private double participationPercentage;
    private List<CandidateResultResponse> candidates;
}
