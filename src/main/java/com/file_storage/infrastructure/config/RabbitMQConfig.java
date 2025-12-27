package com.file_storage.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILE_EVENTS_EXCHANGE = "file.events";
    public static final String SYNC_EVENTS_EXCHANGE = "sync.events";
    public static final String NOTIFICATION_EXCHANGE = "notification.events";
    public static final String VIRUS_SCAN_EXCHANGE = "virus.scan";

    public static final String FILE_UPLOADED_QUEUE = "file.uploaded.queue";
    public static final String FILE_DELETED_QUEUE = "file.deleted.queue";
    public static final String SYNC_QUEUE = "sync.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String VIRUS_SCAN_QUEUE = "virus.scan.queue";

    @Bean
    public TopicExchange fileEventsExchange() {
        return new TopicExchange(FILE_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange syncEventsExchange() {
        return new TopicExchange(SYNC_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public TopicExchange virusScanExchange() {
        return new TopicExchange(VIRUS_SCAN_EXCHANGE);
    }

    @Bean
    public Queue fileUploadedQueue() {
        return QueueBuilder.durable(FILE_UPLOADED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue fileDeletedQueue() {
        return QueueBuilder.durable(FILE_DELETED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Queue syncQueue() {
        return QueueBuilder.durable(SYNC_QUEUE)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Queue virusScanQueue() {
        return QueueBuilder.durable(VIRUS_SCAN_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Binding fileUploadedBinding() {
        return BindingBuilder.bind(fileUploadedQueue())
                .to(fileEventsExchange())
                .with("file.uploaded");
    }

    @Bean
    public Binding fileDeletedBinding() {
        return BindingBuilder.bind(fileDeletedQueue())
                .to(fileEventsExchange())
                .with("file.deleted");
    }

    @Bean
    public Binding syncBinding() {
        return BindingBuilder.bind(syncQueue())
                .to(syncEventsExchange())
                .with("sync.event");
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.send");
    }

    @Bean
    public Binding virusScanBinding() {
        return BindingBuilder.bind(virusScanQueue())
                .to(virusScanExchange())
                .with("scan.request");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
