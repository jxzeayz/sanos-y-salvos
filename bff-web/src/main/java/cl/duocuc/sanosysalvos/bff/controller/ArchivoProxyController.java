package cl.duocuc.sanosysalvos.bff.controller;

import cl.duocuc.sanosysalvos.bff.config.JwtValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff/archivos")
@RequiredArgsConstructor
public class ArchivoProxyController {

    @Qualifier("archivosClient")
    private final WebClient archivosClient;

    @Qualifier("mascotasClient")
    private final WebClient mascotasClient;

    private final JwtValidator jwtValidator;

    @PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map>> subirArchivo(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam Long mascotaId) {

        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        Long usuarioId = getUsuarioId(claims);

        return mascotasClient.get()
                .uri("/api/mascotas/{id}", mascotaId)
                .header("Authorization", authHeader)
                .exchangeToMono(response -> response.bodyToMono(Map.class)
                        .flatMap(mascota -> {
                            Object mascotaOwnerId = mascota.get("usuarioId");
                            Long ownerId = mascotaOwnerId instanceof Number ? ((Number) mascotaOwnerId).longValue() : Long.valueOf(mascotaOwnerId.toString());
                            if (!usuarioId.equals(ownerId)) {
                                return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                       .<Map>body(Map.of("mensaje", "No tienes permiso para subir archivos a esta mascota")));
                            }

                            MultipartBodyBuilder builder = new MultipartBodyBuilder();
                            builder.part("file", archivo.getResource());
                            builder.part("mascotaId", mascotaId);

                            return archivosClient.post()
                                    .uri("/api/archivos")
                                    .header("Authorization", authHeader)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .body(BodyInserters.fromMultipartData(builder.build()))
                                    .exchangeToMono(resp -> resp.bodyToMono(Map.class)
                                            .defaultIfEmpty(Map.of())
                                            .map(body -> ResponseEntity.status(resp.statusCode()).body(body)));
                        })
                );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map>> obtenerInfo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return archivosClient.get()
                .uri("/api/archivos/{id}", id)
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

    @GetMapping("/mascota/{mascotaId}")
    public Mono<ResponseEntity<List>> listarPorMascota(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long mascotaId) {

        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return archivosClient.get()
                .uri("/api/archivos/mascota/{mascotaId}", mascotaId)
                .header("Authorization", authHeader)
                .exchangeToMono(response ->
                        response.bodyToMono(List.class)
                                .defaultIfEmpty(List.of())
                                .map(responseBody ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .body(responseBody)
                                )
                );
    }

    @GetMapping("/{id}/descargar")
    public Mono<ResponseEntity<byte[]>> descargarArchivo(
            @PathVariable Long id) {

        return archivosClient.get()
                .uri("/api/archivos/{id}/descargar", id)
                .exchangeToMono(response ->
                        response.bodyToMono(byte[].class)
                                .defaultIfEmpty(new byte[0])
                                .map(bytes -> {
                                    MediaType contentType = response.headers().contentType().orElse(MediaType.APPLICATION_OCTET_STREAM);
                                    return ResponseEntity.ok()
                                            .contentType(contentType)
                                            .header("Cache-Control", "public, max-age=86400")
                                            .body(bytes);
                                })
                );
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminarArchivo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        Claims claims = validarToken(authHeader);
        if (claims == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        Long usuarioId = getUsuarioId(claims);
        String rol = claims.get("rol", String.class);

        return archivosClient.get()
                .uri("/api/archivos/{id}", id)
                .header("Authorization", authHeader)
                .exchangeToMono(resp -> resp.bodyToMono(Map.class)
                        .flatMap(archivo -> {
                            Object mascotaIdObj = archivo.get("mascotaId");
                            if (mascotaIdObj == null) {
                                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build());
                            }
                            Long mascotaId = mascotaIdObj instanceof Number ? ((Number) mascotaIdObj).longValue() : Long.valueOf(mascotaIdObj.toString());

                            return mascotasClient.get()
                                    .uri("/api/mascotas/{mid}", mascotaId)
                                    .header("Authorization", authHeader)
                                    .exchangeToMono(mascotaResp -> mascotaResp.bodyToMono(Map.class)
                                            .flatMap(mascota -> {
                                                Object mascotaOwnerId = mascota.get("usuarioId");
                                                Long ownerId = mascotaOwnerId instanceof Number ? ((Number) mascotaOwnerId).longValue() : Long.valueOf(mascotaOwnerId.toString());
                                                if (!usuarioId.equals(ownerId) && !"ADMIN".equals(rol)) {
                                                    return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build());
                                                }
                                                return archivosClient.delete()
                                                        .uri("/api/archivos/{id}", id)
                                                        .header("Authorization", authHeader)
                                                        .exchangeToMono(delResp -> Mono.just(ResponseEntity.status(delResp.statusCode()).<Void>build()));
                                            })
                                    );
                        })
                );
    }

    private Long getUsuarioId(Claims claims) {
        Object id = claims.get("usuarioId");
        if (id instanceof Number) return ((Number) id).longValue();
        return id != null ? Long.valueOf(id.toString()) : null;
    }

    private Claims validarToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return jwtValidator.validate(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
