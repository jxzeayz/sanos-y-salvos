package cl.duocuc.sanosysalvos.bff.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bff-web corre como aplicación servlet (ver GlobalCorsConfig), por lo que el
 * rate limiting se implementa como un jakarta.servlet.Filter estándar y no
 * como un WebFilter reactivo (que aquí nunca se ejecutaría).
 */
@Component
public class RateLimitConfig extends OncePerRequestFilter {

    @Value("${bff.rate-limit.max-requests-per-minute:100}")
    private int maxRequestsPerMinute;

    @Value("${bff.rate-limit.window-ms:60000}")
    private long windowMs;

    private final Map<String, RequestWindow> requestMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);

        RequestWindow window = requestMap.computeIfAbsent(clientIp, k -> new RequestWindow());
        synchronized (window) {
            long now = Instant.now().toEpochMilli();
            if (now - window.windowStart > windowMs) {
                window.count = 0;
                window.windowStart = now;
            }
            if (window.count >= maxRequestsPerMinute) {
                writeRateLimitResponse(response);
                return;
            }
            window.count++;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Se usa la IP real de la conexión TCP, nunca X-Forwarded-For: ese header lo
     * controla el cliente y es trivialmente falsificable si no hay un proxy
     * confiable en frente reescribiéndolo.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    @Scheduled(fixedRateString = "${bff.rate-limit.window-ms:60000}")
    void limpiarEntradasExpiradas() {
        long now = Instant.now().toEpochMilli();
        requestMap.entrySet().removeIf(entry -> now - entry.getValue().windowStart > windowMs);
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");

        String body = "{\"mensaje\":\"Límite de solicitudes excedido. Máximo " + maxRequestsPerMinute
                + " solicitudes por minuto.\",\"segundosParaReintentar\":60}";
        response.getWriter().write(body);
    }

    private static class RequestWindow {
        long windowStart = Instant.now().toEpochMilli();
        int count = 0;
    }
}
