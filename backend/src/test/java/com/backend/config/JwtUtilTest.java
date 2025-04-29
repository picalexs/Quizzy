package com.backend.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Act
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(TEST_EMAIL, jwtUtil.getSubjectFromToken(token));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Act & Assert
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Create an expired token directly instead of trying to modify the expiration time
        // Get the SECRET_KEY using reflection for this test
        try {
            // Create a token that's already expired
            Date issuedAt = new Date(System.currentTimeMillis() - 2000); // 2 seconds ago
            Date expiration = new Date(System.currentTimeMillis() - 1000); // 1 second ago (already expired)
            
            // Access the private SECRET_KEY field using reflection
            java.lang.reflect.Field secretKeyField = JwtUtil.class.getDeclaredField("SECRET_KEY");
            secretKeyField.setAccessible(true);
            SecretKey secretKey = (SecretKey) secretKeyField.get(null);
            
            // Generate an expired token
            String expiredToken = Jwts.builder()
                    .setSubject(TEST_EMAIL)
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiration)
                    .signWith(secretKey)
                    .compact();

            // Verify that our token is detected as invalid
            assertFalse(jwtUtil.validateToken(expiredToken));
            
        } catch (Exception e) {
            fail("Test failed due to exception: " + e.getMessage());
        }
    }

    @Test
    void getSubjectFromToken_WithValidToken_ShouldReturnSubject() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_EMAIL);

        // Act
        String subject = jwtUtil.getSubjectFromToken(token);

        // Assert
        assertEquals(TEST_EMAIL, subject);
    }

    @Test
    void getSubjectFromToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            jwtUtil.getSubjectFromToken(invalidToken);
        });

        assertTrue(exception.getMessage().contains("Invalid"));
    }
}