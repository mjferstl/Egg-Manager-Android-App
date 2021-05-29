package mfdevelopement.eggmanager.charts;

import android.content.Context;

import com.github.mikephil.charting.data.BaseDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public interface IGenericChart {

    /**
     * Set the data, which should be displayed in the chart
     *
     * @param dataSet DataSet which contains the data to be displayed
     */
    <T extends Entry> void setChartData(Context context, BaseDataSet<T> dataSet);

    <T extends Entry> BaseDataSet<T> getChartData();

    /**
     * Hide the Chart
     */
    void hideChart();

    /**
     * Make the chart visible
     */
    void showChart();

    void setXAxisValueFormatter(ValueFormatter valueFormatter);
}
