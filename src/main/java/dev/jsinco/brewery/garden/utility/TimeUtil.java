package dev.jsinco.brewery.garden.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static final Pattern DAYS_PATTERN = Pattern.compile("(\\d+)d");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)m");

    public static final int SECOND = 20;
    public static final int MINUTE = SECOND * 60;
    public static final int DAY = 20 * MINUTE;

    private TimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static int parseTime(String timeString) {
        Matcher daysMatcher = DAYS_PATTERN.matcher(timeString);
        if (daysMatcher.matches()) {
            return Integer.parseInt(daysMatcher.group(1)) * DAY;
        }
        Matcher minutesMatcher = MINUTES_PATTERN.matcher(timeString);
        if (minutesMatcher.matches()) {
            return Integer.parseInt(minutesMatcher.group(1)) * MINUTE;
        }
        throw new IllegalArgumentException("Not a supported time string");
    }
}
