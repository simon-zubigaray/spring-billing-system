package js.zubigaray.facturacion.service;

import js.zubigaray.facturacion.dto.AuthRequest;
import js.zubigaray.facturacion.dto.AuthResponse;
import js.zubigaray.facturacion.dto.RegisterRequest;
import js.zubigaray.facturacion.entity.RoleEntity;
import js.zubigaray.facturacion.entity.UserEntity;
import js.zubigaray.facturacion.repository.RoleRepository;
import js.zubigaray.facturacion.repository.UserRepository;
import js.zubigaray.facturacion.util.JwtUtil;
import js.zubigaray.facturacion.util.UserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final UserFactory userFactory;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserEntity user = userFactory.validateAndBuildUser(request);

        RoleEntity defaultRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName("USER");
                    return roleRepository.save(newRole);
                });

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);

        return getAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos.");
        }

        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return getAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new BadCredentialsException("Token de refresh inválido o expirado.");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return getAuthResponse(user);
    }

    // --- MÉTODO DE RESPUESTA SIMPLIFICADO ---

    /**
     * Método auxiliar para generar los tokens JWT y construir la respuesta DTO.
     * Se simplifica para usar el generateToken(UserDetails) de JwtUtil.
     */
    private AuthResponse getAuthResponse(UserEntity user) {
        // 1. Cargar UserDetails (necesario para el generador de tokens)
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // 2. Extraer roles para el DTO de respuesta
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        // 3. Generar tokens
        // NOTA: No necesitamos construir 'claims' aquí. JwtUtil lo hace internamente.
        String accessToken = jwtUtil.generateToken(userDetails); // 👈 SOLO pasamos UserDetails
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 4. Construir y devolver la respuesta
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }
}