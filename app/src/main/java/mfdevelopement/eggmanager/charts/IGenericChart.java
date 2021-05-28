package mfdevelopement.eggmanager.charts;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.Entry;

public interface IGenericChart<T extends Entry> {

    /**
     * Set the data, which should be displayed in the chart
     *
     * @param dataSet DataSet which contains the data to be displayed
     */
    void setChartData(BarLineScatterCandleBubbleDataSet<T> dataSet);

    BarLineScatterCandleBubbleDataSet<T> getChartData();

    /**
     * Hide the Chart
     */
    void hideChart();

    /**
     * Make the chart visible
     */
    void showChart();
}
