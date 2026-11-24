package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.LoginRequest;
import com.netballmanager.netballmanager.dto.RegisterRequest;
import com.netballmanager.netballmanager.dto.TokenResponse;
import com.netballmanager.netballmanager.domain.NetballUser;
import com.netballmanager.netballmanager.domain.UserRole;
import com.netballmanager.netballmanager.exception.ConflictException;
import com.netballmanager.netballmanager.repository.NetballUserRepository;
import com.netballmanager.netballmanager.repository.PlayerRepository;
import com.netballmanager.netballmanager.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final NetballUserRepository users;
    private final PlayerRepository players;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokens;

    public AuthService(NetballUserRepository users, PlayerRepository players, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenService tokens) {
        this.users = users;
        this.players = players;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokens = tokens;
    }

    public TokenResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with that email already exists");
        }
        NetballUser user = DomainFactory.createUser(email, passwordEncoder.encode(request.password()), UserRole.PLAYER);
        players.findByEmailIgnoreCase(email).ifPresent(user::setPlayer);
        users.save(user);
        return login(new LoginRequest(email, request.password()));
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        NetballUser user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));
        return tokens.issue(user);
    }
}
