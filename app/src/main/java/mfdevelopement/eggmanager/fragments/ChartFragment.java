package mfdevelopement.eggmanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.data_models.ChartAxisLimits;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

import static mfdevelopement.eggmanager.fragments.DatabaseFragment.EDIT_FILTER_STRING_REQUEST_CODE;
import static mfdevelopement.eggmanager.fragments.DatabaseFragment.EXTRA_REQUEST_CODE_NAME;
import static mfdevelopement.eggmanager.utils.FilterActivityResultHandler.handleFilterActivityResult;

public class ChartFragment extends Fragment {

    private final int DATA_LINE_WIDTH = 3;
    private final int TEXT_SIZE = 12;
    private final String LOG_TAG = "ChartFragment";
    private final int GRANULARITY_DAY = 1;

    private SharedViewModel viewModel;

    private LineChart lineChart;
    private TextView txtv_title;

    public final static String NAME_EGGS_COLLECTED = "Abgenommene Eier";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_diagrams, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // this framgent has its own options menu
        setHasOptionsMenu(true);

        // get references to GUI elements
        txtv_title = root.findViewById(R.id.txtv_chart_title);
        txtv_title.setText(getString(R.string.txt_title_eggs_collected));

        lineChart = root.findViewById(R.id.line_chart);

        // modify chart layout
        lineChart.setBackgroundColor(getResources().getColor(R.color.transparent));     // transparent background
        lineChart.setBorderColor(getResources().getColor(R.color.main_text_color));     // color of the chart borders
        lineChart.getLegend().setEnabled(false);                                        // hide the legend

        // Description
        Description description = lineChart.getDescription();   // get description
        String desc = "";
        description.setText(desc);                          // text for description
        description.setEnabled(false);                      // hide description

        // init observers
        initObservers();

        return root;
    }

    private void modifyAxes(LineChart chart, float xMin, float xMax, float yMin, float yMax) {
        modifyXAxis(chart, xMin, xMax);
        modifyYAxis(chart, yMin, yMax);
    }

    private void modifyAxes(LineChart lineChart, ChartAxisLimits chartAxisLimits) {
        modifyAxes(lineChart, chartAxisLimits.getxMin(), chartAxisLimits.getxMax(), chartAxisLimits.getyMin(), chartAxisLimits.getyMax());
    }

    private ChartAxisLimits calcAxisLimits(LineDataSet lineDataSet) {
        List<Entry> eggsCollectedList = getEntriesOfLineDataSet(lineDataSet);

        // modify both axes
        float xMin = (int)eggsCollectedList.get(0).getX();
        float xMax = eggsCollectedList.get(eggsCollectedList.size()-1).getX();
        float yMin = 0f;
        float yMax = roundToNextFive(getMax(eggsCollectedList));

        return new ChartAxisLimits(xMin, xMax, yMin, yMax);
    }

    private void modifyXAxis(LineChart chart, float min, float max) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        // // X-Axis Style // //
        XAxis xAxis;
        xAxis = chart.getXAxis();                       // get the x axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // axis at the bottom of the diagram
        xAxis.setGranularity(GRANULARITY_DAY);          // minimum difference between axis labels
        xAxis.setTextSize(TEXT_SIZE);                   // text size
        xAxis.setTextColor(getResources().getColor(R.color.main_text_color)); // text color

        // get the first and the last day of the x data
        Calendar startDate = viewModel.getReferenceDate();
        Log.d(LOG_TAG,"modifyXAxis(): startDate = " + sdf.format(startDate.getTimeInMillis()));
        startDate.add(Calendar.DAY_OF_MONTH,(int)min);
        Log.d(LOG_TAG,"modifyXAxis(): updated startDate = " + sdf.format(startDate.getTimeInMillis()));
        Calendar endDate = viewModel.getReferenceDate();
        endDate.add(Calendar.DAY_OF_MONTH,(int)max);

        // If all dates are of the same year, then no year information is shown in the x labels
        String titlePrefix = getString(R.string.txt_title_eggs_collected);
        String title = titlePrefix;
        if (startDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)) {
            xAxis.setValueFormatter(new AxisDateFormatterWithOutYear());

            if (startDate.get(Calendar.MONTH) == endDate.get(Calendar.MONTH)) {
                String monthName = viewModel.getMonthNameByIndex(startDate.get(Calendar.MONTH)+1);
                title = titlePrefix + String.format(Locale.getDefault(), "\nim %s %d", monthName, startDate.get(Calendar.YEAR));
            } else {
                title = titlePrefix + String.format(Locale.getDefault(), "\nim Jahr %d", startDate.get(Calendar.YEAR));
            }
        } else {
            xAxis.setValueFormatter(new AxisDateFormatterWithYear());
        }

        txtv_title.setText(title);

        // set minimum and maximum value of the axis
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

    private LineDataSet createLineDataSet(List<Entry> entries, String dataSetName) {
        LineDataSet dataSet = new LineDataSet(entries, dataSetName);
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(DATA_LINE_WIDTH);
        dataSet.setValueTextColor(getResources().getColor(R.color.main_text_color));
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        if (entries.size() > 5) {
            dataSet.setDrawValues(false);                   // do not show the value of each data point
        } else {
            dataSet.setDrawValues(true);
        }
        return dataSet;
    }

    private List<Entry> getEntriesOfLineDataSet(LineDataSet lineDataSet) {
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<lineDataSet.getEntryCount(); i++)
            entries.add(lineDataSet.getEntryForIndex(i));
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

    private LineData createLineData(LineDataSet lineDataSet) {
        // line data - containing all data for a chart
        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        return lineData;
    }

    private void refreshChart(LineChart lineChart) {
        lineChart.invalidate();
    }

    private void updateChart(LineChart lineChart, LineDataSet lineDataSet) {

        // modify both axes
        ChartAxisLimits axisLimits = calcAxisLimits(lineDataSet);
        modifyAxes(lineChart, axisLimits);

        // add line data to the chart
        lineChart.setData(createLineData(lineDataSet));
        refreshChart(lineChart);
    }

    private int roundToNextFive(float value) {
        return ((int)value/5)*5+5;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_charts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_charts_filter:
                openFilterActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFilterActivity() {
        Intent intent = new Intent(getContext(), FilterActivity.class);
        intent.putExtra(EXTRA_REQUEST_CODE_NAME, EDIT_FILTER_STRING_REQUEST_CODE);
        startActivityForResult(intent, EDIT_FILTER_STRING_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_FILTER_STRING_REQUEST_CODE) {

            // handle the return value from the FilterActivity
            handleFilterActivityResult(resultCode, data);

            if (resultCode == DatabaseFragment.FILTER_ACTIVITY_OK_RESULT_CODE) {
                // update the new filter string in the view model
                viewModel.setDateFilter(viewModel.loadDateFilter());
            }
        }
    }

    private void initObservers() {
        if (getActivity() ==  null)
            Log.e(LOG_TAG,"initObservers(): observers cannot be initialized, because getActivity = null");
        else {
            viewModel.getFilteredDailyBalance().observe(getActivity(), dailyBalanceList -> {
                List<Entry> entries = viewModel.getDataEggsCollected(dailyBalanceList);
                LineDataSet lineDataSet = createLineDataSet(entries, NAME_EGGS_COLLECTED);
                updateChart(lineChart, lineDataSet);
            });
        }
    }

    /**
     * Class for formatting the x axis labels
     */
    private class AxisDateFormatterWithYear extends ValueFormatter {

        private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            Calendar cal = viewModel.getReferenceDate();
            cal.add(Calendar.DAY_OF_MONTH,(int)value);
            return sdf.format(cal.getTimeInMillis());
        }
    }

    private class AxisDateFormatterWithOutYear extends ValueFormatter {

        private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            Calendar cal = viewModel.getReferenceDate();
            cal.add(Calendar.DAY_OF_MONTH,(int)value);
            return sdf.format(cal.getTimeInMillis());
        }
    }
}
