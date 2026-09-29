package com.votingapp.voting.repository;

import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobile(String mobile);

    @Query("select u from User u where u.email = :identifier or u.mobile = :identifier")
    Optional<User> findByEmailOrMobile(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    @Query("select u from User u where "
            + "(:status is null or u.status = :status) and "
            + "(:search is null or lower(u.fullName) like lower(concat('%', :search, '%')) "
            + "  or lower(u.email) like lower(concat('%', :search, '%')) "
            + "  or u.mobile like concat('%', :search, '%'))")
    Page<User> search(@Param("status") UserStatus status, @Param("search") String search, Pageable pageable);

    long countByStatus(UserStatus status);

    long countByEmailVerifiedTrueAndMobileVerifiedTrue();
}
