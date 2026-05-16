package com.robot.grid.service;

import com.robot.grid.model.Position;
import com.robot.grid.util.InstructionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core service responsible for:
 *   1. Executing move instructions and returning the final position.
 *   2. Managing the set of golden points and checking arrivals.
 *   3. Generating unique instruction numbers (IN000001, IN000002, ...).
 */
@Service
public class RobotService {

    private static final Logger log = LoggerFactory.getLogger(RobotService.class);

    private final InstructionParser parser;

    // Thread-safe golden-point registry — populated by the Kafka consumer
    private final Set<Position> goldenPoints = ConcurrentHashMap.newKeySet();

    // Monotonically increasing counter for instruction IDs
    private final AtomicLong counter = new AtomicLong(0);

    public RobotService(InstructionParser parser) {
        this.parser = parser;
    }

    /** Parses and executes all moves from the given instruction string. */
    public Position execute(Position start, String instruction) {
        Position current = start;
        for (var move : parser.parse(instruction)) {
            current = current.move(move.direction(), move.steps());
        }
        log.debug("execute '{}' from {} → {}", instruction, start, current);
        return current;
    }

    /** Returns the next instruction number, e.g. "IN000001". */
    public String nextInstructionNumber() {
        return "IN%06d".formatted(counter.incrementAndGet());
    }

    /** Registers a golden point from the golden_points Kafka topic. */
    public void addGoldenPoint(Position p) {
        goldenPoints.add(p);
        log.info("Golden point registered: {}", p);
    }

    /**
     * Checks whether the robot's position is a golden point.
     * Prints the required message to stdout if it matches.
     */
    public boolean checkGoldenPoint(Position position, String instructionNumber) {
        if (goldenPoints.contains(position)) {
            System.out.println("I have reached a golden point!");
            log.info("[{}] Reached golden point at {}", instructionNumber, position);
            return true;
        }
        return false;
    }

    // Exposed for testing
    public Set<Position> getGoldenPoints() {
        return Set.copyOf(goldenPoints);
    }
}
