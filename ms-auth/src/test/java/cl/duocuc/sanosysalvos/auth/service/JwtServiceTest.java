package cl.duocuc.sanosysalvos.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "sanos-y-salvos-super-secret-key-for-testing-must-be-256bits!!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generateToken_retornaJwtBienFormado() {
        String token = jwtService.generateToken("user@test.cl", Map.of("rol", "DUEÑO", "usuarioId", 1L));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractEmail_retornaSubjectCorrecto() {
        String token = jwtService.generateToken("user@test.cl", Map.of());

        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.cl");
    }

    @Test
    void extractAllClaims_contieneClaimsPersonalizados() {
        String token = jwtService.generateToken("user@test.cl", Map.of("rol", "ADMIN"));

        assertThat(jwtService.extractAllClaims(token).get("rol")).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_tokenVigente_retornaTrue() {
        String token = jwtService.generateToken("user@test.cl", Map.of());

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tokenExpirado_retornaFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String token = jwtService.generateToken("user@test.cl", Map.of());

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tokenMalformado_retornaFalse() {
        assertThat(jwtService.isTokenValid("esto.no.es.un.jwt")).isFalse();
    }

    @Test
    void isTokenValid_tokenVacio_retornaFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
