package com.sanosysalvos.msauditoria.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("sanos-salvos.events");
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable("q.auditoria")
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "q.auditoria.dlq")
                .build();
    }

    @Bean
    public Queue auditoriaDlqQueue() {
        return QueueBuilder.durable("q.auditoria.dlq").build();
    }

    @Bean
    public Binding binding() {
        // "*.*" solo matchea routing keys de exactamente 2 segmentos (ej. "mascota.perdida",
        // "coincidencia.hallada", "zona.actualizada"). Todos los eventos actuales del sistema
        // cumplen ese formato; si se agrega un evento de 1 o 3+ segmentos, este binding no lo
        // capturará y quedará fuera de la auditoría sin ningún error visible.
        return BindingBuilder.bind(queue()).to(exchange()).with("*.*");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}