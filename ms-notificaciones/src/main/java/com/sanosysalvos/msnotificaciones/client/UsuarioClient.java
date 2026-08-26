package com.sanosysalvos.msnotificaciones.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class UsuarioClient {

    private final RestTemplate restTemplate;

    @Value("${services.auth.url}")
    private String authUrl;

    public UsuarioClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public String obtenerEmail(Long usuarioId) {
        try {
            Map<String, Object> usuario = restTemplate.getForObject(
                    authUrl + "/api/auth/internal/usuarios/{id}", Map.class, usuarioId);
            return usuario != null ? (String) usuario.get("email") : null;
        } catch (Exception e) {
            log.warn("No se pudo resolver el email del usuario {}: {}", usuarioId, e.getMessage());
            return null;
        }
    }
}
