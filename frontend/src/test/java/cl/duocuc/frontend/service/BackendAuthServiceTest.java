package cl.duocuc.frontend.service;

import cl.duocuc.frontend.dto.LoginRequest;
import cl.duocuc.frontend.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackendAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private BackendAuthService service;

    @BeforeEach
    void setUp() {
        service = new BackendAuthService(restTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", "http://backend");
    }

    @Test
    void loginReturnsBackendJwtResponse() {
        LoginResponse response = loginResponse("jwt-token", "admin", "ROLE_ADMIN");
        when(restTemplate.postForEntity(
                eq("http://backend/api/auth/login"),
                any(LoginRequest.class),
                eq(LoginResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        LoginResponse result = service.login("admin", "admin123");

        assertEquals("jwt-token", result.getToken());
        assertEquals("ROLE_ADMIN", result.getRole());

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(restTemplate).postForEntity(eq("http://backend/api/auth/login"), captor.capture(), eq(LoginResponse.class));
        assertEquals("admin", captor.getValue().getUsername());
        assertEquals("admin123", captor.getValue().getPassword());
    }

    @Test
    void loginThrowsWhenBackendReturnsNoBody() {
        when(restTemplate.postForEntity(
                eq("http://backend/api/auth/login"),
                any(LoginRequest.class),
                eq(LoginResponse.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.OK).body(null));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.login("admin", "admin123"));

        assertTrue(exception.getMessage().contains("No se pudo obtener el token JWT"));
    }

    @Test
    void loginPropagatesRestClientErrors() {
        when(restTemplate.postForEntity(
                eq("http://backend/api/auth/login"),
                any(LoginRequest.class),
                eq(LoginResponse.class)
        )).thenThrow(new RestClientException("backend caido"));

        assertThrows(RestClientException.class, () -> service.login("admin", "admin123"));
    }

    private LoginResponse loginResponse(String token, String username, String role) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setRole(role);
        return response;
    }
}
