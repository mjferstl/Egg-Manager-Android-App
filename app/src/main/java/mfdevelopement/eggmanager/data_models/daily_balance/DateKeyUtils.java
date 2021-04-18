package mfdevelopement.eggmanager.data_models.daily_balance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateKeyUtils {

    public static final String DATE_KEY_FORMAT = "yyyyMMdd";

    public static String getYearByDateKey(String dateKey) {
        if (dateKey.length() >= 4) {
            return dateKey.substring(0, 4).trim();
        } else {
            return "";
        }
    }

    public static String getMonthByDateKey(String dateKey) {
        if (dateKey.length() >= 6) {
            return dateKey.substring(4, 6).trim();
        } else {
            return "";
        }
    }

    public static Date getDateByDateKey(String dateKey) throws ParseException {
        SimpleDateFormat sdfDateKeyFormat = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());
        return sdfDateKeyFormat.parse(dateKey);
    }
}
