package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.viewmodels.LineChartViewModel;

public class LineChartActivity extends Fragment {

    private final int DATA_LINE_WIDTH = 3;
    private final int TEXT_SIZE = 12;
    private final String LOG_TAG = "LineChartActivity";
    private final int GRANULARITY_DAY = 1;

    private LineChartViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diagrams, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this).get(LineChartViewModel.class);

        // get references to GUI elements
        TextView txtv_title = root.findViewById(R.id.txtv_chart_title);
        LineChart chart = root.findViewById(R.id.line_chart);

        // modify chart layout
        chart.setBackgroundColor(getResources().getColor(R.color.transparent));     // transparent background
        chart.setBorderColor(getResources().getColor(R.color.main_text_color));     // color of the chart borders
        chart.getLegend().setEnabled(false);                                        // hide the legend

        // Description
        Description description = chart.getDescription();   // get description
        String desc = "Statistik im Oktober";
        description.setText(desc);                          // text for description
        description.setEnabled(false);                      // hide description

        // DataSet for all collected eggs
        LineDataSet dataSetEggsCollected = getDataSetEggsCollected();
        List<Entry> eggsCollectedList = getEntriesOfLineDataSet(dataSetEggsCollected);
        txtv_title.setText(getString(R.string.txt_title_eggs_collected));

        // modify both axes
        float xMin = (int)eggsCollectedList.get(0).getX();
        float xMax = eggsCollectedList.get(eggsCollectedList.size()-1).getX();
        float yMin = 0f;
        float yMax = roundToNextFive(getMax(eggsCollectedList));
        modifyAxes(chart, xMin, xMax, yMin, yMax);

        // line data - containing all data for a chart
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetEggsCollected);
        // add line data to the chart
        chart.setData(lineData);
        chart.invalidate();             // refresh the chart

        return root;
    }

    private void modifyAxes(LineChart chart, float xMin, float xMax, float yMin, float yMax) {
        modifyXAxis(chart, xMin, xMax);
        modifyYAxis(chart, yMin, yMax);
    }

    private void modifyXAxis(LineChart chart, float min, float max) {
        // // X-Axis Style // //
        XAxis xAxis;
        xAxis = chart.getXAxis();                       // get the x axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // axis at the bottom of the diagram
        xAxis.setGranularity(GRANULARITY_DAY);          // minimum difference between axis labels
        xAxis.setTextSize(TEXT_SIZE);                   // text size
        xAxis.setTextColor(getResources().getColor(R.color.main_text_color)); // text color
        xAxis.setValueFormatter(new AxisDateFormatter());
        xAxis.setAxisMinimum(min);
        xAxis.setAxisMaximum(max);
    }

    private void modifyYAxis(LineChart chart, float min, float max) {
        // // Y-Axis Style // //
        YAxis yAxis;
        yAxis = chart.getAxisLeft();                    // get the y axis
        chart.getAxisRight().setEnabled(false);         // disable dual axis (only use LEFT axis)

        yAxis.setAxisMaximum(max);
        yAxis.setAxisMinimum(min);
        yAxis.setGranularity(1f);
        yAxis.setTextColor(getResources().getColor(R.color.main_text_color));
        yAxis.setTextSize(TEXT_SIZE);
        yAxis.setCenterAxisLabels(true);
        yAxis.setDrawLabels(true);
        yAxis.setDrawGridLinesBehindData(true);
        yAxis.setLabelCount((int)max/5+1);
        Log.d(LOG_TAG,"yAxis label count: " + yAxis.getLabelCount());
    }

    private LineDataSet getDataSetEggsCollected() {
        // line data set for collected eggs
        String dataSetName = "Abgenommene Eier";
        List<Entry> dataEggsCollected = viewModel.getDataEggsCollected(viewModel.getFilteredDailyBalance());
        LineDataSet dataSet = new LineDataSet(dataEggsCollected, dataSetName);
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(DATA_LINE_WIDTH);
        dataSet.setValueTextColor(getResources().getColor(R.color.main_text_color));
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        if (dataEggsCollected.size() > 5) {
            dataSet.setDrawValues(false);                   // do not show the value of each data point
        } else {
            dataSet.setDrawValues(true);
        }
        return dataSet;
    }

    private List<Entry> getEntriesOfLineDataSet(LineDataSet lineDataSet) {
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<lineDataSet.getEntryCount(); i++){
            entries.add(lineDataSet.getEntryForIndex(i));
        }
        return entries;
    }

    private float getMax(List<Entry> entryList) {
        float maxValue = entryList.get(0).getY();
        for (Entry e: entryList) {
            if (e.getY() > maxValue) {
                maxValue = e.getY();
            }
        }
        return maxValue;
    }

    private int roundToNextFive(float value) {
        return ((int)value/5)*5+5;
    }

    /**
     * Class for formatting the x axis labels
     */
    private class AxisDateFormatter extends ValueFormatter {

        private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            Calendar cal = LineChartViewModel.getReferenceCalendar();
            cal.add(Calendar.DAY_OF_MONTH,(int)value);
            return sdf.format(new Date(cal.getTimeInMillis()));
        }
    }
}
