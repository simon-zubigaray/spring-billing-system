package js.zubigaray.facturacion.controller;

import jakarta.validation.Valid;
import js.zubigaray.facturacion.dto.AuthRequest;
import js.zubigaray.facturacion.dto.AuthResponse;
import js.zubigaray.facturacion.dto.RegisterRequest;
import js.zubigaray.facturacion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para registrar un nuevo usuario.
     * Devuelve 201 Created por convención REST.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        // 201 Created
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para iniciar sesión y obtener tokens JWT.
     * Devuelve 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        // 200 OK
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para renovar el token de acceso usando el token de refresh.
     * Devuelve 200 OK.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        // Remueve el prefijo "Bearer "
        String token = refreshToken.substring(7);
        return ResponseEntity.ok(authService.refreshToken(token));
    }
}