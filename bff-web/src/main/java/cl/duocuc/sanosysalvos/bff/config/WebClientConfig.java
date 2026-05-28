package cl.duocuc.sanosysalvos.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.auth.url}")
    private String authUrl;

    @Value("${services.mascotas.url}")
    private String mascotasUrl;

    @Value("${services.geo.url}")
    private String geoUrl;

    @Value("${services.matching.url}")
    private String matchingUrl;

    @Bean("authClient")
    public WebClient authClient() {
        return WebClient.builder().baseUrl(authUrl).build();
    }

    @Bean("mascotasClient")
    public WebClient mascotasClient() {
        return WebClient.builder().baseUrl(mascotasUrl).build();
    }

    @Bean("geoClient")
    public WebClient geoClient() {
        return WebClient.builder().baseUrl(geoUrl).build();
    }

    @Bean("matchingClient")
    public WebClient matchingClient() {
        return WebClient.builder().baseUrl(matchingUrl).build();
    }
}
