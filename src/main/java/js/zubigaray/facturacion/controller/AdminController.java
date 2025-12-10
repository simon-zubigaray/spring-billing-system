package js.zubigaray.facturacion.controller;

import jakarta.validation.Valid;
import js.zubigaray.facturacion.dto.RegisterRequest;
import js.zubigaray.facturacion.dto.UserDTO;
import js.zubigaray.facturacion.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Endpoint para crear usuarios con roles específicos.
     * Requiere el rol ADMIN.
     */
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUserWithRoles(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = adminService.createUserWithRoles(request);
        // 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}