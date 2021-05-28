package mfdevelopement.eggmanager.charts;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

public interface IGenericChart {

    /**
     * Set the data, which should be displayed in the chart
     *
     * @param dataSet DataSet which contains the data to be displayed
     */
    <T extends Entry> void setChartData(IDataSet<T> dataSet);

    <T extends Entry> IDataSet<T> getChartData();

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
