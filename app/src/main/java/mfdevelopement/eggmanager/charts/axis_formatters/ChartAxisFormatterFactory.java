package mfdevelopement.eggmanager.charts.axis_formatters;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class ChartAxisFormatterFactory {

    private ChartAxisFormatterFactory() {
    }

    public static ValueFormatter getInstance(AxisDateFormat axisDateFormat) {
        switch (axisDateFormat) {
            case MONTH_YEAR:
                return new ChartAxisValueFormatter(AxisDateFormat.MONTH_YEAR);
            case DAY_MONTH_YEAR:
                return new ChartAxisValueFormatter(AxisDateFormat.DAY_MONTH_YEAR);
            case DAY_MONTH:
                return new ChartAxisValueFormatter(AxisDateFormat.DAY_MONTH);
            default:
                throw new RuntimeException("ChartAxisFormatterFactory: cannot return a value for argument axisDateFormat=" + axisDateFormat);
        }
    }
}
