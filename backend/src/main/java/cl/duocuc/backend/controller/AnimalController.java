package cl.duocuc.backend.controller;

import cl.duocuc.backend.entity.Animal;
import cl.duocuc.backend.service.AnimalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
public class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping("/api/public/animales")
    public ResponseEntity<List<Animal>> getPublicAnimals() {
        return ResponseEntity.ok(animalService.findAll());
    }

    @GetMapping("/api/private/animales")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'VISITANTE')")
    public ResponseEntity<List<Animal>> getPrivateAnimals() {
        return ResponseEntity.ok(animalService.findAll());
    }

    @GetMapping("/api/private/animales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'VISITANTE')")
    public ResponseEntity<Animal> getAnimalById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findById(id));
    }

    @PostMapping("/api/private/animales")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Animal> createAnimal(@Valid @RequestBody Animal animal) {
        return ResponseEntity.ok(animalService.save(animal));
    }

    @PutMapping("/api/private/animales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public ResponseEntity<Animal> updateAnimal(@PathVariable Long id, @Valid @RequestBody Animal animal) {
        return ResponseEntity.ok(animalService.update(id, animal));
    }

    @DeleteMapping("/api/private/animales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
