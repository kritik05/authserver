package com.authserver.Authserver.producer;

import com.authserver.Authserver.events.ScanRequestEvent;
import com.authserver.Authserver.events.TicketCreateRequestEvent;
import com.authserver.Authserver.events.TicketTransitionRequestEvent;
import com.authserver.Authserver.events.UpdateRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Value("${app.kafka.topics.jobunified}")
    private String unifiedTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    public EventProducer(@Qualifier("eventKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper=objectMapper;
    }

    public void sendScanEvent(ScanRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(unifiedTopic, json);
        } catch (Exception e) {
            // Handle serialization exception appropriately
            e.printStackTrace();
        }
    }

    public void sendUpdateEvent(UpdateRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println(json);
            kafkaTemplate.send(unifiedTopic, json);
        } catch (Exception e) {
            // Handle serialization exception appropriately
            e.printStackTrace();
        }
//        kafkaTemplate.send(updatetopic, event);
    }
    public void sendTicketCreateEvent(TicketCreateRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println(json);
            kafkaTemplate.send(unifiedTopic, json);
        } catch (Exception e) {
            // Handle serialization exception appropriately
            e.printStackTrace();
        }
//        kafkaTemplate.send(updatetopic, event);
    }

    public void sendTicketTransitionEvent(TicketTransitionRequestEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            System.out.println(json);
            kafkaTemplate.send(unifiedTopic, json);
        } catch (Exception e) {
            // Handle serialization exception appropriately
            e.printStackTrace();
        }
//        kafkaTemplate.send(updatetopic, event);
    }

}