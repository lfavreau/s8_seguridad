package cl.duocuc.backend.security;

import cl.duocuc.backend.entity.AppUser;
import cl.duocuc.backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private AppUserRepository repository;

    @InjectMocks
    private AppUserDetailsService service;

    @Test
    void loadUserByUsernameReturnsSpringUser() {
        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(new AppUser("admin", "hash", "ROLE_ADMIN")));

        UserDetails userDetails = service.loadUserByUsername("admin");

        assertEquals("admin", userDetails.getUsername());
        assertEquals("hash", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsernameThrowsWhenMissing() {
        when(repository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie"));
    }
}
