package com.robot.grid;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 
 */
public class RobotSimulator {

    enum Dir { U(0,1), D(0,-1), L(-1,0), R(1,0), TR(1,1), TL(-1,1), BR(1,-1), BL(-1,-1);
        final int dx, dy;
        Dir(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    private static final Pattern TOKEN = Pattern.compile("(TR|TL|BR|BL|U|D|L|R)([1-3]?)");

    public static int[] simulate(int x, int y, String instruction) {
        Matcher m = TOKEN.matcher(instruction.toUpperCase());
        int pos = 0;
        while (m.find()) {
            if (m.start() != pos) throw new IllegalArgumentException("Bad token at index " + pos);
            pos = m.end();
            Dir dir  = Dir.valueOf(m.group(1));
            int steps = m.group(2).isEmpty() ? 1 : Integer.parseInt(m.group(2));
            x = Math.max(0, x + dir.dx * steps);
            y = Math.max(0, y + dir.dy * steps);
        }
        if (pos != instruction.length()) throw new IllegalArgumentException("Trailing characters");
        return new int[]{x, y};
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Starting position (x, y): ");
        String[] parts = sc.nextLine().trim().split("[,\\s]+");
        System.out.print("Move instruction: ");
        String instruction = sc.nextLine().trim();

        int[] result = simulate(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), instruction);
        System.out.println(result[0] + ", " + result[1]);
    }
}
