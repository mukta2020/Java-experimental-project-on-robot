package com.robot.grid.model;

/**
 * Immutable grid position. Both coordinates must be >= 0 (first quadrant only).
 */
public record Position(int x, int y) {

    public Position {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException(
                "Position must be in the first quadrant, got: (%d, %d)".formatted(x, y));
        }
    }

    /** Returns a new position after moving in the given direction by the given steps.
     *  Result is clamped to the first quadrant — the robot never crosses an axis. */
    public Position move(Direction dir, int steps) {
        return new Position(
            Math.max(0, x + dir.dx * steps),
            Math.max(0, y + dir.dy * steps)
        );
    }

    @Override
    public String toString() {
        return "(%d, %d)".formatted(x, y);
    }
}
