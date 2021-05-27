package mfdevelopement.eggmanager.charts.axis_formatters;

import java.text.SimpleDateFormat;
import java.util.Locale;

public enum AxisDateFormat {
    DAY_MONTH,
    MONTH_YEAR,
    DAY_MONTH_YEAR;

    protected static SimpleDateFormat getSdfDayMonthYear() {
        return new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
    }

    protected static SimpleDateFormat getSdfMonthYear() {
        return new SimpleDateFormat("MM/yy", Locale.getDefault());
    }

    protected static SimpleDateFormat getSdfDayMonth() {
        return new SimpleDateFormat("dd.MM", Locale.getDefault());
    }
}
