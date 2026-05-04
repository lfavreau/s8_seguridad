package cl.duocuc.frontend.controller;

import cl.duocuc.frontend.model.Animal;
import cl.duocuc.frontend.service.AnimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private AnimalService animalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HomeController(animalService))
                .setViewResolvers(stubViewResolver())
                .build();
    }

    @Test
    void homeRedirectsToLoginWhenSessionHasNoJwt() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired"));
    }

    @Test
    void homeShowsAnimalsWhenSessionHasJwt() throws Exception {
        MockHttpSession session = sessionWithToken();
        when(animalService.getPrivateAnimals(session)).thenReturn(List.of(animal("Luna"), animal("Sol")));

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("animales", hasSize(2)));
    }

    @Test
    void homeShowsBackendErrorWhenServiceFails() throws Exception {
        MockHttpSession session = sessionWithToken();
        when(animalService.getPrivateAnimals(session)).thenThrow(new RestClientException("backend caido"));

        mockMvc.perform(get("/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("backendError", true))
                .andExpect(model().attribute("animales", hasSize(0)));
    }

    private MockHttpSession sessionWithToken() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "token");
        return session;
    }

    private Animal animal(String nombre) {
        Animal animal = new Animal();
        animal.setNombre(nombre);
        animal.setEspecie("Gato");
        animal.setEdad("edad 2");
        animal.setEstado("Disponible");
        animal.setDescripcion("Rescatado.");
        return animal;
    }

    private ViewResolver stubViewResolver() {
        return (viewName, locale) -> {
            if (viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring("redirect:".length()));
            }
            return (model, request, response) -> response.setStatus(200);
        };
    }
}
