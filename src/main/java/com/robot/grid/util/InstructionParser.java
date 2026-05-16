package com.robot.grid.util;

import com.robot.grid.model.Direction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a move-instruction string into a list of (Direction, steps) pairs.
 *
 * Grammar:  token* where token = direction steps?
 *           direction = TR | TL | BR | BL | U | D | L | R
 *           steps     = 1 | 2 | 3  (defaults to 1)
 *
 * Two-character tokens are matched before single-character ones to avoid
 * "TR2" being read as "T + R2".
 */
@Component
public class InstructionParser {

    // Two-char directions must come before single-char to avoid partial matches
    private static final Pattern TOKEN = Pattern.compile("(TR|TL|BR|BL|U|D|L|R)([1-3]?)");

    public record Move(Direction direction, int steps) {}

    public List<Move> parse(String instruction) {
        if (instruction == null || instruction.isBlank()) return List.of();

        var moves   = new ArrayList<Move>();
        Matcher m   = TOKEN.matcher(instruction.toUpperCase());
        int consumed = 0;

        while (m.find()) {
            if (m.start() != consumed) {
                throw new IllegalArgumentException(
                    "Unrecognised token at index %d in: '%s'".formatted(consumed, instruction));
            }
            consumed = m.end();
            int steps = m.group(2).isEmpty() ? 1 : Integer.parseInt(m.group(2));
            moves.add(new Move(Direction.valueOf(m.group(1)), steps));
        }

        if (consumed != instruction.length()) {
            throw new IllegalArgumentException(
                "Unrecognised trailing characters in: '%s'".formatted(instruction));
        }

        return List.copyOf(moves);
    }
}
