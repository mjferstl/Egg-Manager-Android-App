package mfdevelopement.eggmanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    /**
     * Human readable date format used in UI
     */
    public static SimpleDateFormat HUMAN_READABLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String getHumanReadableDate(Date date) {
        return HUMAN_READABLE_DATE_FORMAT.format(date);
    }

    public static String getHumanReadableDate(long timeInMillis) {
        return HUMAN_READABLE_DATE_FORMAT.format(timeInMillis);
    }

    public static String getHumanReadableDate(Calendar calendar) {
        return getHumanReadableDate(calendar.getTimeInMillis());
    }

    public static Date parseHumanReadableDateString(String dateString) throws ParseException {
        return HUMAN_READABLE_DATE_FORMAT.parse(dateString);
    }
}
