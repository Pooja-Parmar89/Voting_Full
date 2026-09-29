package com.votingapp.voting.dto.response;

import com.votingapp.voting.entity.Candidate;
import com.votingapp.voting.entity.enums.CandidateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private Long electionId;
    private String name;
    private String description;
    private String party;
    private String symbolUrl;
    private String imageUrl;
    private CandidateStatus status;

    public static CandidateResponse from(Candidate c) {
        return CandidateResponse.builder()
                .id(c.getId())
                .electionId(c.getElection().getId())
                .name(c.getName())
                .description(c.getDescription())
                .party(c.getParty())
                .symbolUrl(c.getSymbolUrl())
                .imageUrl(c.getImageUrl())
                .status(c.getStatus())
                .build();
    }
}
