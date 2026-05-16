package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.ResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes computed-result payloads to the instruction_results topic.
 */
@Component
public class ResultProducer {

    private static final Logger log = LoggerFactory.getLogger(ResultProducer.class);

    @Value("${kafka.topics.instruction-results}")
    private String topic;

    private final KafkaTemplate<String, ResultMessage> kafkaTemplate;

    public ResultProducer(KafkaTemplate<String, ResultMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ResultMessage result) {
        kafkaTemplate.send(topic, result.instructionNumber(), result)
            .whenComplete((r, ex) -> {
                if (ex != null) {
                    log.error("Failed to send result [{}]: {}", result.instructionNumber(), ex.getMessage());
                } else {
                    log.debug("Sent result [{}] to {}", result.instructionNumber(), topic);
                }
            });
    }
}
