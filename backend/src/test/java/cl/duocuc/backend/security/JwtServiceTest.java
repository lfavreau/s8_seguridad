package cl.duocuc.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secret", "supersecreto_supersecreto_supersecreto_2026_supersecreto");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    @Test
    void generateTokenIncludesUsernameAndRole() {
        UserDetails userDetails = User.withUsername("admin").password("hash").roles("ADMIN").build();

        String token = jwtService.generateToken(userDetails, "ROLE_ADMIN");

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ROLE_ADMIN", jwtService.extractRole(token));
    }

    @Test
    void tokenIsValidOnlyForSameUser() {
        UserDetails admin = User.withUsername("admin").password("hash").roles("ADMIN").build();
        UserDetails gestor = User.withUsername("gestor").password("hash").roles("GESTOR").build();
        String token = jwtService.generateToken(admin, "ROLE_ADMIN");

        assertTrue(jwtService.isTokenValid(token, admin));
        assertFalse(jwtService.isTokenValid(token, gestor));
    }
}
