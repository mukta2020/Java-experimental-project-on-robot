package com.robot.grid.controller;

import com.robot.grid.dto.Dtos.*;
import com.robot.grid.kafka.MoveProducer;
import com.robot.grid.model.Position;
import com.robot.grid.service.RobotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for robot movement.
 *
 * POST /api/v1/robot/move
 *   - Computes the final position synchronously (so the caller gets an immediate response)
 *   - Also publishes the request to Kafka for async downstream processing
 */
@RestController
@RequestMapping("/api/v1/robot")
public class RobotController {

    private final RobotService robotService;
    private final MoveProducer moveProducer;

    public RobotController(RobotService robotService, MoveProducer moveProducer) {
        this.robotService = robotService;
        this.moveProducer = moveProducer;
    }

    @PostMapping("/move")
    public ResponseEntity<MoveResponse> move(@Valid @RequestBody MoveRequest request) {
        String instructionNumber = robotService.nextInstructionNumber();

        Position start = new Position(request.initialPosition().x(), request.initialPosition().y());
        Position end   = robotService.execute(start, request.moveInstruction());

        // Publish to Kafka for async consumers (golden-point checker, etc.)
        moveProducer.send(new MoveMessage(
            request.initialPosition(),
            request.moveInstruction(),
            instructionNumber
        ));

        return ResponseEntity.ok(new MoveResponse(
            new PositionDto(end.x(), end.y()),
            instructionNumber
        ));
    }
}
