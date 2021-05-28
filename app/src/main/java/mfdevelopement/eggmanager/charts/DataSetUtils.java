package mfdevelopement.eggmanager.charts;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import mfdevelopement.eggmanager.R;

public class DataSetUtils {

    private static final String LOG_TAG = "DataSetUtils";

    private static final int TEXT_SIZE = 12;
    private static final int DATA_LINE_WIDTH = 3;

    public static <T extends Entry> List<T> getEntriesOfDataSet(IDataSet<T> dataSet) {
        List<T> entries = new ArrayList<>();
        for (int i = 0; i < dataSet.getEntryCount(); i++)
            entries.add(dataSet.getEntryForIndex(i));
        return entries;
    }

    public static LineData createLineData(ILineDataSet lineDataSet) {
        // line data - containing all data for a chart
        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        return lineData;
    }

    public static BarData createBarData(IBarDataSet barDataSet) {
        // bar data - containing all data for a chart
        BarData barData = new BarData();
        barData.addDataSet(barDataSet);
        return barData;
    }

    public static LineDataSet convertToLineDataSet(Context context, IDataSet<Entry> dataSet) {
        return DataSetUtils.createLineDataSet(context, DataSetUtils.getEntriesOfDataSet(dataSet), dataSet.getLabel());
    }

    public static BarDataSet convertToBarDataSet(Context context, IDataSet<Entry> dataSet) {
        return DataSetUtils.createBarDataSet(context, DataSetUtils.getEntriesOfDataSet(dataSet), dataSet.getLabel());
    }

    public static LineDataSet createLineDataSet(Context context, List<Entry> entries, String dataSetName) {
        LineDataSet dataSet = new LineDataSet(entries, dataSetName);

        // sometimes there is no context and the resources cannot be fetched
        // but then a NullPointerException is thrown
        if (context != null) {
            Log.e(LOG_TAG, "createLineDataSet: context is null");
            dataSet.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
            dataSet.setValueTextColor(ContextCompat.getColor(context, R.color.main_text_color));
        }

        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(DATA_LINE_WIDTH);
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        dataSet.setDrawValues(entries.size() <= 5);     // do not show the value of each data point for large data

        return dataSet;
    }

    public static BarDataSet createBarDataSet(Context context, List<Entry> entries, String dataSetName) {
        List<BarEntry> barEntries = new ArrayList<>();
        for (Entry entry : entries) {
            barEntries.add(new BarEntry(entry.getX(), entry.getY()));
        }
        BarDataSet dataSet = new BarDataSet(barEntries, dataSetName);

        if (context == null) {
            Log.e(LOG_TAG, "createBarDataSet: context is null");
            return null;
        }

        dataSet.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        dataSet.setValueTextColor(ContextCompat.getColor(context, R.color.main_text_color));
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        dataSet.setDrawValues(barEntries.size() <= 5);     // do not show the value of each data point for large data
        return dataSet;
    }
}
