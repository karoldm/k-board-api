package com.karoldm.k_board_api.services;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withSubject(email)
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch(JWTCreationException exception) {
            throw new RuntimeException("Error while generating token: " + exception);
        }
    }

    public String validateToken(String token) {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.require(algorithm)
                    .build()
                    .verify(token)
                    .getSubject();
    }

    public Instant generateExpirationDate() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toInstant();
    }
}
