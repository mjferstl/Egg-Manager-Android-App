package mfdevelopement.eggmanager.fragments;

import android.content.Intent;
import android.graphics.Paint;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.snackbar.Snackbar;

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
    private BarChart barChart;
    private TextView txtv_title, txtv_title_extra;
    private View rootView;

    public final static String NAME_EGGS_COLLECTED = "Abgenommene Eier";
    public final static String NAME_EGGS_SOLD = "Verkaufte Eier";
    public final static String ARG_DATA = "chartData";

    private int chartSwitch = 1; // inital view is "Abgenommene Eier"
    private final int chartSwitchCollectedEggs = 1;
    private final int chartSwitchSoldEggs = 2;
    private int databaseEntryCount = 0;

    private String chartTitle = "Abgenommene Eier";



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            String extraArgument = args.getString(ARG_DATA);
            Log.d(LOG_TAG, "starting onCreateView() with extra argument \"" + extraArgument + "\"");
            if (extraArgument != null) {
                if (extraArgument.equals(NAME_EGGS_COLLECTED))
                    chartSwitch = chartSwitchCollectedEggs;
                else if (extraArgument.equals(NAME_EGGS_SOLD))
                    chartSwitch = chartSwitchSoldEggs;
            }
            setChartVariables();
        }

        rootView = inflater.inflate(R.layout.content_chart, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // this framgent has its own options menu
        setHasOptionsMenu(true);

        // get references to GUI elements
        txtv_title = rootView.findViewById(R.id.txtv_chart_title);
        txtv_title.setText(chartTitle);
        txtv_title_extra = rootView.findViewById(R.id.txtv_chart_title_extra);
        txtv_title_extra.setText("");
        txtv_title_extra.setVisibility(View.GONE);

        // line chart
        lineChart = rootView.findViewById(R.id.line_chart);
        // modify chart layout
        lineChart.setBackgroundColor(getResources().getColor(R.color.transparent));     // transparent background
        lineChart.setBorderColor(getResources().getColor(R.color.main_text_color));     // color of the chart borders
        lineChart.getLegend().setEnabled(false);                                        // hide the legend
        lineChart.setNoDataText(getString(R.string.chart_no_data_text));
        // Change text size of the no-data text
        Paint pLine = lineChart.getPaint(Chart.PAINT_INFO);
        pLine.setTextSize(getResources().getDimension(R.dimen.textAppearanceSubtitle2));

        // Description
        Description description = lineChart.getDescription();   // get description
        description.setText("");                          // text for description
        description.setEnabled(false);                      // hide description

        // bar chart
        barChart = rootView.findViewById(R.id.bar_chart);
        barChart.setBackgroundColor(getResources().getColor(R.color.transparent));     // transparent background
        barChart.getLegend().setEnabled(false);                                        // hide the legend
        barChart.setNoDataText(getString(R.string.chart_no_data_text));
        // Change text size of the no-data text
        Paint pBar = barChart.getPaint(Chart.PAINT_INFO);
        pBar.setTextSize(getResources().getDimension(R.dimen.textAppearanceSubtitle2));

        // Description
        Description barChartDescription = barChart.getDescription();
        barChartDescription.setText("");
        barChartDescription.setEnabled(false);

        // init observers
        initObservers();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG,"onResume()");
        viewModel.setDateFilter(viewModel.loadDateFilter());
    }

    private void modifyAxes(Chart chart, float xMin, float xMax, float yMin, float yMax) {
        modifyXAxis(chart, xMin, xMax);
        modifyYAxis(chart, yMin, yMax);
    }

    private void modifyAxes(Chart lineChart, ChartAxisLimits chartAxisLimits) {
        modifyAxes(lineChart, chartAxisLimits.getxMin(), chartAxisLimits.getxMax(), chartAxisLimits.getyMin(), chartAxisLimits.getyMax());
    }

    private ChartAxisLimits calcAxisLimits(LineDataSet lineDataSet) {
        List<Entry> entriesList = getEntriesOfLineDataSet(lineDataSet);

        // modify both axes
        float xMin = getMinX(entriesList);
        float xMax = getMaxX(entriesList);
        float yMin = 0f;
        float yMax;

        float maxYValue = getMaxY(entriesList);

        if (maxYValue < 50) {
            yMax = roundToNextNumber(maxYValue,5)+5;
        } else if (maxYValue < 100) {
            yMax = roundToNextNumber(maxYValue, 10)+10;
        } else if (maxYValue < 1000) {
            yMax = roundToNextNumber(maxYValue, 100)+100;
        } else {
            yMax = roundToNextNumber(maxYValue, 1000)+1000;
        }

        return new ChartAxisLimits(xMin, xMax, yMin, yMax);
    }

    private ChartAxisLimits calcAxisLimits(BarDataSet barDataSet) {
        List<Entry> entriesList = getEntriesOfLineDataSet(barDataSet);

        // modify both axes
        float xMin = getMinX(entriesList)-1;
        float xMax = getMaxX(entriesList)+1;
        float yMin = 0f;
        float yMax;

        float maxYValue = getMaxY(entriesList);

        if (maxYValue < 50) {
            yMax = roundToNextNumber(maxYValue,5)+5;
        } else if (maxYValue < 100) {
            yMax = roundToNextNumber(maxYValue, 10)+10;
        } else if (maxYValue < 1000) {
            yMax = roundToNextNumber(maxYValue, 100)+100;
        } else {
            yMax = roundToNextNumber(maxYValue, 1000)+1000;
        }

        return new ChartAxisLimits(xMin, xMax, yMin, yMax);
    }

    private void modifyXAxis(Chart chart, float min, float max) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        // X-Axis Style
        XAxis xAxis;
        xAxis = chart.getXAxis();                       // get the x axis
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // axis at the bottom of the diagram
        xAxis.setGranularity(GRANULARITY_DAY);          // minimum difference between axis labels
        xAxis.setTextSize(TEXT_SIZE);                   // text size

        if (this.getContext() != null) {
            xAxis.setTextColor(getResources().getColor(R.color.main_text_color)); // text color
        }

        // get the first and the last day of the x data
        Calendar startDate = viewModel.getReferenceDate();
        Log.d(LOG_TAG,"modifyXAxis(): startDate = " + sdf.format(startDate.getTimeInMillis()));
        startDate.add(Calendar.DAY_OF_MONTH,(int)min);
        Log.d(LOG_TAG,"modifyXAxis(): updated startDate = " + sdf.format(startDate.getTimeInMillis()));
        Calendar endDate = viewModel.getReferenceDate();
        endDate.add(Calendar.DAY_OF_MONTH,(int)max);

        // If all dates are of the same year, then no year information is shown in the x labels
        if (chartSwitch == chartSwitchSoldEggs) {
            xAxis.setValueFormatter(new AxisMonthYearFormatter());
        } else {
            String extraTitle = "";
            if (startDate.get(Calendar.YEAR) == endDate.get(Calendar.YEAR)) {
                xAxis.setValueFormatter(new AxisDateFormatterWithOutYear());

                if (startDate.get(Calendar.MONTH) == endDate.get(Calendar.MONTH)) {
                    String monthName = viewModel.getMonthNameByIndex(startDate.get(Calendar.MONTH) + 1);
                    extraTitle = String.format(Locale.getDefault(), "\nim %s %d", monthName, startDate.get(Calendar.YEAR));
                } else {
                    extraTitle = String.format(Locale.getDefault(), "\nim Jahr %d", startDate.get(Calendar.YEAR));
                }
            } else {
                xAxis.setValueFormatter(new AxisDateFormatterWithYear());
            }

            txtv_title_extra.setText(extraTitle);
        }

        // set minimum and maximum value of the axis
        xAxis.setAxisMinimum(min);
        xAxis.setAxisMaximum(max);
    }

    private void modifyYAxis(Chart chart, float min, float max) {

        YAxis yAxis;

        if (chartSwitch == chartSwitchSoldEggs) {
            BarChart barChart = (BarChart) chart;
            yAxis = barChart.getAxisLeft();
            barChart.getAxisRight().setEnabled(false);
        } else {
            LineChart lineChart = (LineChart) chart;
            yAxis = lineChart.getAxisLeft();                    // get the y axis
            lineChart.getAxisRight().setEnabled(false);         // disable dual axis (only use LEFT axis)
        }

        yAxis.setAxisMaximum(max);
        yAxis.setAxisMinimum(min);
        yAxis.setGranularity(1f);
        yAxis.setTextSize(TEXT_SIZE);
        yAxis.setCenterAxisLabels(true);
        yAxis.setDrawLabels(true);
        yAxis.setDrawGridLinesBehindData(true);

        if (this.getContext() != null) {
            yAxis.setTextColor(getResources().getColor(R.color.main_text_color));
        }

        if (max/5 < 8) {
            yAxis.setLabelCount((int)max/5);
        } else {
            yAxis.setLabelCount(8);
        }

        Log.d(LOG_TAG,"yAxis max value: " + yAxis.getAxisMaximum());
        Log.d(LOG_TAG,"yAxis label count: " + yAxis.getLabelCount());
    }

    private LineDataSet createLineDataSet(List<Entry> entries, String dataSetName) {
        LineDataSet dataSet = new LineDataSet(entries, dataSetName);

        // sometimes there is no context and the resources cannot be fetched
        // but then a NullPointerException is thrown
        if (this.getContext() != null) {
            dataSet.setColor(getResources().getColor(R.color.colorPrimary));
            dataSet.setValueTextColor(getResources().getColor(R.color.main_text_color));
        }

        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(DATA_LINE_WIDTH);
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        if (entries.size() > 5) {
            dataSet.setDrawValues(false);                   // do not show the value of each data point
        } else {
            dataSet.setDrawValues(true);
        }

        return dataSet;
    }

    private BarDataSet createBarDataSet(List<BarEntry> entries, String dataSetName) {
        BarDataSet dataSet = new BarDataSet(entries, dataSetName);
        dataSet.setColor(getResources().getColor(R.color.colorAccent));
        dataSet.setValueTextColor(getResources().getColor(R.color.main_text_color));
        dataSet.setValueTextSize(TEXT_SIZE);            // text size of the text for each data point
        if (entries.size() > 5) {
            dataSet.setDrawValues(false);                   // do not show the value of each data point
        } else {
            dataSet.setDrawValues(true);
        }
        return dataSet;
    }

    private List<Entry> getEntriesOfLineDataSet(LineDataSet dataSet) {
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<dataSet.getEntryCount(); i++)
            entries.add(dataSet.getEntryForIndex(i));
        return entries;
    }

    private List<Entry> getEntriesOfLineDataSet(BarDataSet dataSet) {
        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<dataSet.getEntryCount(); i++)
            entries.add(dataSet.getEntryForIndex(i));
        return entries;
    }

    private float getMinX(List<Entry> entryList) {
        return getMin(getXValues(entryList));
    }

    private float getMaxX(List<Entry> entryList) {
        return getMax(getXValues(entryList));
    }

    private float getMinY(List<Entry> entryList) {
        return getMin(getYValues(entryList));
    }

    private float getMaxY(List<Entry> entryList) {
        return getMax(getYValues(entryList));
    }

    private List<Float> getXValues(List<Entry> entryList) {
        List<Float> xValues = new ArrayList<>();
        for (Entry entry : entryList)
            xValues.add(entry.getX());
        return xValues;
    }

    private List<Float> getYValues(List<Entry> entryList) {
        List<Float> yValues = new ArrayList<>();
        for (Entry entry : entryList)
            yValues.add(entry.getY());
        return yValues;
    }

    private float getMax(List<Float> values) {
        float maxValue = values.get(0);
        for (float f: values) {
            if (f > maxValue) {
                maxValue = f;
            }
        }
        return maxValue;
    }

    private float getMin(List<Float> values) {
        float minValue = values.get(0);
        for (float f: values) {
            if (f < minValue) {
                minValue = f;
            }
        }
        return minValue;
    }

    private LineData createLineData(LineDataSet lineDataSet) {
        // line data - containing all data for a chart
        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        return lineData;
    }

    private BarData createBarData(BarDataSet barDataSet) {
        BarData barData = new BarData();
        barData.addDataSet(barDataSet);
        return barData;
    }

    private void refreshChart(Chart chart) {
        chart.invalidate();
    }

    private void updateLineChart(LineChart lineChart, LineDataSet lineDataSet) {

        barChart.setVisibility(View.GONE);
        this.lineChart.setVisibility(View.VISIBLE);

        // modify both axes
        ChartAxisLimits axisLimits = calcAxisLimits(lineDataSet);
        modifyAxes(lineChart, axisLimits);

        // add line data to the chart
        lineChart.setData(createLineData(lineDataSet));
        refreshChart(lineChart);
    }

    private void updateBarChart(BarChart barChart, BarDataSet barDataSet) {

        this.barChart.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.GONE);

        ChartAxisLimits axisLimits = calcAxisLimits(barDataSet);
        Log.d(LOG_TAG,"axis limits: xMin: " + axisLimits.getxMin() + ", xMax: " + axisLimits.getxMax() + ", yMin: " + axisLimits.getyMin() + ", yMax: " + axisLimits.getyMax());
        modifyAxes(barChart, axisLimits);

        // add line data to the chart
        barChart.setData(createBarData(barDataSet));
        refreshChart(barChart);
    }

    private void hideChart(Chart chart) {
        Log.d(LOG_TAG,"hiding chart");
        chart.setVisibility(View.GONE);
    }

    private void showChart(Chart chart) {
        chart.setVisibility(View.VISIBLE);
    }

    private int roundToNextNumber(float value, int stepsize) {
        int roundedValue = ((int)value/stepsize)*stepsize+stepsize;
        Log.d(LOG_TAG,"rounding " + value + " to " + roundedValue);
        return roundedValue;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_charts, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_charts_filter:
                // Show the filter activity if there's some data in the database
                if (databaseEntryCount > 0) {
                    openFilterActivity();
                } else {
                    Snackbar.make(rootView.findViewById(R.id.main_chart_container), getString(R.string.snackbar_no_data_to_filter), Snackbar.LENGTH_SHORT).show();
                }
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

            if (resultCode == DatabaseFragment.FILTER_OK_RESULT_CODE) {
                // update the new filter string in the view model
                viewModel.setDateFilter(viewModel.loadDateFilter());
            }
        }
    }

    private void setChartVariables() {
        if (chartSwitch == chartSwitchCollectedEggs) {
            chartTitle = getString(R.string.txt_title_eggs_collected);
        }
        else if (chartSwitch == chartSwitchSoldEggs) {
            chartTitle = "Verkaufte Eier";
        }
    }

    private void initObservers() {
        if (getActivity() ==  null)
            Log.e(LOG_TAG,"initObservers(): observers cannot be initialized, because getActivity = null");
        else {
            viewModel.getFilteredDailyBalance().observe(getActivity(), dailyBalanceList -> {

                if (dailyBalanceList != null && !dailyBalanceList.isEmpty()) {
                    if (chartSwitch == chartSwitchCollectedEggs) {
                        List<Entry> entries = viewModel.getDataEggsCollected(dailyBalanceList);

                        // create a new LineDataSet containing the received data
                        LineDataSet lineDataSet = createLineDataSet(entries, NAME_EGGS_COLLECTED);

                        // show chart (to make it visible if it's hidden)
                        showChart(lineChart);

                        // update the content of the chart
                        updateLineChart(lineChart, lineDataSet);

                    } else if (chartSwitch == chartSwitchSoldEggs) {
                        List<BarEntry> entries = viewModel.getDataEggsSold(dailyBalanceList);

                        // create a new BarDataSet containing the received data
                        BarDataSet barDataSet = createBarDataSet(entries, NAME_EGGS_SOLD);

                        // show chart (to make it visible if it's hidden)
                        showChart(barChart);

                        // update the content of the chart
                        updateBarChart(barChart, barDataSet);
                    }
                } else {
                    // Hide the charts as there's nothing to show in the chart
                    if (chartSwitch == chartSwitchCollectedEggs) {
                        hideChart(lineChart);
                    } else {
                        hideChart(barChart);
                    }
                }
            });

            viewModel.getEntriesCount().observe(getViewLifecycleOwner(), count -> {
                if (count != null) {
                    databaseEntryCount = count;
                }
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

    private class AxisMonthYearFormatter extends ValueFormatter {

        private SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            Calendar cal = viewModel.getReferenceDate();
            cal.add(Calendar.MONTH,(int)value-1);
            return sdf.format(cal.getTimeInMillis());
        }
    }
}
