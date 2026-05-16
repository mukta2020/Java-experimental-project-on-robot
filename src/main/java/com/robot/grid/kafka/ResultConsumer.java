package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.ResultMessage;
import com.robot.grid.model.Position;
import com.robot.grid.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reads final positions from instruction_results and checks them against
 * registered golden points.
 */
@Component
public class ResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(ResultConsumer.class);

    private final RobotService robotService;

    public ResultConsumer(RobotService robotService) {
        this.robotService = robotService;
    }

    @KafkaListener(topics = "${kafka.topics.instruction-results}", containerFactory = "resultListenerContainerFactory")
    public void consume(@Payload ResultMessage result) {
        log.info("Received result [{}]: final position ({}, {})",
            result.instructionNumber(), result.finalPosition().x(), result.finalPosition().y());
        try {
            Position finalPos = new Position(result.finalPosition().x(), result.finalPosition().y());
            robotService.checkGoldenPoint(finalPos, result.instructionNumber());
        } catch (Exception e) {
            log.error("Failed to process result [{}]: {}", result.instructionNumber(), e.getMessage());
        }
    }
}
