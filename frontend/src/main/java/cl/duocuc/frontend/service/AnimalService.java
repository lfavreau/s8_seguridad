package cl.duocuc.frontend.service;

import cl.duocuc.frontend.model.Animal;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class AnimalService {

    private final RestTemplate restTemplate;

    @Value("${backend.api.base-url}")
    private String baseUrl;

    public AnimalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Animal> getPublicAnimals() {
        ResponseEntity<List<Animal>> response = restTemplate.exchange(
                baseUrl + "/api/public/animales",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Animal>>() {}
        );
        return response.getBody() == null ? Collections.emptyList() : response.getBody();
    }

    public List<Animal> getPrivateAnimals(HttpSession session) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(session));
        ResponseEntity<List<Animal>> response = restTemplate.exchange(
                baseUrl + "/api/private/animales",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Animal>>() {}
        );
        return response.getBody() == null ? Collections.emptyList() : response.getBody();
    }

    public Animal getAnimalById(Long id, HttpSession session) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(session));
        ResponseEntity<Animal> response = restTemplate.exchange(
                baseUrl + "/api/private/animales/" + id,
                HttpMethod.GET,
                entity,
                Animal.class
        );
        return response.getBody();
    }

    public void createAnimal(Animal animal, HttpSession session) {
        HttpEntity<Animal> entity = new HttpEntity<>(animal, authHeaders(session));
        restTemplate.exchange(baseUrl + "/api/private/animales", HttpMethod.POST, entity, Animal.class);
    }

    public void updateAnimal(Long id, Animal animal, HttpSession session) {
        HttpEntity<Animal> entity = new HttpEntity<>(animal, authHeaders(session));
        restTemplate.exchange(baseUrl + "/api/private/animales/" + id, HttpMethod.PUT, entity, Animal.class);
    }

    public void deleteAnimal(Long id, HttpSession session) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(session));
        restTemplate.exchange(baseUrl + "/api/private/animales/" + id, HttpMethod.DELETE, entity, Void.class);
    }

    private HttpHeaders authHeaders(HttpSession session) {
        String token = (String) session.getAttribute("JWT_TOKEN");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
