package edu.uoc.pdp.core.utils;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Set;

public class DateUtils {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<Integer> VOWEL_START_DAY = Sets.newHashSet(1, 11);
    private static final Set<Integer> VOWEL_START_MONTH = Sets.newHashSet(4, 8, 10);

    private DateUtils() {
        // hide instantiation
    }

    /**
     * Parses a string into a {@link LocalDate} object using the pattern dd/MM/yyyy.
     *
     * @param date Date to be parsed
     * @return Parsed local date
     */
    public static LocalDate parse(String date) {
        return LocalDate.parse(date, DD_MM_YYYY);
    }

    /**
     * Formats a local date object using the format dd/MM/yyyy
     *
     * @param date Date to be formatted
     * @return Formatted date
     */
    public static String format(LocalDate date) {
        return date.format(DD_MM_YYYY);
    }

    /**
     * Formats a publish date using a long format.
     *
     * @param date Date to be formatted
     * @return Formatted date
     */
    public static String formatPublishDate(LocalDateTime date) {
        return "Publicat " + formatLongDate(date);
    }

    /**
     * Formats a rent creation date using a long format.
     *
     * @param date Date to be formatted
     * @return Formatted date
     */
    public static String formatCreationDate(LocalDateTime date) {
        return "Reservat " + formatLongDate(date);
    }

    /**
     * Formats a cancellation date using a long format.
     *
     * @param date Date to be formatted
     * @return Formatted date
     */
    public static String formatCancellationDate(LocalDateTime date) {
        return "Cancel·lat " + formatLongDate(date);
    }

    private static String formatLongDate(LocalDateTime date) {
        int month = date.getMonthValue();
        String monthName = StringUtils.lowerCase(Month.of(month)
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ca")));
        int day = date.getDayOfMonth();
        String dayPrefix = VOWEL_START_DAY.contains(day) ? "l'" : "el ";
        String monthPrefix = VOWEL_START_MONTH.contains(month) ? "d'" : "de ";

        return dayPrefix + day + " " + monthPrefix + monthName + " del " + date.getYear();
    }
}
