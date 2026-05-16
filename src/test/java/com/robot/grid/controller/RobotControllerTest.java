package com.robot.grid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.grid.dto.Dtos.*;
import com.robot.grid.kafka.MoveProducer;
import com.robot.grid.service.RobotService;
import com.robot.grid.util.InstructionParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RobotController.class)
@Import({RobotService.class, InstructionParser.class})
class RobotControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean MoveProducer moveProducer; // keep Kafka out of this slice

    private static final String URL = "/api/v1/robot/move";

    // ── Sample cases ────────────────────────────────────────────────────

    @Test void sample1_returns9_10() throws Exception {
        assertMove(new MoveRequest(new PositionDto(10, 10), "UL2RD"), 9, 10);
    }

    @Test void sample2_returns1_1() throws Exception {
        assertMove(new MoveRequest(new PositionDto(0, 0), "LDDDLRDU"), 1, 1);
    }

    @Test void sample3_returns3_2() throws Exception {
        assertMove(new MoveRequest(new PositionDto(0, 0), "TR2R2L"), 3, 2);
    }

    @Test void responseContainsInstructionNumber() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new MoveRequest(new PositionDto(0, 0), "U"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instruction_number").value(org.hamcrest.Matchers.matchesPattern("IN\\d{6}")));
    }

    // ── Validation errors ────────────────────────────────────────────────

    @Test void missingInitialPosition_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "move_instruction": "U" }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test void blankMoveInstruction_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "initial_position": {"x": 0, "y": 0}, "move_instruction": "" }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test void negativeCoordinate_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "initial_position": {"x": -1, "y": 0}, "move_instruction": "U" }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test void malformedInstruction_returns400() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "initial_position": {"x": 0, "y": 0}, "move_instruction": "U4X" }
                """))
            .andExpect(status().isBadRequest());
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private void assertMove(MoveRequest req, int expectedX, int expectedY) throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.final_position.x").value(expectedX))
            .andExpect(jsonPath("$.final_position.y").value(expectedY));
    }
}
