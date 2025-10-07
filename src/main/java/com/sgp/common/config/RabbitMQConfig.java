package com.sgp.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Nombres de las colas y exchanges (basados en tu configuración)
    public static final String MAIL_QUEUE = "mailQueue";
    public static final String MAIL_EXCHANGE = "mailExchange";

    // Nombres para la Dead Letter Queue
    public static final String DLQ_QUEUE = MAIL_QUEUE + ".dlq";
    public static final String DLQ_EXCHANGE = MAIL_EXCHANGE + ".dlx";

    /**
     * 1. Definición de la Dead Letter Exchange (DLX)
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    /**
     * 2. Definición de la Dead Letter Queue (DLQ)
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE);
    }

    /**
     * 3. Binding: Conecta la DLQ al DLX
     * La DLQ escucha al DLX con la clave de ruteo de la cola original.
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(MAIL_QUEUE); // La clave de ruteo es el nombre de la cola principal
    }

    /**
     * 4. Definición de la Cola Principal (mailQueue)
     * - Configura los argumentos para que, al fallar, los mensajes vayan al DLX.
     */
    @Bean
    public Queue mailQueue() {
        Map<String, Object> args = new HashMap<>();
        // 4a. Configura el Exchange al que los mensajes fallidos deben ser enviados
        args.put("x-dead-letter-exchange", DLQ_EXCHANGE);
        // 4b. Configura la clave de ruteo que deben usar los mensajes fallidos
        args.put("x-dead-letter-routing-key", MAIL_QUEUE);
        // Opcional: Número máximo de reintentos antes de ir a DLQ (ej. 2 reintentos)
        args.put("x-max-attempts", 2);

        return new Queue(MAIL_QUEUE, true, false, false, args);
    }

    /**
     * 5. Definición del Exchange Principal (Direct)
     */
    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange(MAIL_EXCHANGE);
    }

    /**
     * 6. Binding: Conecta la Cola Principal al Exchange Principal
     */
    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue())
                .to(mailExchange())
                .with(MAIL_QUEUE);
    }

    /**
     * Define el MessageConverter para serializar/deserializar los objetos MailMessage.
     * Usar Jackson2JsonMessageConverter es más moderno, seguro y flexible que el Serializador Java por defecto.
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}