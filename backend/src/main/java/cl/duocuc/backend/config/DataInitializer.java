package cl.duocuc.backend.config;

import cl.duocuc.backend.entity.Animal;
import cl.duocuc.backend.entity.AppUser;
import cl.duocuc.backend.repository.AnimalRepository;
import cl.duocuc.backend.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(AppUserRepository userRepository,
                               AnimalRepository animalRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN"));
                userRepository.save(new AppUser("gestor", passwordEncoder.encode("gestor123"), "ROLE_GESTOR"));
                userRepository.save(new AppUser("visitante", passwordEncoder.encode("visitante123"), "ROLE_VISITANTE"));
            }

            if (animalRepository.count() == 0) {
                animalRepository.save(new Animal("Cachupin", "Perro", "edad 2", "Disponible", "Perro rescatado."));
                animalRepository.save(new Animal("Misifus", "Gato", "edad 1", "En tratamiento", "Gato en observación."));
                animalRepository.save(new Animal("Canela", "Perro", "edad 4", "Adoptado", "Adoptada por familia."));
            }
        };
    }
}
