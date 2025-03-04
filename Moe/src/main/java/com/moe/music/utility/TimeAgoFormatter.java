package com.moe.music.utility;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeAgoFormatter {
    public static String formatTimeAgo(LocalDateTime pastTime) {
        LocalDateTime now = LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(pastTime, now);
        long days = ChronoUnit.DAYS.between(pastTime, now);
        long months = ChronoUnit.MONTHS.between(pastTime, now);
        long years = ChronoUnit.YEARS.between(pastTime, now);

        if (hours < 24) {
            return hours + "h";
        } else if (days < 30) {
            return days + " day";
        } else if (months < 12) {
            return months + "mon";
        } else {
            return years + " year";
        }
    }
}
