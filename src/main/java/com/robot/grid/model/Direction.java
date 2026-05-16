package com.robot.grid.model;

/**
 * The eight directions the robot can move, each carrying its x/y deltas.
 *
 * Coordinate system: x increases right, y increases up (standard first quadrant).
 *
 *  TL  U  TR
 *   L  .  R
 *  BL  D  BR
 */
public enum Direction {

    U(0, 1), D(0, -1), L(-1, 0), R(1, 0),
    TR(1, 1), TL(-1, 1), BR(1, -1), BL(-1, -1);

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
