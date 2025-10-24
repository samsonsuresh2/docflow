package com.docflow.docflow_core.service;

import com.docflow.docflow_core.entity.UserAccount;
import com.docflow.docflow_core.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

@Service @RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;

    public UserAccount validate(String username, String password) {
        log.trace("Entering validate() username={}", username);
        return repo.findByUsernameAndActiveTrue(username)
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .map(u -> { log.info("User {} authenticated", username); return u; })
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
    }
}
