package com.karoldm.k_board_api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private final String email = "user@example.com";

    @BeforeEach
    void setUp() throws Exception {
        // set manually the secret field that is injected by @value
        Field secretField = TokenService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(tokenService, "mySecretKey");
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = tokenService.generateToken(email);

        String subject = tokenService.validateToken(token);

        assertNotNull(subject);
        assertEquals(email, subject);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertThrows(JWTVerificationException.class, () -> {
            tokenService.validateToken(invalidToken);
        });
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        Instant expiredDate = LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        String expiredToken = JWT.create()
                .withSubject(email)
                .withExpiresAt(expiredDate)
                .sign(Algorithm.HMAC256("mySecretKey"));

        assertThrows(JWTVerificationException.class, () -> {
            tokenService.validateToken(expiredToken);
        });
    }
}
