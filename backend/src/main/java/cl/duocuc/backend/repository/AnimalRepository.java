package cl.duocuc.backend.repository;

import cl.duocuc.backend.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
}
