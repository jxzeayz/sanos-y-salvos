package cl.duocuc.sanosysalvos.matching.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.matching}")
    private String queueMatching;

    @Bean
    public TopicExchange sanosExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue matchingQueue() {
        return QueueBuilder.durable(queueMatching)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", "q.dlq")
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable("q.dlq").build();
    }

    @Bean
    public Binding matchingBinding(Queue matchingQueue, TopicExchange sanosExchange) {
        return BindingBuilder.bind(matchingQueue).to(sanosExchange).with("mascota.*");
    }

    @Bean
    public Queue coincidenciaQueue() {
        return new Queue("q.coincidencia", true);
    }

    @Bean
    public Binding coincidenciaBinding(Queue coincidenciaQueue, TopicExchange sanosExchange) {
        return BindingBuilder.bind(coincidenciaQueue).to(sanosExchange).with("coincidencia.*");
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
