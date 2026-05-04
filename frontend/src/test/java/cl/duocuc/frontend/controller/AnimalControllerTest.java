package cl.duocuc.frontend.controller;

import cl.duocuc.frontend.model.Animal;
import cl.duocuc.frontend.service.AnimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnimalControllerTest {

    @Mock
    private AnimalService animalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AnimalController(animalService))
                .setViewResolvers(stubViewResolver())
                .build();
    }

    @Test
    void listarRedirectsWhenSessionHasNoJwt() throws Exception {
        mockMvc.perform(get("/animales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired"));
    }

    @Test
    void listarShowsAnimalsWhenSessionHasJwt() throws Exception {
        MockHttpSession session = sessionWithToken();
        when(animalService.getPrivateAnimals(session)).thenReturn(List.of(animal(1L, "Luna")));

        mockMvc.perform(get("/animales").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("animales"))
                .andExpect(model().attribute("animales", hasSize(1)));
    }

    @Test
    void listarShowsBackendErrorWhenServiceFails() throws Exception {
        MockHttpSession session = sessionWithToken();
        when(animalService.getPrivateAnimals(session)).thenThrow(new RestClientException("backend caido"));

        mockMvc.perform(get("/animales").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("animales"))
                .andExpect(model().attribute("backendError", true))
                .andExpect(model().attribute("animales", hasSize(0)));
    }

    @Test
    void nuevoShowsCreateForm() throws Exception {
        mockMvc.perform(get("/animales/nuevo").session(sessionWithToken()))
                .andExpect(status().isOk())
                .andExpect(view().name("animal-form"))
                .andExpect(model().attributeExists("animal"))
                .andExpect(model().attribute("accion", "Crear"));
    }

    @Test
    void guardarCreatesAnimalAndRedirects() throws Exception {
        MockHttpSession session = sessionWithToken();

        mockMvc.perform(post("/animales/guardar")
                        .session(session)
                        .param("nombre", "Luna")
                        .param("especie", "Gato")
                        .param("edad", "edad 2")
                        .param("estado", "Disponible")
                        .param("descripcion", "Rescatada."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animales"));

        ArgumentCaptor<Animal> captor = ArgumentCaptor.forClass(Animal.class);
        verify(animalService).createAnimal(captor.capture(), eq(session));
        assertEquals("Luna", captor.getValue().getNombre());
    }

    @Test
    void guardarAddsFlashErrorWhenBackendFails() throws Exception {
        MockHttpSession session = sessionWithToken();
        doThrow(new RestClientException("backend caido")).when(animalService).createAnimal(any(), eq(session));

        mockMvc.perform(post("/animales/guardar")
                        .session(session)
                        .param("nombre", "Luna"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animales"))
                .andExpect(flash().attribute("backendError", true));
    }

    @Test
    void editarLoadsAnimalAndShowsEditForm() throws Exception {
        MockHttpSession session = sessionWithToken();
        when(animalService.getAnimalById(1L, session)).thenReturn(animal(1L, "Luna"));

        mockMvc.perform(get("/animales/editar/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("animal-edit"))
                .andExpect(model().attribute("accion", "Actualizar"))
                .andExpect(model().attributeExists("animal"));
    }

    @Test
    void actualizarUpdatesAnimalAndRedirects() throws Exception {
        MockHttpSession session = sessionWithToken();

        mockMvc.perform(post("/animales/actualizar/1")
                        .session(session)
                        .param("nombre", "Luna actualizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animales"));

        verify(animalService).updateAnimal(eq(1L), any(Animal.class), eq(session));
    }

    @Test
    void eliminarDeletesAnimalAndRedirects() throws Exception {
        MockHttpSession session = sessionWithToken();

        mockMvc.perform(post("/animales/eliminar/1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/animales"));

        verify(animalService).deleteAnimal(1L, session);
    }

    @Test
    void eliminarRedirectsWhenSessionHasNoJwt() throws Exception {
        mockMvc.perform(post("/animales/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?expired"));
    }

    private MockHttpSession sessionWithToken() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "token");
        return session;
    }

    private Animal animal(Long id, String nombre) {
        Animal animal = new Animal();
        animal.setId(id);
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
