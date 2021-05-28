package mfdevelopement.eggmanager.charts;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.data.Entry;

public class MyLineChart<T extends Entry> extends MyGenericLineChart<T> {

    public MyLineChart(Context context) {
        super(context);
        initChart();
    }

    public MyLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public MyLineChart(Context context, AttributeSet attrs, int defStyle) {
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
