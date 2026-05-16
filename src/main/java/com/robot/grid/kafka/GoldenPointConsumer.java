package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.PositionDto;
import com.robot.grid.model.Position;
import com.robot.grid.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reads golden-point coordinates from the golden_points topic and registers
 * them in RobotService. Any subsequent move landing on one of these points
 * triggers the "I have reached a golden point!" message.
 */
@Component
public class GoldenPointConsumer {

    private static final Logger log = LoggerFactory.getLogger(GoldenPointConsumer.class);

    private final RobotService robotService;

    public GoldenPointConsumer(RobotService robotService) {
        this.robotService = robotService;
    }

    @KafkaListener(topics = "${kafka.topics.golden-points}", containerFactory = "goldenPointListenerContainerFactory")
    public void consume(@Payload PositionDto dto) {
        log.info("Received golden point: ({}, {})", dto.x(), dto.y());
        try {
            robotService.addGoldenPoint(new Position(dto.x(), dto.y()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid golden point ({}, {}): {}", dto.x(), dto.y(), e.getMessage());
        }
    }
}
