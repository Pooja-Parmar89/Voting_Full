package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {
    private String voteReference;
    private String electionTitle;
    private LocalDateTime votedAt;
}
