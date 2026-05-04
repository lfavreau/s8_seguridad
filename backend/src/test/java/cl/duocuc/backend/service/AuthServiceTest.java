package cl.duocuc.backend.service;

import cl.duocuc.backend.dto.LoginRequest;
import cl.duocuc.backend.dto.LoginResponse;
import cl.duocuc.backend.entity.AppUser;
import cl.duocuc.backend.repository.AppUserRepository;
import cl.duocuc.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginWithValidCredentialsReturnsJwtResponse() {
        LoginRequest request = loginRequest("admin", "admin123");
        UserDetails userDetails = User.withUsername("admin").password("hash").roles("ADMIN").build();
        AppUser appUser = new AppUser("admin", "hash", "ROLE_ADMIN");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(appUser));
        when(jwtService.generateToken(userDetails, "ROLE_ADMIN")).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("admin", captor.getValue().getPrincipal());
        assertEquals("admin123", captor.getValue().getCredentials());
    }

    @Test
    void loginWithInvalidCredentialsPropagatesAuthenticationError() {
        LoginRequest request = loginRequest("admin", "mala");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(jwtService, never()).generateToken(any(), any());
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
