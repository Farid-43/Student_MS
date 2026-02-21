package com.example.StudentMS.security;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "testSecretKeyForJWTTokenGenerationStudentMS2024SecureKeyTestKey123456";
    private static final int EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION);
    }

    private Authentication createAuthentication(String username) {
        UserDetails userDetails = new User(username, "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate a valid JWT token")
        void shouldGenerateToken() {
            Authentication auth = createAuthentication("admin");

            String token = jwtTokenProvider.generateToken(auth);

            assertThat(token).isNotNull().isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
        }

        @Test
        @DisplayName("should include username in token")
        void shouldIncludeUsername() {
            Authentication auth = createAuthentication("testuser");

            String token = jwtTokenProvider.generateToken(auth);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateToken {

        @Test
        @DisplayName("should validate a correct token")
        void shouldValidateCorrectToken() {
            Authentication auth = createAuthentication("admin");
            String token = jwtTokenProvider.generateToken(auth);

            boolean isValid = jwtTokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should reject an invalid token")
        void shouldRejectInvalidToken() {
            boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should reject an expired token")
        void shouldRejectExpiredToken() {
            // Create a token with -1ms expiration (already expired)
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
            String expiredToken = Jwts.builder()
                    .subject("admin")
                    .issuedAt(new Date(System.currentTimeMillis() - 100000))
                    .expiration(new Date(System.currentTimeMillis() - 50000))
                    .signWith(key)
                    .compact();

            boolean isValid = jwtTokenProvider.validateToken(expiredToken);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should reject null token")
        void shouldRejectNullToken() {
            boolean isValid = jwtTokenProvider.validateToken(null);

            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("getUsernameFromToken")
    class GetUsernameFromToken {

        @Test
        @DisplayName("should extract correct username")
        void shouldExtractUsername() {
            Authentication auth = createAuthentication("student1");
            String token = jwtTokenProvider.generateToken(auth);

            String username = jwtTokenProvider.getUsernameFromToken(token);

            assertThat(username).isEqualTo("student1");
        }
    }
}
