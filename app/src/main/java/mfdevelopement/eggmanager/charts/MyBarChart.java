package mfdevelopement.eggmanager.charts;

import android.content.Context;
import android.util.AttributeSet;

public class MyBarChart extends MyGenericBarChart {
    public MyBarChart(Context context) {
        super(context);
        initChart();
    }

    public MyBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public MyBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    private void initChart() {
        // hide the legend
        this.getLegend().setEnabled(false);

        // disable the description
        this.getDescription().setEnabled(false);
    }
}
