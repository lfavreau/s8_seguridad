package cl.duocuc.frontend.security;

import cl.duocuc.frontend.dto.LoginResponse;
import cl.duocuc.frontend.service.BackendAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackendLoginSuccessHandlerTest {

    private final BackendAuthService backendAuthService = mock(BackendAuthService.class);
    private final BackendLoginSuccessHandler handler = new BackendLoginSuccessHandler(backendAuthService);

    @Test
    void successfulBackendLoginStoresJwtInSessionAndRedirects() throws Exception {
        LoginResponse response = loginResponse("jwt-token", "admin", "ROLE_ADMIN");
        when(backendAuthService.login("admin", "admin123")).thenReturn(response);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("password", "admin123");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                request,
                servletResponse,
                new TestingAuthenticationToken("admin", "admin123")
        );

        assertEquals("jwt-token", request.getSession().getAttribute("JWT_TOKEN"));
        assertEquals("ROLE_ADMIN", request.getSession().getAttribute("BACKEND_ROLE"));
        assertEquals("admin", request.getSession().getAttribute("BACKEND_USERNAME"));
        assertEquals("/animales", servletResponse.getRedirectedUrl());
    }

    @Test
    void backendLoginErrorInvalidatesSessionAndRedirectsToLogin() throws Exception {
        when(backendAuthService.login("admin", "admin123")).thenThrow(new RestClientException("backend caido"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("password", "admin123");
        MockHttpSession session = (MockHttpSession) request.getSession();
        session.setAttribute("JWT_TOKEN", "old-token");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(
                request,
                servletResponse,
                new TestingAuthenticationToken("admin", "admin123")
        );

        assertEquals("/login?backendError", servletResponse.getRedirectedUrl());
        assertTrue(session.isInvalid());
    }

    private LoginResponse loginResponse(String token, String username, String role) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setRole(role);
        return response;
    }
}
