package cl.duocuc.sanosysalvos.bff.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class JwtValidator {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Valida el header "Authorization: Bearer &lt;token&gt;" de una request de un
     * *ProxyController. Devuelve Optional.empty() si el header falta, no trae
     * "Bearer " o el token es inválido/expirado, en vez de lanzar.
     */
    public Optional<Claims> validarHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        try {
            return Optional.of(validate(authHeader.substring(7)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Long getUsuarioId(Claims claims) {
        Object id = claims.get("usuarioId");
        if (id instanceof Number) return ((Number) id).longValue();
        return id != null ? Long.valueOf(id.toString()) : null;
    }
}
