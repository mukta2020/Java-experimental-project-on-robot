package com.robot.grid.service;

import com.robot.grid.model.Position;
import com.robot.grid.util.InstructionParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

class RobotServiceTest {

    private RobotService service;

    @BeforeEach
    void setUp() { service = new RobotService(new InstructionParser()); }

    // ── Sample cases from problem statement ─────────────────────────────

    @ParameterizedTest(name = "({0},{1}) + {2} → ({3},{4})")
    @CsvSource({
        "10, 10, UL2RD,     9, 10",
        " 0,  0, LDDDLRDU,  1,  1",
        " 0,  0, TR2R2L,    3,  2"
    })
    void sampleCases(int sx, int sy, String instr, int ex, int ey) {
        Position result = service.execute(new Position(sx, sy), instr);
        assertThat(result).isEqualTo(new Position(ex, ey));
    }

    // ── All 8 directions from (5, 5) ────────────────────────────────────

    @Test void movesUp()          { assertThat(service.execute(new Position(5,5), "U2")).isEqualTo(new Position(5,7)); }
    @Test void movesDown()        { assertThat(service.execute(new Position(5,5), "D2")).isEqualTo(new Position(5,3)); }
    @Test void movesLeft()        { assertThat(service.execute(new Position(5,5), "L2")).isEqualTo(new Position(3,5)); }
    @Test void movesRight()       { assertThat(service.execute(new Position(5,5), "R2")).isEqualTo(new Position(7,5)); }
    @Test void movesTR()          { assertThat(service.execute(new Position(5,5), "TR2")).isEqualTo(new Position(7,7)); }
    @Test void movesTL()          { assertThat(service.execute(new Position(5,5), "TL2")).isEqualTo(new Position(3,7)); }
    @Test void movesBR()          { assertThat(service.execute(new Position(5,5), "BR2")).isEqualTo(new Position(7,3)); }
    @Test void movesBL()          { assertThat(service.execute(new Position(5,5), "BL2")).isEqualTo(new Position(3,3)); }

    // ── Boundary clamping ────────────────────────────────────────────────

    @Test void clampsXAtZero()   { assertThat(service.execute(new Position(1,5), "L3").x()).isZero(); }
    @Test void clampsYAtZero()   { assertThat(service.execute(new Position(5,1), "D3").y()).isZero(); }
    @Test void staysAtOrigin()   { assertThat(service.execute(new Position(0,0), "BLBLBL")).isEqualTo(new Position(0,0)); }

    // ── Empty instruction ─────────────────────────────────────────────────

    @Test void emptyInstructionReturnsStart() {
        Position start = new Position(3, 7);
        assertThat(service.execute(start, "")).isEqualTo(start);
    }

    // ── Instruction numbers ──────────────────────────────────────────────

    @Test void instructionNumbersAreSequential() {
        String first  = service.nextInstructionNumber();
        String second = service.nextInstructionNumber();
        assertThat(first).matches("IN\\d{6}");
        assertThat(first).isLessThan(second);   // lexicographic < works for zero-padded fixed-width
    }

    @Test void instructionNumbersAreUniqueUnderConcurrency() throws InterruptedException {
        int threads = 100;
        Set<String> seen = ConcurrentHashMap.newKeySet();
        var latch = new CountDownLatch(threads);
        try (var executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> { seen.add(service.nextInstructionNumber()); latch.countDown(); });
            }
            latch.await();
        }
        assertThat(seen).hasSize(threads);
    }

    // ── Golden points ────────────────────────────────────────────────────

    @Test void goldenPointMissReturnsFalse() {
        assertThat(service.checkGoldenPoint(new Position(5,5), "IN000001")).isFalse();
    }

    @Test void goldenPointHitReturnsTrue() {
        service.addGoldenPoint(new Position(9, 10));
        assertThat(service.checkGoldenPoint(new Position(9, 10), "IN000001")).isTrue();
    }

    @Test void multipleGoldenPoints_onlyMatchingHits() {
        service.addGoldenPoint(new Position(1, 1));
        service.addGoldenPoint(new Position(5, 5));
        assertThat(service.checkGoldenPoint(new Position(5, 5), "IN000002")).isTrue();
        assertThat(service.checkGoldenPoint(new Position(2, 2), "IN000003")).isFalse();
    }

    @Test void getGoldenPointsIsUnmodifiable() {
        service.addGoldenPoint(new Position(3, 3));
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> service.getGoldenPoints().add(new Position(4, 4)));
    }
}
