package com.votingapp.voting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long verifiedUsers;
    private long activeUsers;
    private long blockedUsers;
    private long totalElections;
    private long activeElections;
    private long completedElections;
    private long totalVotes;
}
