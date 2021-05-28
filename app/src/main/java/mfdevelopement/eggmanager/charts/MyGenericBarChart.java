package mfdevelopement.eggmanager.charts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.Entry;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.charts.axis_formatters.AxisDateFormat;
import mfdevelopement.eggmanager.charts.axis_formatters.ChartAxisFormatterFactory;
import mfdevelopement.eggmanager.data_models.ChartAxisLimits;

public abstract class MyGenericBarChart<T extends Entry> extends BarChart implements IGenericChart<T> {

    private int maxYLabelCount = 8;
    private BarLineScatterCandleBubbleDataSet<T> dataSet;

    public MyGenericBarChart(Context context) {
        super(context);
        initChart();
    }

    public MyGenericBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public MyGenericBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    public void setChartAxisLimits(ChartAxisLimits chartAxisLimits) {
        ChartUtils.setChartAxisLimits(this, chartAxisLimits);
        this.getAxisLeft().setTextColor(ContextCompat.getColor(this.getContext(), R.color.main_text_color));
    }

    public void setChartData(BarLineScatterCandleBubbleDataSet<T> dataSet) {
        this.dataSet = dataSet;

        // modify both axes
        ChartAxisLimits axisLimits = ChartUtils.calcAxisLimits(dataSet);

        // set the number of labels
        this.getAxisLeft().setLabelCount(calcLabelCount(axisLimits, 5, maxYLabelCount));

        // calculate the limits of the axes
        this.setChartAxisLimits(axisLimits);

        // add line data to the chart
        this.setData(DataSetUtils.createBarData((BarDataSet) dataSet));

        // refresh the chart
        this.invalidate();
    }

    @Override
    public BarLineScatterCandleBubbleDataSet<T> getChartData() {
        return this.dataSet;
    }

    private int calcLabelCount(ChartAxisLimits chartAxisLimits, int stepSize, int maxLabels) {
        return calcLabelCount(chartAxisLimits.getyMin(), chartAxisLimits.getyMax(), stepSize, maxLabels);
    }

    private int calcLabelCount(float min, float max, int stepSize, int maxLabels) {
        if ((max - min) / stepSize < maxLabels) {
            return (int) (max - min) / stepSize;
        } else {
            return maxLabels;
        }
    }

    private void initChart() {
        // transparent background
        this.setBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.transparent));     // transparent background
        this.setBorderColor(ContextCompat.getColor(this.getContext(), R.color.main_text_color));     // color of the chart borders

        // text when there is no data to display
        this.setNoDataText(getResources().getString(R.string.chart_no_data_text));

        // Disable the second axis
        this.getAxisRight().setEnabled(false);

        this.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);  // axis at the bottom of the diagram

        // set the format for the dates on the x axis
        this.getXAxis().setValueFormatter(ChartAxisFormatterFactory.getInstance(AxisDateFormat.MONTH_YEAR));
    }

    public void hideChart() {
        this.setVisibility(View.GONE);
    }

    public void showChart() {
        this.setVisibility(View.VISIBLE);
    }

    public int getMaxYLabelCount() {
        return maxYLabelCount;
    }

    public void setMaxYLabelCount(int maxYLabelCount) {
        this.maxYLabelCount = maxYLabelCount;
    }
}
