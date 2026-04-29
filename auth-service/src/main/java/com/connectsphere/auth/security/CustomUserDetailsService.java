package com.connectsphere.auth.security;

import com.connectsphere.auth.exception.NotFoundException;
import com.connectsphere.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username)
                .map(AuthenticatedUser::new)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }
}
