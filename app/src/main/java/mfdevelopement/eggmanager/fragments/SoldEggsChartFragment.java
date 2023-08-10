package mfdevelopement.eggmanager.fragments;

import static mfdevelopement.eggmanager.fragments.DatabaseFragment.EXTRA_REQUEST_CODE_NAME;

import android.app.Activity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.charts.ChartStyle;
import mfdevelopement.eggmanager.charts.DataSetUtils;
import mfdevelopement.eggmanager.charts.IGenericChart;
import mfdevelopement.eggmanager.charts.MyBarChart;
import mfdevelopement.eggmanager.charts.MyLineChart;
import mfdevelopement.eggmanager.charts.axis_formatters.AxisDateFormat;
import mfdevelopement.eggmanager.charts.axis_formatters.ChartAxisFormatterFactory;
import mfdevelopement.eggmanager.data_models.TextWithIconItem;
import mfdevelopement.eggmanager.dialog_fragments.ChartStyleDialogFragment;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class SoldEggsChartFragment extends Fragment {

    /**
     * String for logging
     */
    private static final String LOG_TAG = "SoldEggsChartFragment";
    /**
     * ActivityResultLauncher to start another activity
     * call with mStartActivityWithIntent.launch(new Intent())
     */
    private final ActivityResultLauncher<Intent> mStartActivityWithIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(LOG_TAG, "received RESULT_OK as activity result");
                    //Intent intent = result.getData();
                    // Handle the Intent
                    // nothing to handle yet
                }
            });
    private SharedViewModel viewModel;
    private IGenericChart genericChart;
    private View rootView;
    private ViewGroup mainView;
    private TextView txtv_title_extra;
    private int databaseEntryCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.content_chart, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // this fragment has its own options menu
        setHasOptionsMenu(true);

        // get references to GUI elements
        TextView txtv_title = rootView.findViewById(R.id.txtv_chart_title);
        txtv_title.setText(getString(R.string.txt_title_eggs_sold));
        txtv_title_extra = rootView.findViewById(R.id.txtv_chart_title_extra);
        txtv_title_extra.setText("");
        txtv_title_extra.setVisibility(View.GONE);

        // Create the chart
        mainView = rootView.findViewById(R.id.main_chart_container);
        MyBarChart barChart = new MyBarChart(this.getContext());
        addChartView(mainView, barChart, txtv_title_extra);
        genericChart = barChart;

        // Change text size of the no-data text
        Paint pLine = barChart.getPaint(Chart.PAINT_INFO);
        pLine.setTextSize(getResources().getDimension(R.dimen.textAppearanceSubtitle2));


        if (this.getContext() == null) {
            Log.e(LOG_TAG, "onCreateView: this.getContext() returned null");
            return null;
        }

        // init observers
        initObservers();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The usage of an interface lets you inject your own implementation
        MenuHost menuHost = requireActivity();

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_charts, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (databaseEntryCount <= 0) {
                    Snackbar.make(rootView.findViewById(R.id.main_chart_container), getString(R.string.snackbar_no_data_to_filter), Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (menuItem.getItemId() == R.id.menu_date_filter) {// Show the filter activity if there's some data in the database
                    openFilterActivity();
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_charts_style) {
                    showChartStyleDialog();
                    return true;
                }

                // Return false if the selection has not been handled
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private <T extends ChartData<? extends IDataSet<? extends Entry>>> void addChartView(ViewGroup parent, Chart<T> chart, View viewAbove) {
        // Create the layout params
        // The view will be at the bottom of the other views
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topToBottom = viewAbove.getId();
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = 0;
        layoutParams.topMargin = (int) getResources().getDimension(R.dimen.small_padding);
        layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.small_padding);

        // set the layout params
        chart.setLayoutParams(layoutParams);

        // add the chart to the parent view
        parent.addView(chart);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        viewModel.setDateFilter(viewModel.loadDateFilter());
    }

    private void showChartStyleDialog() {
        List<TextWithIconItem> items = new ArrayList<>();
        List<ChartStyle> chartStyleList = new ArrayList<>();
        items.add(new TextWithIconItem(getString(R.string.chart_style_bar), R.drawable.ic_baseline_bar_chart_24));
        chartStyleList.add(ChartStyle.BAR);
        items.add(new TextWithIconItem(getString(R.string.chart_style_line), R.drawable.ic_baseline_line_chart_24));
        chartStyleList.add(ChartStyle.LINE);
        ChartStyleDialogFragment fragment = new ChartStyleDialogFragment(items);

        int initialPositionSelected = -1;
        if (genericChart instanceof MyBarChart) {
            initialPositionSelected = 0;
        } else if (genericChart instanceof MyLineChart) {
            initialPositionSelected = 1;
        }
        fragment.setItemSelected(initialPositionSelected);

        int finalInitialPositionSelected = initialPositionSelected;
        fragment.setOnItemClickListener((textWithIconItem, position) -> {
            Log.d(LOG_TAG, "user clicked on item at position " + position);
            fragment.setItemSelected(position);
            fragment.dismiss();
            if (position != finalInitialPositionSelected) {
                changeChartStyle(chartStyleList.get(position));
            }
        });

        // FragmentActivity.getSupportFragmentManager()
        final String fragmentTag = "ChartStyleDialogFragment";
        if (getActivity() == null) {
            Log.e(LOG_TAG, "showChartStyleDialog(): getActivity() == null");
            return;
        }

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        fragment.show(ft, fragmentTag);
    }

    private void removeChart() {
        if (genericChart == null) return;
        if (genericChart instanceof MyBarChart) {
            mainView.removeView((MyBarChart) genericChart);
        } else if (genericChart instanceof MyLineChart) {
            mainView.removeView((MyLineChart) genericChart);
        }
    }

    private void changeChartStyle(ChartStyle chartStyle) {
        // remove the current chart
        removeChart();

        IDataSet<Entry> dataSet = genericChart.getChartData();

        if (chartStyle == ChartStyle.BAR) {
            MyBarChart barChart = new MyBarChart(this.getContext());
            addChartView(mainView, barChart, txtv_title_extra);
            genericChart = barChart;
            BarDataSet barDataSet = DataSetUtils.convertToBarDataSet(this.getContext(), dataSet);
            genericChart.setChartData(this.getContext(), barDataSet);
        } else if (chartStyle == ChartStyle.LINE) {
            MyLineChart lineChart = new MyLineChart(this.getContext());
            addChartView(mainView, lineChart, txtv_title_extra);
            genericChart = lineChart;
            LineDataSet lineDataSet = DataSetUtils.convertToLineDataSet(this.getContext(), dataSet);
            genericChart.setChartData(this.getContext(), lineDataSet);
        } else {
            return;
        }

        genericChart.setXAxisValueFormatter(ChartAxisFormatterFactory.getInstance(AxisDateFormat.MONTH_YEAR));
        //genericChart.setChartData(dataSet);
    }

    private void openFilterActivity() {
        Intent intent = new Intent(getContext(), FilterActivity.class);
        intent.putExtra(EXTRA_REQUEST_CODE_NAME, IntentCodes.Request.EDIT_FILTER.ordinal());

        mStartActivityWithIntent.launch(intent);
    }

    private void initObservers() {
        if (getActivity() == null || this.getContext() == null)
            Log.e(LOG_TAG, "initObservers(): observers cannot be initialized, because getActivity() returned null");
        else {
            viewModel.getFilteredDailyBalance().observe(getViewLifecycleOwner(), dailyBalanceList -> {

                if (dailyBalanceList != null && !dailyBalanceList.isEmpty()) {

                    List<Entry> entries = viewModel.getDataEggsSold(dailyBalanceList);

                    // create a new BarDataSet containing the received data
                    BarDataSet barDataSet = DataSetUtils.createBarDataSet(this.getContext(), entries, getString(R.string.txt_title_eggs_sold));

                    // show chart (to make it visible if it's hidden)
                    genericChart.showChart();

                    // update the content of the chart
                    barDataSet.setColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
                    genericChart.setChartData(this.getContext(), barDataSet);
                } else {
                    Log.d(LOG_TAG, "initObservers(): The received daily balance list is null or empty");
                    // Hide the charts as there's nothing to show in the chart
                    genericChart.hideChart();
                }
            });

            viewModel.getEntriesCount().observe(getViewLifecycleOwner(), count -> {
                if (count != null) {
                    databaseEntryCount = count;
                }
            });
        }
    }
}
