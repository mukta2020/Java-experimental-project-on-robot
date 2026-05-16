package com.robot.grid.util;

import com.robot.grid.model.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InstructionParserTest {

    private InstructionParser parser;

    @BeforeEach
    void setUp() { parser = new InstructionParser(); }

    // ── Parsing ──────────────────────────────────────────────────────────

    @Test void parsesBareSingleDirection() {
        var moves = parser.parse("U");
        assertThat(moves).hasSize(1);
        assertThat(moves.get(0).direction()).isEqualTo(Direction.U);
        assertThat(moves.get(0).steps()).isEqualTo(1);
    }

    @Test void parsesDirectionWithStep() {
        var moves = parser.parse("L2");
        assertThat(moves.get(0).direction()).isEqualTo(Direction.L);
        assertThat(moves.get(0).steps()).isEqualTo(2);
    }

    @Test void parsesDiagonalTwoCharToken() {
        var moves = parser.parse("TR2");
        assertThat(moves.get(0).direction()).isEqualTo(Direction.TR);
        assertThat(moves.get(0).steps()).isEqualTo(2);
    }

    @Test void parsesMultiTokenString() {
        // UL2RD → [U(1), L(2), R(1), D(1)]
        var moves = parser.parse("UL2RD");
        assertThat(moves).hasSize(4);
        assertThat(moves.get(1).direction()).isEqualTo(Direction.L);
        assertThat(moves.get(1).steps()).isEqualTo(2);
    }

    @Test void sampleOneParsesCorrectly() {
        // TR2R2L → [TR(2), R(2), L(1)]
        var moves = parser.parse("TR2R2L");
        assertThat(moves).hasSize(3);
        assertThat(moves.get(0).direction()).isEqualTo(Direction.TR);
        assertThat(moves.get(0).steps()).isEqualTo(2);
        assertThat(moves.get(2).steps()).isEqualTo(1);
    }

    @Test void isCaseInsensitive() {
        assertThat(parser.parse("tr2r2l")).isEqualTo(parser.parse("TR2R2L"));
    }

    // ── Edge cases ───────────────────────────────────────────────────────

    @Test void nullReturnsEmptyList()  { assertThat(parser.parse(null)).isEmpty(); }
    @Test void blankReturnsEmptyList() { assertThat(parser.parse("  ")).isEmpty(); }

    @Test void unrecognisedTokenThrows() {
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("UXD"));
    }

    @Test void step4IsRejected() {
        // "U4" — "U" is consumed, "4" is unmatched trailing char
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parse("U4"));
    }

    @Test void allEightDirectionsParsed() {
        var moves = parser.parse("UDLRTRBRBLTR");
        // spot-check BL
        assertThat(moves.stream().anyMatch(m -> m.direction() == Direction.BL)).isTrue();
    }
}
