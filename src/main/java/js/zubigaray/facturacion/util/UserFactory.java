package js.zubigaray.facturacion.util;

import js.zubigaray.facturacion.dto.RegisterRequest;
import js.zubigaray.facturacion.entity.UserEntity;
import js.zubigaray.facturacion.exception.custom_exceptions.UserAlreadyExistsException;
import js.zubigaray.facturacion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component // O @Service, ambos funcionan
@RequiredArgsConstructor
public class UserFactory {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Valida que no exista duplicidad y construye la entidad UserEntity inicial
     * con la contraseña codificada.
     * * @param request DTO de registro.
     * @return UserEntity completamente construido.
     */
    public UserEntity validateAndBuildUser(RegisterRequest request) {

        // 1. Validación de unicidad
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }

        // 2. Construcción de la entidad
        UserEntity user = new UserEntity();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // 3. Codificación de contraseña
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return user;
    }
}