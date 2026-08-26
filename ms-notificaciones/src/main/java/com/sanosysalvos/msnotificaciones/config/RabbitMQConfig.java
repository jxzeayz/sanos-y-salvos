package com.sanosysalvos.msnotificaciones.config;

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
    public Queue notificacionesQueue() {
        return QueueBuilder.durable("q.notificaciones")
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "q.notificaciones.dlq")
                .build();
    }

    @Bean
    public Queue notificacionesDlqQueue() {
        return QueueBuilder.durable("q.notificaciones.dlq").build();
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificacionesQueue).to(exchange).with("coincidencia.*");
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