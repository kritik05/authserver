package com.authserver.Authserver.service;
import com.authserver.Authserver.model.ScanEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScanEventProducer {

    @Value("${app.kafka.topics.scan}")
    private String scanTopic;

    private final KafkaTemplate<String, ScanEvent> kafkaTemplate;

    public ScanEventProducer(KafkaTemplate<String, ScanEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendScanEvent(ScanEvent event) {
        kafkaTemplate.send(scanTopic, event);
    }
}
