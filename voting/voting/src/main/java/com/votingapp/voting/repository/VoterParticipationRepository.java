package com.votingapp.voting.repository;

import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.VoterParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoterParticipationRepository extends JpaRepository<VoterParticipation, Long> {

    Optional<VoterParticipation> findByUserAndElection(User user, Election election);

    long countByElectionAndHasVotedTrue(Election election);
}
