package js.zubigaray.facturacion.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    // Se obtienen las propiedades de configuración (application.properties o .yml)
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration; // Tiempo de vida del token de acceso (ej. 15 minutos)
    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration; // Tiempo de vida del refresh token (ej. 7 días)

    /** Extrae el nombre de usuario (subject) del token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extrae la fecha de expiración del token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Resuelve una claim específica del token. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Parsea y obtiene todas las claims del token. */
    private Claims extractAllClaims(String token) {
        // Parsea y verifica el token usando la clave secreta
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Verifica si el token ha expirado. */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Valida que el token no esté expirado y pertenezca al usuario proporcionado. */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /** Genera un token de acceso incluyendo los roles del usuario. */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // MEJORA APLICADA: Incluir los roles del usuario en el token JWT
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        claims.put("roles", roles);

        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /** Genera un refresh token (mayor expiración) */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /** Método privado para construir el JWT. */
    private String createToken(Map<String, Object> claims, String subject, Long validity) {
        return Jwts.builder()
                .claims(claims) // Carga las claims (incluyendo roles)
                .subject(subject) // Establece el username como sujeto
                .issuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
                .expiration(new Date(System.currentTimeMillis() + validity)) // Fecha de expiración
                .signWith(getSignKey()) // Firma el token con la clave secreta
                .compact();
    }

    /** Obtiene la clave secreta HmacSha a partir de la clave Base64 codificada. */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Extrae los roles del token (útil si se necesita re-autenticar o verificar roles sin ir a la DB). */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }
}