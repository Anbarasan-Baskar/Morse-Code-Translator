package com.example.morse.service;

import com.example.morse.domain.AppUser;
import com.example.morse.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AccountService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String usernameInput, String password) {
        String username = usernameInput.strip().toLowerCase(Locale.ROOT);
        if (!username.matches("[a-z0-9_-]{3,32}")) {
            throw new IllegalArgumentException("Use 3–32 letters, numbers, underscores, or hyphens for your username.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be no more than 72 bytes.");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        return users.save(new AppUser(username, passwordEncoder.encode(password)));
    }
}
