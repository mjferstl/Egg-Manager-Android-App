package mfdevelopement.eggmanager.fragments;

import android.content.Context;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.charts.DataSetUtils;
import mfdevelopement.eggmanager.charts.IGenericChart;
import mfdevelopement.eggmanager.charts.MyLineChart;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

import static mfdevelopement.eggmanager.fragments.DatabaseFragment.EXTRA_REQUEST_CODE_NAME;
import static mfdevelopement.eggmanager.utils.FilterActivityResultHandler.handleFilterActivityResult;

public class CollectedEggsChartFragment extends Fragment {

    private static final String LOG_TAG = "CollectedEggsChartFragm";

    private final ActivityResultContract<Integer, Integer> mContract = new ActivityResultContract<Integer, Integer>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Integer input) {
            Intent intent = new Intent(getContext(), FilterActivity.class);
            intent.putExtra(EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.EDIT_FILTER.ordinal());
            return intent;
        }

        @Override
        public Integer parseResult(int resultCode, @Nullable Intent intent) {
            Log.d(LOG_TAG, "parseResult(): resultCode = " + resultCode);
            // handle the return value from the FilterActivity
            handleFilterActivityResult(resultCode, intent);
            return resultCode;
        }
    };
    private SharedViewModel viewModel;
    private final ActivityResultLauncher<Integer> mGetContent = registerForActivityResult(mContract,
            new ActivityResultCallback<Integer>() {
                @Override
                public void onActivityResult(Integer resultCode) {
                    Log.d(LOG_TAG, "registerForActivityResult(): resultCode = " + resultCode);

                    if (resultCode == DatabaseActions.Result.FILTER_OK.ordinal()) {
                        // update the new filter string in the view model
                        viewModel.setDateFilter(viewModel.loadDateFilter());
                    }
                }
            });
    private IGenericChart genericChart;
    private View rootView;
    private int databaseEntryCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.content_chart, container, false);

        // get reference to view model
        viewModel = new ViewModelProvider(this.getActivity()).get(SharedViewModel.class);

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // this fragment has its own options menu
        setHasOptionsMenu(true);

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_charts, menu);
        MenuItem item = menu.findItem(R.id.menu_charts_style);
        item.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_date_filter) {// Show the filter activity if there's some data in the database
            if (databaseEntryCount > 0) {
                openFilterActivity();
            } else {
                Snackbar.make(rootView.findViewById(R.id.main_chart_container), getString(R.string.snackbar_no_data_to_filter), Snackbar.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFilterActivity() {
        mGetContent.launch(DatabaseActions.Request.EDIT_FILTER.ordinal());
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
