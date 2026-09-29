package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResultResponse {
    private Long candidateId;
    private String candidateName;
    private String party;
    private long totalVotes;
    private double percentage;
}
