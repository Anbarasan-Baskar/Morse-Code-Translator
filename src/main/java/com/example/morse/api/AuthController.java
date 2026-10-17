package com.example.morse.api;

import com.example.morse.domain.AppUser;
import com.example.morse.security.AppUserDetails;
import com.example.morse.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AccountService accounts;
    private final SecurityContextRepository contextRepository;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(AuthenticationManager authenticationManager, AccountService accounts,
                          SecurityContextRepository contextRepository,
                          CsrfTokenRepository csrfTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.accounts = accounts;
        this.contextRepository = contextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    @GetMapping("/me")
    public UserResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal AppUserDetails user) {
        return new UserResponse(user.getUsername());
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse response) {
        AppUser user = accounts.register(request.username(), request.password());
        return authenticate(user.getUsername(), request.password(), servletRequest, response);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest servletRequest, HttpServletResponse response) {
        return authenticate(request.username().strip().toLowerCase(Locale.ROOT), request.password(),
                servletRequest, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        csrfTokenRepository.saveToken(null, request, response);
        return ResponseEntity.noContent().build();
    }

    private UserResponse authenticate(String username, String password,
                                      HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        request.getSession(true);
        request.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return new UserResponse(authentication.getName());
    }
}
