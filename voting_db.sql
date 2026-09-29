-- =====================================================================
-- Online Voting System - Full Database Schema
-- MySQL 8+
--
-- NOTE: spring.jpa.hibernate.ddl-auto=update will also auto-create/alter
-- these tables on application startup from the JPA entities. This script
-- is provided for reference and for anyone who prefers to set up the
-- schema manually / inspect it without running the app first.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS online_voting_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE online_voting_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS voter_participation;
DROP TABLE IF EXISTS candidates;
DROP TABLE IF EXISTS elections;
DROP TABLE IF EXISTS otp_verifications;
DROP TABLE IF EXISTS login_logs;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(150)  NOT NULL,
    email             VARCHAR(150)  NOT NULL,
    mobile            VARCHAR(20)   NOT NULL,
    password          VARCHAR(255)  NOT NULL,
    voter_ref_id      VARCHAR(50),
    role              VARCHAR(20)   NOT NULL,               -- ADMIN, VOTER
    status            VARCHAR(30)   NOT NULL,               -- PENDING_VERIFICATION, ACTIVE, BLOCKED
    email_verified    BIT(1)        NOT NULL DEFAULT 0,
    mobile_verified   BIT(1)        NOT NULL DEFAULT 0,
    created_date      DATETIME(6)   NOT NULL,
    created_by        VARCHAR(100),
    updated_date      DATETIME(6),
    updated_by        VARCHAR(100),
    CONSTRAINT uk_users_email  UNIQUE (email),
    CONSTRAINT uk_users_mobile UNIQUE (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- otp_verifications
-- Generic table for EMAIL_VERIFICATION / MOBILE_VERIFICATION / LOGIN_OTP /
-- PASSWORD_RESET. Only a BCrypt hash of the OTP is ever stored.
-- ---------------------------------------------------------------------
CREATE TABLE otp_verifications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier    VARCHAR(150) NOT NULL,
    otp_type      VARCHAR(30)  NOT NULL,
    otp_hash      VARCHAR(255) NOT NULL,
    user_id       BIGINT,
    created_date  DATETIME(6)  NOT NULL,
    expiry_date   DATETIME(6)  NOT NULL,
    attempts      INT          NOT NULL DEFAULT 0,
    max_attempts  INT          NOT NULL DEFAULT 5,
    verified      BIT(1)       NOT NULL DEFAULT 0,
    KEY idx_otp_identifier_type (identifier, otp_type),
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- login_logs  (one row per login attempt - never overwritten)
-- ---------------------------------------------------------------------
CREATE TABLE login_logs (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id               BIGINT,
    attempted_identifier  VARCHAR(150),
    ip_address            VARCHAR(64),
    user_agent            VARCHAR(500),
    browser               VARCHAR(100),
    operating_system      VARCHAR(100),
    login_time            DATETIME(6) NOT NULL,
    logout_time           DATETIME(6),
    status                VARCHAR(20) NOT NULL,   -- SUCCESS, FAILED
    failure_reason        VARCHAR(255),
    KEY idx_login_logs_user (user_id),
    CONSTRAINT fk_login_logs_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- audit_logs  (centralized audit trail - never stores secrets)
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT,
    action        VARCHAR(60)  NOT NULL,
    module        VARCHAR(60)  NOT NULL,
    description   VARCHAR(500),
    ip_address    VARCHAR(64),
    created_date  DATETIME(6)  NOT NULL,
    created_by    VARCHAR(100),
    KEY idx_audit_logs_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- elections
-- ---------------------------------------------------------------------
CREATE TABLE elections (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(200)  NOT NULL,
    description        VARCHAR(2000),
    election_type      VARCHAR(50),
    start_date_time    DATETIME(6)   NOT NULL,
    end_date_time      DATETIME(6)   NOT NULL,
    status             VARCHAR(30)   NOT NULL,   -- DRAFT, UPCOMING, ACTIVE, CLOSED, RESULT_DECLARED, CANCELLED
    result_visibility  VARCHAR(20)   NOT NULL,   -- ALWAYS, AFTER_CLOSE
    created_date       DATETIME(6)   NOT NULL,
    created_by         VARCHAR(100),
    updated_date       DATETIME(6),
    updated_by         VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- candidates
-- ---------------------------------------------------------------------
CREATE TABLE candidates (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    election_id   BIGINT        NOT NULL,
    name          VARCHAR(150)  NOT NULL,
    description   VARCHAR(1000),
    party         VARCHAR(150),
    symbol_url    VARCHAR(500),
    image_url     VARCHAR(500),
    status        VARCHAR(20)   NOT NULL,   -- ACTIVE, REMOVED
    created_date  DATETIME(6)   NOT NULL,
    created_by    VARCHAR(100),
    updated_date  DATETIME(6),
    updated_by    VARCHAR(100),
    KEY idx_candidates_election (election_id),
    CONSTRAINT fk_candidates_election FOREIGN KEY (election_id) REFERENCES elections (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- voter_participation
-- Tracks WHO voted (for eligibility + one-vote-per-election enforcement).
-- Deliberately separate from `votes` below - see README "Vote privacy design".
-- ---------------------------------------------------------------------
CREATE TABLE voter_participation (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    election_id  BIGINT      NOT NULL,
    has_voted    BIT(1)      NOT NULL DEFAULT 0,
    voted_at     DATETIME(6),
    CONSTRAINT uk_participation_user_election UNIQUE (user_id, election_id),
    CONSTRAINT fk_participation_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_participation_election FOREIGN KEY (election_id) REFERENCES elections (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- votes
-- The actual ballot: WHAT was voted. Holds NO reference to the voter,
-- so joining votes <-> voter_participation never reveals who voted for whom.
-- ---------------------------------------------------------------------
CREATE TABLE votes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    election_id     BIGINT       NOT NULL,
    candidate_id    BIGINT       NOT NULL,
    vote_reference  VARCHAR(40)  NOT NULL,
    voted_at        DATETIME(6)  NOT NULL,
    CONSTRAINT uk_votes_reference UNIQUE (vote_reference),
    KEY idx_votes_election (election_id),
    KEY idx_votes_candidate (candidate_id),
    CONSTRAINT fk_votes_election  FOREIGN KEY (election_id)  REFERENCES elections (id),
    CONSTRAINT fk_votes_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Default admin account: NOT inserted here.
-- The application seeds it automatically on first run (see
-- config/DataSeeder.java) using app.admin.email / app.admin.password
-- from application.properties (default: admin@votingapp.com / Admin@123).
-- ---------------------------------------------------------------------
