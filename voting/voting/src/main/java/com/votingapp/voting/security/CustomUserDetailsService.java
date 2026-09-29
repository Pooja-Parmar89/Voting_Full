package com.votingapp.voting.security;

import com.votingapp.voting.entity.User;
import com.votingapp.voting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrMobile(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        return new CustomUserPrincipal(user);
    }
}
