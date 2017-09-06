package com.adven.concordion.extensions.exam.db;

import static java.lang.Integer.parseInt;

public class Range {
    int start;
    int end;
    private boolean up;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
        up = end > start;
    }

    public static Range from(String range) {
        if (isRange(range)) {
            String[] split = range.split("[.]{2}");
            return new Range(parseInt(split[0]), parseInt(split[1]));
        }
        throw new IllegalArgumentException("Couldn't parse range from string " + range);
    }

    public static boolean isRange(String range) {
        return range.matches("^[0-9]+[.]{2}[0-9]+$");
    }

    public int get(int index) {
        int result = start;
        for (int i = 0; i < index; i++) {
            if (up) {
                result++;
                if (result > end) {
                    result = start;
                }
            } else {
                result--;
                if (result < end) {
                    result = start;
                }
            }
        }
        return result;
    }
}