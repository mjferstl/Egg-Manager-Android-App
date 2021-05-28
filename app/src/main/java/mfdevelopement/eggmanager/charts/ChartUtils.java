package mfdevelopement.eggmanager.charts;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.util.List;

import mfdevelopement.eggmanager.data_models.ChartAxisLimits;

public class ChartUtils {

    public static int TEXT_SIZE = 12;

    public static <T extends BarLineScatterCandleBubbleData<? extends IBarLineScatterCandleBubbleDataSet<? extends Entry>>> void setChartAxisLimits(BarLineChartBase<T> chart, ChartAxisLimits chartAxisLimits) {
        ChartUtils.setChartAxisLimitsX(chart, chartAxisLimits.getxMin(), chartAxisLimits.getxMax());
        ChartUtils.setChartAxisLimitsY(chart, chartAxisLimits.getyMin(), chartAxisLimits.getyMax());
    }

    private static <T extends BarLineScatterCandleBubbleData<? extends IBarLineScatterCandleBubbleDataSet<? extends Entry>>> void setChartAxisLimitsX(BarLineChartBase<T> chart, float min, float max) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // axis at the bottom of the diagram

        // set the limits
        xAxis.setAxisMinimum(min);
        xAxis.setAxisMaximum(max);
    }

    private static <T extends BarLineScatterCandleBubbleData<? extends IBarLineScatterCandleBubbleDataSet<? extends Entry>>> void setChartAxisLimitsY(BarLineChartBase<T> chart, float min, float max) {
        YAxis yAxis = chart.getAxisLeft();

        // set the limits
        yAxis.setAxisMaximum(max);
        yAxis.setAxisMinimum(min);

        yAxis.setGranularity(1f);

        // label text in density pixels
        yAxis.setTextSize(TEXT_SIZE);

        //yAxis.setCenterAxisLabels(true);

        // draw the labels
        yAxis.setDrawLabels(true);

        // draw the grid behind the data
        yAxis.setDrawGridLinesBehindData(true);
    }

    public static <T extends Entry> ChartAxisLimits calcAxisLimits(IDataSet<T> dataSet) {
        List<T> entriesList = DataSetUtils.getEntriesOfDataSet(dataSet);

        ChartAxisLimits axisLimits = ChartEntryUtils.getChartDataLimits(entriesList);

        // set the max y value properly
        float yMax = axisLimits.getyMax();
        float maxY;
        if (yMax < 50) {
            maxY = roundToNextNumber(yMax, 5) + 5;
        } else if (yMax < 100) {
            maxY = roundToNextNumber(yMax, 10) + 10;
        } else if (yMax < 1000) {
            maxY = roundToNextNumber(yMax, 100) + 100;
        } else {
            maxY = roundToNextNumber(yMax, 1000) + 1000;
        }

        axisLimits.setyMax(maxY);
        axisLimits.setyMin(0f);

        return axisLimits;
    }

    private static int roundToNextNumber(float value, int stepSize) {
        return ((int) value / stepSize) * stepSize + stepSize;
    }
}
