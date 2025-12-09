package js.zubigaray.facturacion.service;

import js.zubigaray.facturacion.dto.RegisterRequest;
import js.zubigaray.facturacion.dto.UserDTO;
import js.zubigaray.facturacion.entity.RoleEntity;
import js.zubigaray.facturacion.entity.UserEntity;
import js.zubigaray.facturacion.exception.custom_exceptions.RoleNotFoundException;
import js.zubigaray.facturacion.repository.RoleRepository;
import js.zubigaray.facturacion.repository.UserRepository;
import js.zubigaray.facturacion.util.UserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserFactory userFactory;

    /**
     * Crea un nuevo usuario y le asigna roles específicos, lo que requiere permisos de ADMIN.
     * @param request Datos del usuario a crear.
     * @return UserDTO del usuario creado.
     */
    @Transactional
    public UserDTO createUserWithRoles(RegisterRequest request) {

        // 1. Validaciones de unicidad (Práctica recomendada para administración)
        UserEntity user = userFactory.validateAndBuildUser(request);

        // 3. Obtener y validar los roles
        Set<RoleEntity> roles = new HashSet<>();

        // El RegisterRequest.getRoles() devuelve Set<RoleEntity> en tu DTO.
        // Convertimos a una lista de nombres (String) para facilitar la búsqueda.
        List<String> rolesString = request.getRoles()
                .stream()
                .map(RoleEntity::getName)
                .toList();

        for (String roleName : rolesString) {
            // Busca el rol por nombre. Si no existe, lanza la excepción personalizada.
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + roleName));
            roles.add(role);
        }

        // 4. Asignar roles y guardar el usuario
        user.setRoles(roles);
        UserEntity savedUser = userRepository.save(user);

        // 5. Devolver DTO
        return convertToDTO(savedUser);
    }

    /**
     * Convierte la entidad UserEntity a un DTO simple para evitar exponer datos sensibles (ej. password, roles completos).
     */
    private UserDTO convertToDTO(UserEntity user){
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                // Podrías añadir roles aquí si UserDTO lo permite:
                // .roles(user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toList()))
                .build();
    }
}