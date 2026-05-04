package cl.duocuc.backend.service;

import cl.duocuc.backend.entity.Animal;
import cl.duocuc.backend.repository.AnimalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AnimalService {

    private final AnimalRepository repository;

    public AnimalService(AnimalRepository repository) {
        this.repository = repository;
    }

    public List<Animal> findAll() {
        return repository.findAll();
    }

    public Animal findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal no encontrado"));
    }

    public Animal save(Animal animal) {
        return repository.save(animal);
    }

    public Animal update(Long id, Animal updated) {
        Animal current = findById(id);
        current.setNombre(updated.getNombre());
        current.setEspecie(updated.getEspecie());
        current.setEdad(updated.getEdad());
        current.setEstado(updated.getEstado());
        current.setDescripcion(updated.getDescripcion());
        return repository.save(current);
    }

    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }
}
