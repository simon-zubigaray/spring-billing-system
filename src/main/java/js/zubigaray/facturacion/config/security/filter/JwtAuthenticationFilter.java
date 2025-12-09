package js.zubigaray.facturacion.config.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import js.zubigaray.facturacion.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j // Herramienta de logging para errores
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Se ejecuta en cada petición HTTP para verificar si hay un token JWT.
     * @param request La petición HTTP.
     * @param response La respuesta HTTP.
     * @param filterChain La cadena de filtros.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Verificar el header de autorización
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Si no hay token, continúa la cadena (el EntryPoint manejará el 401 si la ruta es protegida)
        }

        try {
            // 2. Extraer el token
            jwt = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);

            // 3. Validar el token y establecer la autenticación
            // Solo procede si el username no es nulo y si el usuario NO está autenticado en el contexto actual
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Carga los detalles del usuario desde la DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Valida que el token es legítimo y no ha expirado
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Crea un objeto de autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credenciales (contraseña) se establece como null ya que el token ya verificó la identidad
                            userDetails.getAuthorities() // Roles/Autoridades obtenidas del UserDetails
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establece el objeto de autenticación en el contexto de seguridad.
                    // Esto es lo que "autentica" al usuario para la duración de la petición.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Loguea cualquier error en la validación o procesamiento del token
            log.warn("Cannot set user authentication: {}", e.getMessage());
        }

        // Continúa la cadena de filtros
        filterChain.doFilter(request, response);
    }
}