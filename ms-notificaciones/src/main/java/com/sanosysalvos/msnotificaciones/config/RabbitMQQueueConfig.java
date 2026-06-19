package com.sanosysalvos.msnotificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.queue")
public class RabbitMQQueueConfig {
    private String notificaciones = "q.notificaciones";
    public String getNotificaciones() { return notificaciones; }
    public void setNotificaciones(String notificaciones) { this.notificaciones = notificaciones; }
}