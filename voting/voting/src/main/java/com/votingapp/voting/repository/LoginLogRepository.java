package com.votingapp.voting.repository;

import com.votingapp.voting.entity.LoginLog;
import com.votingapp.voting.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    Page<LoginLog> findByUserOrderByLoginTimeDesc(User user, Pageable pageable);

    Page<LoginLog> findAllByOrderByLoginTimeDesc(Pageable pageable);

    Optional<LoginLog> findFirstByUserAndLogoutTimeIsNullOrderByLoginTimeDesc(User user);
}
