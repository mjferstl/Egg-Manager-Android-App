package mfdevelopement.eggmanager.fragments;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activity_contracts.OpenFilterActivityContract;
import mfdevelopement.eggmanager.charts.DataSetUtils;
import mfdevelopement.eggmanager.charts.IGenericChart;
import mfdevelopement.eggmanager.charts.MyLineChart;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class CollectedEggsChartFragment extends Fragment {

    /**
     * String used as identifier for logging
     */
    private static final String LOG_TAG = "CollectedEggsChartFragm";

    /**
     * {@link androidx.lifecycle.ViewModel} used in this fragment
     */
    private SharedViewModel viewModel;
    private final ActivityResultLauncher<Long> showFilterActivity = registerForActivityResult(new OpenFilterActivityContract(), resultCode -> {

        Log.d(LOG_TAG, "registerForActivityResult(): resultCode = " + resultCode);

        if (resultCode == IntentCodes.Result.FILTER_OK.id) {
            // update the new filter string in the view model
            viewModel.setDateFilter(viewModel.loadDateFilter());
        }
    });

    private IGenericChart genericChart;

    /**
     * Root view of the activity
     * Needed for Snackbars
     */
    private View rootView;

    /**
     * Variable to store the number of elements, which are received from the database
     */
    private int databaseEntryCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.content_chart, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // get references to GUI elements
        TextView txtv_title = rootView.findViewById(R.id.txtv_chart_title);
        txtv_title.setText(getString(R.string.txt_title_eggs_collected));
        TextView txtv_title_extra = rootView.findViewById(R.id.txtv_chart_title_extra);
        txtv_title_extra.setText("");
        txtv_title_extra.setVisibility(View.GONE);

        // Constraint Layout
        ConstraintLayout constraintLayout = rootView.findViewById(R.id.main_chart_container);

        // Create the line chart
        MyLineChart lineChart = new MyLineChart(this.getContext());
        addChartView(constraintLayout, lineChart, txtv_title_extra);
        genericChart = lineChart;

        // Change text size of the no-data text
        Paint pLine = lineChart.getPaint(Chart.PAINT_INFO);
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
                MenuItem item = menu.findItem(R.id.menu_charts_style);
                item.setVisible(false);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_date_filter) {// Show the filter activity if there's some data in the database
                    if (databaseEntryCount > 0) {
                        openFilterActivity();
                    } else {
                        Snackbar.make(rootView.findViewById(R.id.main_chart_container), getString(R.string.snackbar_no_data_to_filter), Snackbar.LENGTH_SHORT).show();
                    }
                }

                // Return false if the selection has not been handled
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    /**
     * Add a {@link Chart} to a view
     *
     * @param parent    parent view to add the chart to
     * @param chart     {@link Chart} to add
     * @param viewAbove {@link View}, under which the {@link Chart} will be placed
     * @param <T>       Type of the Chart
     */
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

    private void openFilterActivity() {
        showFilterActivity.launch(IntentCodes.Request.EDIT_FILTER.id);
    }

    private void initObservers() {
        if (getActivity() == null)
            Log.e(LOG_TAG, "initObservers(): observers cannot be initialized, because getActivity = null");
        else {
            viewModel.getFilteredDailyBalance().observe(getViewLifecycleOwner(), dailyBalanceList -> {

                if (dailyBalanceList != null && !dailyBalanceList.isEmpty()) {
                    List<Entry> entries = viewModel.getDataEggsCollected(dailyBalanceList);

                    // create a new LineDataSet containing the received data
                    LineDataSet lineDataSet = DataSetUtils.createLineDataSet(this.getContext(), entries, getString(R.string.txt_title_eggs_collected));

                    // show chart (to make it visible if it's hidden)
                    genericChart.showChart();

                    // update the content of the chart
                    genericChart.setChartData(this.getContext(), lineDataSet);
                } else {
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
