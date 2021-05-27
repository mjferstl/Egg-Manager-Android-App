package mfdevelopement.eggmanager.charts;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.data.Entry;

public interface IGenericChart {

    <T extends Entry> void setChartData(BarLineScatterCandleBubbleDataSet<T> dataSet);

    void hideChart();

    void showChart();
}
