package cl.duocuc.backend.service;

import cl.duocuc.backend.entity.Animal;
import cl.duocuc.backend.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository repository;

    @InjectMocks
    private AnimalService service;

    @Test
    void findAllReturnsRepositoryAnimals() {
        List<Animal> animals = List.of(animal(1L, "Luna"), animal(2L, "Sol"));
        when(repository.findAll()).thenReturn(animals);

        List<Animal> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Luna", result.get(0).getNombre());
        verify(repository).findAll();
    }

    @Test
    void findByIdReturnsExistingAnimal() {
        Animal animal = animal(1L, "Luna");
        when(repository.findById(1L)).thenReturn(Optional.of(animal));

        Animal result = service.findById(1L);

        assertSame(animal, result);
    }

    @Test
    void findByIdThrowsNotFoundWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.findById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Animal no encontrado"));
    }

    @Test
    void savePersistsNewAnimal() {
        Animal animal = animal(null, "Luna");
        Animal saved = animal(10L, "Luna");
        when(repository.save(animal)).thenReturn(saved);

        Animal result = service.save(animal);

        assertEquals(10L, result.getId());
        verify(repository).save(animal);
    }

    @Test
    void updateChangesExistingAnimal() {
        Animal current = animal(1L, "Luna");
        Animal updated = new Animal("Rayo", "Perro", "edad 5", "Adoptado", "Adoptado por familia.");
        when(repository.findById(1L)).thenReturn(Optional.of(current));
        when(repository.save(current)).thenReturn(current);

        Animal result = service.update(1L, updated);

        assertEquals("Rayo", result.getNombre());
        assertEquals("Perro", result.getEspecie());
        assertEquals("edad 5", result.getEdad());
        assertEquals("Adoptado", result.getEstado());
        assertEquals("Adoptado por familia.", result.getDescripcion());
        verify(repository).save(current);
    }

    @Test
    void updateThrowsNotFoundWhenAnimalDoesNotExist() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(7L, animal(null, "Rayo"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingAnimal() {
        when(repository.findById(1L)).thenReturn(Optional.of(animal(1L, "Luna")));

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteThrowsNotFoundWhenAnimalDoesNotExist() {
        when(repository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.delete(8L));

        verify(repository, never()).deleteById(anyLong());
    }

    private Animal animal(Long id, String nombre) {
        Animal animal = new Animal(nombre, "Gato", "edad 2", "Disponible", "Rescatado.");
        animal.setId(id);
        return animal;
    }
}
