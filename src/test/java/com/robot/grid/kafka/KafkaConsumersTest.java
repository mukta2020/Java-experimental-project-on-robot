package com.robot.grid.kafka;

import com.robot.grid.dto.Dtos.*;
import com.robot.grid.model.Position;
import com.robot.grid.service.RobotService;
import com.robot.grid.util.InstructionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumersTest {

    // ── MoveConsumer ─────────────────────────────────────────────────────

    @Nested
    class MoveConsumerTest {

        @Mock ResultProducer resultProducer;
        MoveConsumer consumer;

        @BeforeEach
        void setUp() {
            consumer = new MoveConsumer(new RobotService(new InstructionParser()), resultProducer);
        }

        @Test void sample1_publishesCorrectResult() {
            consumer.consume(new MoveMessage(new PositionDto(10,10), "UL2RD", "IN000001"), "IN000001");
            var captor = ArgumentCaptor.forClass(ResultMessage.class);
            verify(resultProducer).send(captor.capture());
            assertThat(captor.getValue().finalPosition()).isEqualTo(new PositionDto(9, 10));
        }

        @Test void sample2_publishesCorrectResult() {
            consumer.consume(new MoveMessage(new PositionDto(0,0), "LDDDLRDU", "IN000002"), "IN000002");
            var captor = ArgumentCaptor.forClass(ResultMessage.class);
            verify(resultProducer).send(captor.capture());
            assertThat(captor.getValue().finalPosition()).isEqualTo(new PositionDto(1, 1));
        }

        @Test void sample3_publishesCorrectResult() {
            consumer.consume(new MoveMessage(new PositionDto(0,0), "TR2R2L", "IN000003"), "IN000003");
            var captor = ArgumentCaptor.forClass(ResultMessage.class);
            verify(resultProducer).send(captor.capture());
            assertThat(captor.getValue().finalPosition()).isEqualTo(new PositionDto(3, 2));
        }

        @Test void malformedInstruction_doesNotThrow_doesNotPublish() {
            assertDoesNotThrow(() ->
                consumer.consume(new MoveMessage(new PositionDto(0,0), "!!!BAD!!!", "IN999"), "IN999"));
            verify(resultProducer, never()).send(any());
        }
    }

    // ── ResultConsumer ───────────────────────────────────────────────────

    @Nested
    class ResultConsumerTest {

        @Mock RobotService robotService;
        ResultConsumer consumer;

        @BeforeEach
        void setUp() { consumer = new ResultConsumer(robotService); }

        @Test void delegatesCheckToService() {
            consumer.consume(new ResultMessage("IN000001", new PositionDto(0,0), new PositionDto(4,4)));
            verify(robotService).checkGoldenPoint(new Position(4, 4), "IN000001");
        }

        @Test void serviceException_doesNotPropagate() {
            doThrow(new RuntimeException("boom")).when(robotService).checkGoldenPoint(any(), any());
            assertDoesNotThrow(() ->
                consumer.consume(new ResultMessage("IN000002", new PositionDto(0,0), new PositionDto(1,1))));
        }
    }

    // ── GoldenPointConsumer ──────────────────────────────────────────────

    @Nested
    class GoldenPointConsumerTest {

        @Mock RobotService robotService;
        GoldenPointConsumer consumer;

        @BeforeEach
        void setUp() { consumer = new GoldenPointConsumer(robotService); }

        @Test void registersGoldenPointWithService() {
            consumer.consume(new PositionDto(5, 7));
            verify(robotService).addGoldenPoint(new Position(5, 7));
        }

        // GoldenPointConsumer calls new Position(x, y) BEFORE calling the service.
        // A negative coordinate throws inside the consumer's own try/catch, so the
        // service mock is never invoked — no doThrow stubbing needed here.
        @Test void invalidCoordinate_doesNotPropagate() {
            assertDoesNotThrow(() -> consumer.consume(new PositionDto(-1, 0)));
            verify(robotService, never()).addGoldenPoint(any());
        }
    }
}
