package cl.duocuc.frontend.security;

import cl.duocuc.frontend.dto.LoginResponse;
import cl.duocuc.frontend.service.BackendAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@Component
public class BackendLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final BackendAuthService backendAuthService;

    public BackendLoginSuccessHandler(BackendAuthService backendAuthService) {
        this.backendAuthService = backendAuthService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        String password = request.getParameter("password");
        try {
            LoginResponse loginResponse = backendAuthService.login(username, password);
            HttpSession session = request.getSession();
            session.setAttribute("JWT_TOKEN", loginResponse.getToken());
            session.setAttribute("BACKEND_ROLE", loginResponse.getRole());
            session.setAttribute("BACKEND_USERNAME", loginResponse.getUsername());
            response.sendRedirect("/animales");
        } catch (RestClientException ex) {
            request.getSession().invalidate();
            response.sendRedirect("/login?backendError");
        }
    }
}
