package com.votingapp.voting.dto.response;

import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.entity.enums.ResultVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResponse {
    private Long id;
    private String title;
    private String description;
    private String electionType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ElectionStatus status;
    private ResultVisibility resultVisibility;
    private long candidateCount;

    public static ElectionResponse from(Election e, long candidateCount) {
        return ElectionResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .electionType(e.getElectionType())
                .startDateTime(e.getStartDateTime())
                .endDateTime(e.getEndDateTime())
                .status(e.getStatus())
                .resultVisibility(e.getResultVisibility())
                .candidateCount(candidateCount)
                .build();
    }
}
