package com.queryinterface.radio.ytdl.services;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queryinterface.radio.ytdl.kafka.messages.KafkaMessage;

@Service
public class KafkaPublisherService {
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(KafkaPublisherService.class);

    public KafkaPublisherService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishMessage(String topic, KafkaMessage message) {
        try {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<String,String>(topic,
                objectMapper.writeValueAsString(message));
            producerRecord.headers().add("type", message.getType().getBytes(StandardCharsets.UTF_8));
            kafkaTemplate.send(producerRecord);
        } catch (JsonProcessingException e) {
            logger.error("unable to serialize message of type {}", message.getType());
        }
    }
}
