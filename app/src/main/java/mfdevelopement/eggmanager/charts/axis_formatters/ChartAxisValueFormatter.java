package mfdevelopement.eggmanager.charts.axis_formatters;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import mfdevelopement.eggmanager.charts.ReferenceDate;

public class ChartAxisValueFormatter extends ValueFormatter {

    private final AxisDateFormat axisDateFormat;
    private final SimpleDateFormat sdf;

    public ChartAxisValueFormatter(AxisDateFormat axisDateFormat) {
        SimpleDateFormat sdf;
        switch (axisDateFormat) {
            case MONTH_YEAR:
                sdf = AxisDateFormat.getSdfMonthYear();
                break;
            case DAY_MONTH_YEAR:
                sdf = AxisDateFormat.getSdfDayMonthYear();
                break;
            case DAY_MONTH:
                sdf = AxisDateFormat.getSdfDayMonth();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + axisDateFormat);
        }
        this.sdf = sdf;
        this.axisDateFormat = axisDateFormat;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return sdf.format(getDate((int) value).getTimeInMillis());
    }

    private Calendar getDate(int value) {
        Calendar cal = ReferenceDate.getReferenceDate();
        switch (axisDateFormat) {
            case DAY_MONTH:
            case DAY_MONTH_YEAR:
                cal.add(Calendar.DAY_OF_MONTH, value);
                break;
            case MONTH_YEAR:
                cal.add(Calendar.MONTH, value);
                break;
        }
        return cal;
    }
}
