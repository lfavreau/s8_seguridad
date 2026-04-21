package cl.duocuc.backend.service;

import cl.duocuc.backend.dto.LoginRequest;
import cl.duocuc.backend.dto.LoginResponse;
import cl.duocuc.backend.entity.AppUser;
import cl.duocuc.backend.repository.AppUserRepository;
import cl.duocuc.backend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtService jwtService,
                       AppUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        AppUser user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(userDetails, user.getRole());
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
