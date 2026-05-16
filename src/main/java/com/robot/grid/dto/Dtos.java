package com.robot.grid.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * All DTOs used by the REST API and Kafka messages, kept in one place for easy reference.
 */
public class Dtos {

    /** A grid coordinate pair. Used in requests, responses, and Kafka payloads. */
    public record PositionDto(
        @Min(0) int x,
        @Min(0) int y
    ) {}

    /** REST request body: POST /api/v1/robot/move */
    public record MoveRequest(
        @JsonProperty("initial_position") @NotNull @Valid PositionDto initialPosition,
        @JsonProperty("move_instruction") @NotBlank String moveInstruction
    ) {}

    /** REST response body */
    public record MoveResponse(
        @JsonProperty("final_position")    PositionDto finalPosition,
        @JsonProperty("instruction_number") String instructionNumber
    ) {}

    /** Kafka payload on the move_instructions topic */
    public record MoveMessage(
        @JsonProperty("initial_position")  PositionDto initialPosition,
        @JsonProperty("move_instruction")  String moveInstruction,
        @JsonProperty("instruction_number") String instructionNumber
    ) {}

    /** Kafka payload on the instruction_results topic */
    public record ResultMessage(
        @JsonProperty("instruction_number") String instructionNumber,
        @JsonProperty("initial_position")   PositionDto initialPosition,
        @JsonProperty("final_position")     PositionDto finalPosition
    ) {}
}
