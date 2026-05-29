package cl.duocuc.sanosysalvos.bff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
}