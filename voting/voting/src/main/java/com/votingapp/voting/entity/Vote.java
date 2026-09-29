package com.votingapp.voting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Privacy-oriented ballot record: deliberately holds NO reference to the
 * voter. Who voted is recorded in VoterParticipation; what was voted is
 * recorded here. Joining the two tables never tells you who voted for whom.
 */
@Entity
@Table(name = "votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "vote_reference", nullable = false, unique = true, length = 40)
    private String voteReference;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;
}
