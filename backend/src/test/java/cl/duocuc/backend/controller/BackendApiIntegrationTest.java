package cl.duocuc.backend.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicAnimalsRouteIsAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/public/animales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].nombre", not(blankString())));
    }

    @Test
    void privateAnimalsRouteRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/private/animales"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void validLoginReturnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankString())))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void invalidLoginReturnsUnauthorizedError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"mala"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"));
    }

    @Test
    void adminTokenCanCreateReadUpdateAndDeleteAnimal() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        MvcResult createResult = mockMvc.perform(post("/api/private/animales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"Copito",
                                  "especie":"Conejo",
                                  "edad":"edad 1",
                                  "estado":"Disponible",
                                  "descripcion":"Rescatado y sano."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nombre").value("Copito"))
                .andReturn();

        Integer id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/private/animales/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Copito"));

        mockMvc.perform(put("/api/private/animales/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"Copito",
                                  "especie":"Conejo",
                                  "edad":"edad 2",
                                  "estado":"Adoptado",
                                  "descripcion":"Adoptado por familia."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Adoptado"))
                .andExpect(jsonPath("$.descripcion").value("Adoptado por familia."));

        mockMvc.perform(delete("/api/private/animales/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/private/animales/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidAnimalPayloadReturnsBadRequest() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(post("/api/private/animales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"",
                                  "especie":"Gato",
                                  "edad":"edad 1",
                                  "estado":"Disponible",
                                  "descripcion":"Sin nombre."
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gestorTokenCannotDeleteAnimals() throws Exception {
        String token = loginAndGetToken("gestor", "gestor123");

        mockMvc.perform(delete("/api/private/animales/{id}", 1)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
