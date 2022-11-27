package com.konsl.fakecall.call.utils;

import java.time.Duration;

public class Interval {
    public static Duration getInterval(int id) {
        switch (id) {
            case 1:
                return Duration.ofSeconds(5);
            case 2:
                return Duration.ofSeconds(10);
            case 3:
                return Duration.ofSeconds(15);
            case 4:
                return Duration.ofSeconds(30);
            case 5:
                return Duration.ofMinutes(1);
            case 6:
                return Duration.ofMinutes(2);
            case 7:
                return Duration.ofMinutes(5);
            case 8:
                return Duration.ofMinutes(10);
            case 9:
                return Duration.ofMinutes(15);
            case 10:
                return Duration.ofMinutes(30);
            case 11:
                return Duration.ofHours(1);
            case 12:
                return Duration.ofHours(2);

            default:
                return Duration.ZERO;
        }
    }
}
