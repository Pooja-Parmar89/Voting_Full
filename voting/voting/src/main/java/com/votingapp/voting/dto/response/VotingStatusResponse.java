package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotingStatusResponse {
    private boolean hasVoted;
    private boolean electionOpenForVoting;
}
