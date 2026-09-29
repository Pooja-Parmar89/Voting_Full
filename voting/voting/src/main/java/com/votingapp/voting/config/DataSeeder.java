package com.votingapp.voting.config;

import com.votingapp.voting.entity.User;
import com.votingapp.voting.entity.enums.UserRole;
import com.votingapp.voting.entity.enums.UserStatus;
import com.votingapp.voting.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        User admin = User.builder()
                .fullName("System Administrator")
                .email(adminEmail)
                .mobile("9999999999")
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .mobileVerified(true)
                .build();
        userRepository.save(admin);
        log.info("==============================================================");
        log.info("Seeded default admin account -> email: {} | password: {}", adminEmail, adminPassword);
        log.info("Change this password after first login. Configurable via app.admin.* properties.");
        log.info("==============================================================");
    }
}
