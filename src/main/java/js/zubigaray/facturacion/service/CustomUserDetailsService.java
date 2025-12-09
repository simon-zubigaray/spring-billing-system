package js.zubigaray.facturacion.service;

import js.zubigaray.facturacion.entity.UserEntity;
import js.zubigaray.facturacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga el usuario de la base de datos por su nombre de usuario.
     * Este método es usado por Spring Security durante la autenticación.
     * @param username El nombre de usuario.
     * @return Un objeto UserDetails de Spring Security.
     * @throws UsernameNotFoundException Si el usuario no existe.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el UserEntity en la base de datos
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Convierte los roles de la entidad a GrantedAuthority de Spring Security
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    // AÑADIR MEJORA: Asegura el prefijo 'ROLE_' necesario para Spring Security
                    return new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase());
                })
                .collect(Collectors.toList());

        // Retorna un objeto UserDetails que contiene el username, la contraseña (hash) y los roles/autoridades
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // La contraseña (hash)
                .authorities(authorities)
                .build();
    }
}