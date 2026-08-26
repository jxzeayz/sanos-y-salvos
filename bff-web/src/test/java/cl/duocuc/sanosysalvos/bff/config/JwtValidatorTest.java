package cl.duocuc.sanosysalvos.bff.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private static final String SECRET = "clave-de-prueba-de-al-menos-256-bits-para-firmar-hs256";

    private JwtValidator jwtValidator;

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator();
        ReflectionTestUtils.setField(jwtValidator, "secret", SECRET);
    }

    private String generarToken(long usuarioId, long expiraEnMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(Map.of("usuarioId", usuarioId, "rol", "DUEÑO"))
                .subject("usuario@correo.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiraEnMs))
                .signWith(key)
                .compact();
    }

    @Test
    void validarHeader_tokenValido_retornaClaims() {
        String token = generarToken(42L, 60_000);

        Optional<Claims> resultado = jwtValidator.validarHeader("Bearer " + token);

        assertThat(resultado).isPresent();
        assertThat(jwtValidator.getUsuarioId(resultado.get())).isEqualTo(42L);
    }

    @Test
    void validarHeader_sinPrefijoBearer_retornaVacio() {
        String token = generarToken(42L, 60_000);

        Optional<Claims> resultado = jwtValidator.validarHeader(token);

        assertThat(resultado).isEmpty();
    }

    @Test
    void validarHeader_headerNulo_retornaVacio() {
        assertThat(jwtValidator.validarHeader(null)).isEmpty();
    }

    @Test
    void validarHeader_tokenExpirado_retornaVacio() {
        String token = generarToken(42L, -1_000);

        Optional<Claims> resultado = jwtValidator.validarHeader("Bearer " + token);

        assertThat(resultado).isEmpty();
    }

    @Test
    void validarHeader_firmaInvalida_retornaVacio() {
        SecretKey otraClave = Keys.hmacShaKeyFor("otra-clave-completamente-distinta-de-256-bits-hs256".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("usuario@correo.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otraClave)
                .compact();

        assertThat(jwtValidator.validarHeader("Bearer " + token)).isEmpty();
    }
}
