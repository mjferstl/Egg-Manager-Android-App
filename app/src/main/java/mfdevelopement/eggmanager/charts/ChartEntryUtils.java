package mfdevelopement.eggmanager.charts;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

import mfdevelopement.eggmanager.data_models.ChartAxisLimits;

public class ChartEntryUtils {

    private ChartEntryUtils() {
    }

    public static ChartAxisLimits getChartDataLimits(@NonNull List<Entry> entryList) {
        List<Float> xValues = getXValues(entryList);
        List<Float> yValues = getYValues(entryList);
        float xMin = getMin(xValues);
        float xMax = getMax(xValues);
        float yMin = getMin(yValues);
        float yMax = getMax(yValues);
        return new ChartAxisLimits(xMin, xMax, yMin, yMax);
    }

    private static List<Float> getXValues(@NonNull List<Entry> entryList) {
        List<Float> xValues = new ArrayList<>();
        for (Entry entry : entryList)
            xValues.add(entry.getX());
        return xValues;
    }

    private static List<Float> getYValues(@NonNull List<Entry> entryList) {
        List<Float> yValues = new ArrayList<>();
        for (Entry entry : entryList)
            yValues.add(entry.getY());
        return yValues;
    }

    private static float getMin(@NonNull List<Float> floatList) {
        if (floatList.size() == 0) return 0f;
        float min = floatList.get(0);
        for (float f : floatList) {
            if (f < min) min = f;
        }
        return min;
    }

    private static float getMax(@NonNull List<Float> floatList) {
        if (floatList.size() == 0) return 0f;
        float max = floatList.get(0);
        for (float f : floatList) {
            if (f > max) max = f;
        }
        return max;
    }
}
