package com.votingapp.voting.repository;

import com.votingapp.voting.entity.Candidate;
import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByElectionAndStatusOrderByIdAsc(Election election, CandidateStatus status);

    List<Candidate> findByElectionOrderByIdAsc(Election election);

    long countByElectionAndStatus(Election election, CandidateStatus status);
}
