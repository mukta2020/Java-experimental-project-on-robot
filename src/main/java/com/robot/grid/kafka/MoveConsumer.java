package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.*;
import com.robot.grid.model.Position;
import com.robot.grid.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

/**
 * Reads from move_instructions, computes the final position, and publishes
 * the result to instruction_results.
 *
 * Errors are logged and the message is skipped — a dead-letter topic would
 * be the next step in a production setup.
 */
@Component
public class MoveConsumer {

    private static final Logger log = LoggerFactory.getLogger(MoveConsumer.class);

    private final RobotService robotService;
    private final ResultProducer resultProducer;

    public MoveConsumer(RobotService robotService, ResultProducer resultProducer) {
        this.robotService  = robotService;
        this.resultProducer = resultProducer;
    }

    @KafkaListener(topics = "${kafka.topics.move-instructions}", containerFactory = "moveListenerContainerFactory")
    public void consume(@Payload MoveMessage message,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received move instruction [{}]", message.instructionNumber());
        try {
            Position start = new Position(message.initialPosition().x(), message.initialPosition().y());
            Position end   = robotService.execute(start, message.moveInstruction());

            resultProducer.send(new ResultMessage(
                message.instructionNumber(),
                message.initialPosition(),
                new PositionDto(end.x(), end.y())
            ));
        } catch (Exception e) {
            log.error("Failed to process instruction [{}]: {}", message.instructionNumber(), e.getMessage());
        }
    }
}
