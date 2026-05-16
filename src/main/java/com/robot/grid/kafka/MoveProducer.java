package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.MoveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes move-instruction payloads to the move_instructions topic.
 * The instruction number is used as the message key to preserve ordering per instruction.
 */
@Component
public class MoveProducer {

    private static final Logger log = LoggerFactory.getLogger(MoveProducer.class);

    @Value("${kafka.topics.move-instructions}")
    private String topic;

    private final KafkaTemplate<String, MoveMessage> kafkaTemplate;

    public MoveProducer(KafkaTemplate<String, MoveMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(MoveMessage message) {
        kafkaTemplate.send(topic, message.instructionNumber(), message)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send [{}]: {}", message.instructionNumber(), ex.getMessage());
                } else {
                    log.debug("Sent [{}] to {} partition {}",
                        message.instructionNumber(), topic,
                        result.getRecordMetadata().partition());
                }
            });
    }
}
