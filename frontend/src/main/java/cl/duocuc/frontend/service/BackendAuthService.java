package cl.duocuc.frontend.service;

import cl.duocuc.frontend.dto.LoginRequest;
import cl.duocuc.frontend.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BackendAuthService {

    private final RestTemplate restTemplate;

    @Value("${backend.api.base-url}")
    private String baseUrl;

    public BackendAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LoginResponse login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                request,
                LoginResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("No se pudo obtener el token JWT desde el backend");
        }
        return response.getBody();
    }
}
