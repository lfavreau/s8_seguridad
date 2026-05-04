package cl.duocuc.frontend.service;

import cl.duocuc.frontend.model.Animal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private AnimalService service;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        service = new AnimalService(restTemplate);
        ReflectionTestUtils.setField(service, "baseUrl", "http://backend");
        session = new MockHttpSession();
        session.setAttribute("JWT_TOKEN", "token-123");
    }

    @Test
    void getPublicAnimalsReturnsBackendList() {
        List<Animal> animals = List.of(animal(1L, "Luna"));
        when(restTemplate.exchange(
                eq("http://backend/api/public/animales"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Animal>>>any()
        )).thenReturn(ResponseEntity.ok(animals));

        List<Animal> result = service.getPublicAnimals();

        assertEquals(1, result.size());
        assertEquals("Luna", result.get(0).getNombre());
    }

    @Test
    void getPublicAnimalsReturnsEmptyListWhenBackendBodyIsNull() {
        when(restTemplate.exchange(
                eq("http://backend/api/public/animales"),
                eq(HttpMethod.GET),
                isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Animal>>>any()
        )).thenReturn(ResponseEntity.ok(null));

        assertEquals(Collections.emptyList(), service.getPublicAnimals());
    }

    @Test
    void getPrivateAnimalsSendsBearerToken() {
        List<Animal> animals = List.of(animal(2L, "Sol"));
        when(restTemplate.exchange(
                eq("http://backend/api/private/animales"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<Animal>>>any()
        )).thenReturn(ResponseEntity.ok(animals));

        List<Animal> result = service.getPrivateAnimals(session);

        assertEquals("Sol", result.get(0).getNombre());

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("http://backend/api/private/animales"),
                eq(HttpMethod.GET),
                captor.capture(),
                ArgumentMatchers.<ParameterizedTypeReference<List<Animal>>>any()
        );
        assertEquals("Bearer token-123", captor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals(MediaType.APPLICATION_JSON, captor.getValue().getHeaders().getContentType());
    }

    @Test
    void getAnimalByIdReturnsBackendAnimal() {
        Animal animal = animal(3L, "Nube");
        when(restTemplate.exchange(
                eq("http://backend/api/private/animales/3"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Animal.class)
        )).thenReturn(ResponseEntity.ok(animal));

        Animal result = service.getAnimalById(3L, session);

        assertEquals("Nube", result.getNombre());
    }

    @Test
    void createAnimalPostsToPrivateEndpoint() {
        Animal animal = animal(null, "Rayo");
        when(restTemplate.exchange(
                eq("http://backend/api/private/animales"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Animal.class)
        )).thenReturn(ResponseEntity.ok(animal));

        service.createAnimal(animal, session);

        verify(restTemplate).exchange(
                eq("http://backend/api/private/animales"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Animal.class)
        );
    }

    @Test
    void updateAnimalPutsToPrivateEndpoint() {
        Animal animal = animal(4L, "Rayo");
        when(restTemplate.exchange(
                eq("http://backend/api/private/animales/4"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Animal.class)
        )).thenReturn(ResponseEntity.ok(animal));

        service.updateAnimal(4L, animal, session);

        verify(restTemplate).exchange(
                eq("http://backend/api/private/animales/4"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Animal.class)
        );
    }

    @Test
    void deleteAnimalCallsPrivateEndpoint() {
        when(restTemplate.exchange(
                eq("http://backend/api/private/animales/5"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(ResponseEntity.noContent().build());

        service.deleteAnimal(5L, session);

        verify(restTemplate).exchange(
                eq("http://backend/api/private/animales/5"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
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
}
