package js.zubigaray.facturacion.config.security;

import js.zubigaray.facturacion.config.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Define la cadena de filtros de seguridad HTTP para toda la aplicación.
     * @param http Objeto para configurar la seguridad HTTP.
     * @return La cadena de filtros configurada.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita la protección CSRF (necesario para APIs REST sin estado)
                .csrf(AbstractHttpConfigurer::disable)
                // Habilita la configuración CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configura las reglas de autorización para las peticiones HTTP
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos: Auth, Swagger, Health Check
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health"
                        ).permitAll()
                        // Endpoints que requieren roles específicos (la convención `hasRole` espera el rol SIN el prefijo "ROLE_")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // Cualquier otra petición (API de ventas, etc.) requiere autenticación
                        .anyRequest().authenticated()
                )
                // Configuración de manejo de sesiones: STATELESS (sin estado) es fundamental para JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Maneja excepciones de autenticación (ej. token inválido o no proporcionado)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // Establece el proveedor de autenticación personalizado
                .authenticationProvider(authenticationProvider())
                // Añade el filtro JWT personalizado ANTES del filtro de autenticación estándar de Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define el AuthenticationProvider: cómo Spring debe cargar usuarios y verificar contraseñas.
     * @return DaoAuthenticationProvider configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // 1. Crear el DaoAuthenticationProvider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // 2. Llamar al método bean para obtener el PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    /**
     * Expone el AuthenticationManager, que se usa en el AuthService para el login.
     * @param config Configuración de autenticación.
     * @return AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean para el codificador de contraseñas. Es crucial para guardar y verificar hashes de contraseñas.
     * @return BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración detallada de CORS (Cross-Origin Resource Sharing).
     * @return Configuración de la fuente CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (donde está tu frontend)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
        // Permite enviar cookies/credenciales
        configuration.setAllowCredentials(true);
        // Aplica esta configuración a todas las rutas
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
