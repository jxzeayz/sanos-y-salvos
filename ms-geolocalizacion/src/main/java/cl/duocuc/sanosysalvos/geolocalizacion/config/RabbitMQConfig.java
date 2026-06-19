package cl.duocuc.sanosysalvos.geolocalizacion.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String QUEUE_GEO = "q.geolocalizacion";

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Bean
    public TopicExchange sanosSalvosExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue geoQueue() {
        return QueueBuilder.durable(QUEUE_GEO).build();
    }

    @Bean
    public Binding geoPerdidaBinding(Queue geoQueue, TopicExchange sanosSalvosExchange) {
        return BindingBuilder
                .bind(geoQueue)
                .to(sanosSalvosExchange)
                .with("mascota.perdida");
    }

    @Bean
    public Binding geoEncontradaBinding(Queue geoQueue, TopicExchange sanosSalvosExchange) {
        return BindingBuilder
                .bind(geoQueue)
                .to(sanosSalvosExchange)
                .with("mascota.encontrada");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}