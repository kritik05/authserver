package com.authserver.Authserver.producer;

import com.authserver.Authserver.events.UpdateRequestEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class UpdateEventProducer
{
    @Value("${app.kafka.topics.update}")
    private String updatetopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UpdateEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUpdateEvent(UpdateRequestEvent event) {
        kafkaTemplate.send(updatetopic, event);
    }
}
