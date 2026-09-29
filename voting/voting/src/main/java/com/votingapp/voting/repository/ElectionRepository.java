package com.votingapp.voting.repository;

import com.votingapp.voting.entity.Election;
import com.votingapp.voting.entity.enums.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ElectionRepository extends JpaRepository<Election, Long> {

    List<Election> findByStatusOrderByStartDateTimeAsc(ElectionStatus status);

    List<Election> findByStatusInOrderByStartDateTimeDesc(List<ElectionStatus> statuses);

    List<Election> findByStatusAndStartDateTimeLessThanEqual(ElectionStatus status, LocalDateTime now);

    List<Election> findByStatusAndEndDateTimeLessThanEqual(ElectionStatus status, LocalDateTime now);

    long countByStatus(ElectionStatus status);

    long countByStatusIn(List<ElectionStatus> statuses);
}
