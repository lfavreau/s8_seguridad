package cl.duocuc.backend.repository;

import cl.duocuc.backend.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository repository;

    @Test
    void savesFindsByIdAndUsernameAndDeletesUser() {
        AppUser user = new AppUser("admin", "hash", "ROLE_ADMIN");

        AppUser saved = repository.saveAndFlush(user);

        assertNotNull(saved.getId());
        assertTrue(repository.findById(saved.getId()).isPresent());
        assertTrue(repository.findByUsername("admin").isPresent());
        assertTrue(repository.findByUsername("desconocido").isEmpty());

        repository.deleteById(saved.getId());
        repository.flush();

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
