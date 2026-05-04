package cl.duocuc.backend.repository;

import cl.duocuc.backend.entity.Animal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository repository;

    @Test
    void savesFindsListsAndDeletesAnimal() {
        Animal animal = new Animal("Luna", "Gato", "edad 2", "Disponible", "Rescatada.");

        Animal saved = repository.saveAndFlush(animal);

        assertNotNull(saved.getId());
        assertTrue(repository.findById(saved.getId()).isPresent());

        List<Animal> all = repository.findAll();
        assertEquals(1, all.size());
        assertEquals("Luna", all.get(0).getNombre());

        repository.deleteById(saved.getId());
        repository.flush();

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
