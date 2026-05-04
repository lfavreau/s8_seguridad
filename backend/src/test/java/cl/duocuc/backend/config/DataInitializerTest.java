package cl.duocuc.backend.config;

import cl.duocuc.backend.entity.Animal;
import cl.duocuc.backend.entity.AppUser;
import cl.duocuc.backend.repository.AnimalRepository;
import cl.duocuc.backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final DataInitializer initializer = new DataInitializer();

    @Test
    void initDataCreatesDefaultUsersAndAnimalsWhenTablesAreEmpty() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(animalRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "hash-" + invocation.getArgument(0));

        CommandLineRunner runner = initializer.initData(userRepository, animalRepository, passwordEncoder);
        runner.run();

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository, times(3)).save(userCaptor.capture());
        List<AppUser> users = userCaptor.getAllValues();
        assertEquals(List.of("admin", "gestor", "visitante"),
                users.stream().map(AppUser::getUsername).toList());
        assertEquals("hash-admin123", users.get(0).getPassword());
        assertEquals("ROLE_ADMIN", users.get(0).getRole());

        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository, times(3)).save(animalCaptor.capture());
        assertEquals("Cachupin", animalCaptor.getAllValues().get(0).getNombre());
    }

    @Test
    void initDataDoesNotDuplicateExistingData() throws Exception {
        when(userRepository.count()).thenReturn(3L);
        when(animalRepository.count()).thenReturn(3L);

        CommandLineRunner runner = initializer.initData(userRepository, animalRepository, passwordEncoder);
        runner.run();

        verify(userRepository, never()).save(any());
        verify(animalRepository, never()).save(any());
    }
}
