package cl.duocuc.frontend.config;

import cl.duocuc.frontend.dto.LoginResponse;
import cl.duocuc.frontend.service.BackendAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FrontendSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackendAuthService backendAuthService;

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void privatePageRedirectsToLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/animales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void validFrontendLoginStoresBackendJwtAndRedirects() throws Exception {
        when(backendAuthService.login("admin", "admin123"))
                .thenReturn(loginResponse("jwt-token", "admin", "ROLE_ADMIN"));

        mockMvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animales"))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(request().sessionAttribute("JWT_TOKEN", "jwt-token"))
                .andExpect(request().sessionAttribute("BACKEND_ROLE", "ROLE_ADMIN"))
                .andExpect(request().sessionAttribute("BACKEND_USERNAME", "admin"));
    }

    @Test
    void invalidFrontendLoginRedirectsToLoginError() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("mala"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void backendLoginErrorRedirectsToBackendError() throws Exception {
        when(backendAuthService.login("admin", "admin123"))
                .thenThrow(new RestClientException("backend caido"));

        mockMvc.perform(formLogin().user("admin").password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?backendError"));
    }

    private LoginResponse loginResponse(String token, String username, String role) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setRole(role);
        return response;
    }
}
