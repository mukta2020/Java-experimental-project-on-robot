package com.robot.grid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class RobotSimulatorTest {

    @ParameterizedTest(name = "({0},{1}) + {2} → ({3},{4})")
    @CsvSource({
        "10, 10, UL2RD,     9, 10",
        " 0,  0, LDDDLRDU,  1,  1",
        " 0,  0, TR2R2L,    3,  2"
    })
    void sampleCases(int sx, int sy, String instr, int ex, int ey) {
        assertThat(RobotSimulator.simulate(sx, sy, instr)).containsExactly(ex, ey);
    }

    @Test void clampsLeftAtZero()   { assertThat(RobotSimulator.simulate(0, 5, "L3")[0]).isZero(); }
    @Test void clampsBottomAtZero() { assertThat(RobotSimulator.simulate(5, 0, "D3")[1]).isZero(); }
    @Test void staysAtOrigin()      { assertThat(RobotSimulator.simulate(0, 0, "BL3")).containsExactly(0, 0); }

    @Test void allEightDirections() {
        assertThat(RobotSimulator.simulate(5,5,"U")).containsExactly(5,6);
        assertThat(RobotSimulator.simulate(5,5,"D")).containsExactly(5,4);
        assertThat(RobotSimulator.simulate(5,5,"L")).containsExactly(4,5);
        assertThat(RobotSimulator.simulate(5,5,"R")).containsExactly(6,5);
        assertThat(RobotSimulator.simulate(5,5,"TR")).containsExactly(6,6);
        assertThat(RobotSimulator.simulate(5,5,"TL")).containsExactly(4,6);
        assertThat(RobotSimulator.simulate(5,5,"BR")).containsExactly(6,4);
        assertThat(RobotSimulator.simulate(5,5,"BL")).containsExactly(4,4);
    }

    @Test void invalidInstruction_throws() {
        assertThatIllegalArgumentException().isThrownBy(() -> RobotSimulator.simulate(0, 0, "U4X"));
    }
}
