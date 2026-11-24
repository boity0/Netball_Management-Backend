package com.netballmanager.netballmanager.security;

import com.netballmanager.netballmanager.dto.TokenResponse;
import com.netballmanager.netballmanager.domain.NetballUser;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder;
    private final Duration expiration;

    public JwtTokenService(JwtEncoder encoder,
                           @Value("${app.jwt.expiration:PT12H}") Duration expiration) {
        this.encoder = encoder;
        this.expiration = expiration;
    }

    public TokenResponse issue(NetballUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("netball-manager")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiresAt);
    }
}
