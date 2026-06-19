package cl.duocuc.sanosysalvos.bff.controller;

import cl.duocuc.sanosysalvos.bff.config.JwtValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/bff/auth")
@RequiredArgsConstructor
public class AuthProxyController {

    @Qualifier("authClient")
    private final WebClient authClient;

    private final JwtValidator jwtValidator;

    @PostMapping("/register")
    public Mono<ResponseEntity<Map>> register(@RequestBody Map<String, Object> body) {
        return authClient.post()
                .uri("/api/auth/register")
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map>> login(@RequestBody Map<String, Object> body) {
        return authClient.post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map>> refresh(@RequestBody Map<String, Object> body) {
        return authClient.post()
                .uri("/api/auth/refresh")
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @GetMapping("/perfil")
    public Mono<ResponseEntity<Map>> getPerfil(
            @RequestHeader("Authorization") String authHeader) {
        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return authClient.get()
                .uri("/api/auth/perfil")
                .header("Authorization", authHeader)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @PutMapping("/perfil")
    public Mono<ResponseEntity<Map>> updatePerfil(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return authClient.put()
                .uri("/api/auth/perfil")
                .header("Authorization", authHeader)
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @PutMapping("/password")
    public Mono<ResponseEntity<Map>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return authClient.put()
                .uri("/api/auth/password")
                .header("Authorization", authHeader)
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    private Claims validarToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return jwtValidator.validate(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/recuperar-password")
    public Mono<ResponseEntity<Map>> recuperarPassword(@RequestBody Map<String, String> body) {
        return authClient.post()
                .uri("/api/auth/recuperar-password")
                .bodyValue(body)
                .exchangeToMono(response ->
                        response.bodyToMono(Map.class)
                                .defaultIfEmpty(Map.of())
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }
}