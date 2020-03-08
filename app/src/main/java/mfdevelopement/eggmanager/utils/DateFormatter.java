package mfdevelopement.eggmanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    public static SimpleDateFormat sdf_human_readable = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String getHumanReadableDate(Date date) {
        return sdf_human_readable.format(date);
    }

    public static String getHumanReadableDate(long timeInMillis) {
        return sdf_human_readable.format(timeInMillis);
    }

    public static String getHumanReadableDate(Calendar calendar) {
        return getHumanReadableDate(calendar.getTimeInMillis());
    }

    public static Date parseHumanReadableDateString(String dateString) throws ParseException {
        return sdf_human_readable.parse(dateString);
    }
}
