package com.votingapp.voting.entity;

import com.votingapp.voting.entity.enums.ElectionStatus;
import com.votingapp.voting.entity.enums.ResultVisibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "elections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Election extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "election_type", length = 50)
    private String electionType;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ElectionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_visibility", nullable = false, length = 20)
    private ResultVisibility resultVisibility;
}
