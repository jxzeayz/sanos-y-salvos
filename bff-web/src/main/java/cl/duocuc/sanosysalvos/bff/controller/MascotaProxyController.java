package cl.duocuc.sanosysalvos.bff.controller;

import cl.duocuc.sanosysalvos.bff.config.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/mascotas")
@RequiredArgsConstructor
public class MascotaProxyController {

    @Qualifier("mascotasClient")
    private final WebClient mascotasClient;

    @Qualifier("geoClient")
    private final WebClient geoClient;

    @Qualifier("matchingClient")
    private final WebClient matchingClient;

    private final JwtValidator jwtValidator;

    @PostMapping
    public Mono<ResponseEntity<Map>> registrar(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        if (!tokenValido(authHeader)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return mascotasClient.post()
                .uri("/api/mascotas")
                .header("Authorization", authHeader)
                .bodyValue(body)
                .retrieve()
                .toEntity(Map.class);
    }

    @GetMapping
    public Mono<ResponseEntity<List>> listar(
            @RequestParam(required = false) String estado) {
        String uri = estado != null ? "/api/mascotas?estado=" + estado : "/api/mascotas";
        return mascotasClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(List.class);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map>> obtener(@PathVariable Long id) {
        return mascotasClient.get()
                .uri("/api/mascotas/{id}", id)
                .retrieve()
                .toEntity(Map.class);
    }

    @GetMapping("/{id}/coincidencias")
    public Mono<ResponseEntity<List>> coincidencias(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        if (!tokenValido(authHeader)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return matchingClient.get()
                .uri("/api/matching/coincidencias/mascota/{id}", id)
                .retrieve()
                .toEntity(List.class);
    }

    @GetMapping("/mapa")
    public Mono<ResponseEntity<List>> mapa() {
        return geoClient.get()
                .uri("/api/geo/reportes")
                .retrieve()
                .toEntity(List.class);
    }

    private boolean tokenValido(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        return jwtValidator.isValid(authHeader.substring(7));
    }
}
