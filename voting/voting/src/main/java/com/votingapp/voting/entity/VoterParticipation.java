package com.votingapp.voting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks WHETHER a voter has participated in an election, completely
 * separate from the Vote (ballot) table below. This is what lets us avoid
 * ever storing "user X voted for candidate Y" in one row - see Vote.java
 * and the README for the privacy trade-off explanation.
 */
@Entity
@Table(name = "voter_participation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_participation_user_election", columnNames = {"user_id", "election_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoterParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Column(name = "has_voted", nullable = false)
    private boolean hasVoted;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;
}
