package mfdevelopement.eggmanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.list_adapters.DateFilterListAdapter;
import mfdevelopement.eggmanager.viewmodels.FilterActivityViewModel;

public class FilterActivity extends AppCompatActivity implements DateFilterListAdapter.OnButtonClickListener {

    private final String LOG_TAG = "FilterActivity";
    private DateFilterListAdapter adapterYears, adapterMonths;

    private FilterActivityViewModel viewModel;

    private LinearLayout linLayMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);
        //setupEnterFadeAnimation();

        Log.d(LOG_TAG,"starting onCreate()");

        // get reference to the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_filter_activity);
        setSupportActionBar(toolbar);

        // add a button for moving up to the calling activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get the view model
        viewModel = new ViewModelProvider(this).get(FilterActivityViewModel.class);

        // initialize the oberservers for handling LiveData
        initObservers();

        // initialize the GUI
        initGUI();

        Log.d(LOG_TAG,"finished onCreate()");
    }

    private void setupEnterFadeAnimation() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

    private void initGUI() {
        initRecyclerViewYears();
        initRecyclerViewMonths();
    }

    private void initRecyclerViewYears() {
        Log.d(LOG_TAG,"starting initRecyclerViewYears()");
        RecyclerView recyclerViewYears = findViewById(R.id.filter_activity_recycler_view_years);
        recyclerViewYears.setLayoutManager(new GridLayoutManager(this,2));

        // check if the initial filter string is valid
        String initialFilterString = viewModel.getFilterString();
        Log.d(LOG_TAG,"initial filter key: " + initialFilterString);
        if (initialFilterString == null) {
            Log.e(LOG_TAG,"cannot initialize GUI because initialFilterString is NULL");
            return;
        }

        List<String> yearsList = viewModel.getYearNames();

        adapterYears = new DateFilterListAdapter(this, yearsList, DailyBalance.getYearByDateKey(initialFilterString));
        recyclerViewYears.setAdapter(adapterYears);
    }

    private void initRecyclerViewMonths() {
        Log.d(LOG_TAG,"starting initRecyclerViewMonths()");
        RecyclerView recyclerViewMonths = findViewById(R.id.filter_activity_recycler_view_months);
        recyclerViewMonths.setLayoutManager(new GridLayoutManager(this,2));

        // load the last saved filter string
        String initialFilterString = viewModel.getFilterString();
        Log.d(LOG_TAG,"initial filter key: " + initialFilterString);
        if (initialFilterString == null) {
            Log.e(LOG_TAG,"cannot initialize GUI because initialFilterString is NULL");
            return;
        }

        // add an empty adapter to the recycler view
        // set the initial month name from the loaded filter string, if the filter string contains information about the month
        String initialMonthName = "";
        if (!initialFilterString.isEmpty() && initialFilterString.length() >= 6) {
            int indexMonth = Integer.parseInt(DailyBalance.getMonthByDateKey(initialFilterString));
            initialMonthName = viewModel.getMonthNameByIndex(indexMonth);
        }
        adapterMonths = new DateFilterListAdapter(this, new ArrayList<>(), initialMonthName);
        recyclerViewMonths.setAdapter(adapterMonths);

        // change visibility depending on the initial filter string
        linLayMonths = findViewById(R.id.linLay_filter_activity_months);
        if (initialFilterString.isEmpty() || initialFilterString.length() < 4) {
            linLayMonths.setVisibility(View.GONE);
            Log.d(LOG_TAG,"hiding recycler view containing month names, because initialFilterString is empty or has less than 4 characters");
            return;
        } else {
            linLayMonths.setVisibility(View.VISIBLE);
        }

        Log.d(LOG_TAG,"initialFilterString = \"" + initialFilterString + "\"");
        String initialYear = DailyBalance.getYearByDateKey(initialFilterString);
        Log.d(LOG_TAG,"year of initialFilterString = \"" + initialYear + "\"");
        List<String> monthNames = viewModel.getMonthsByYear(initialYear);
        Log.d(LOG_TAG,"initial months for initialFilterString: " + monthNames.size() + " items");

        // add adapter to the recycler view
        adapterMonths.setDatesList(monthNames, false);
    }

    private void initObservers() {
        Log.d(LOG_TAG,"initializing observers");
        viewModel.getAllDateKeys().observe(this, stringList -> {
            List<String> yearNamesList = new ArrayList<>();
            for (String dateString : stringList) {
                yearNamesList.add(dateString.substring(0,4));
            }
            viewModel.setYearNames(yearNamesList);
            viewModel.setYearMonthNames(stringList);
            adapterYears.setDatesList(viewModel.getYearNames(), false);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_apply:
                String newFiterString = parseSelectedFilter();

                // apply the filter string to the view model
                viewModel.setFilterString(newFiterString);

                // finish the activity with a result code
                endActivity(DatabaseFragment.FILTER_ACTIVITY_OK_RESULT_CODE, new Intent().setData(Uri.parse(newFiterString)));
                break;
            case android.R.id.home:
                // action when clicking on the home up button
                endActivity(DatabaseFragment.FILTER_ACTIVITY_CANCEL_RESULT_CODE, null);
                break;
        }
        return true;
        //return super.onOptionsItemSelected(item);
    }

    private void endActivity(int resultCode, @Nullable Intent data) {
        if (data != null)
            setResult(resultCode, data);
        else
            setResult(resultCode);
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        endActivity(DatabaseFragment.FILTER_ACTIVITY_CANCEL_RESULT_CODE, null);
    }

    private String parseSelectedFilter() {
        String yearsSelection = adapterYears.getCurrentSelection();
        String monthSelection = adapterMonths.getCurrentSelection();
        Log.d(LOG_TAG,"user wants to apply a filter: \"" + yearsSelection + "\" and \"" + monthSelection + "\"");

        String filterString = yearsSelection;

        // add optional month index to the filter string
        int monthIndex = viewModel.getMonthIndexByName(monthSelection);
        if (monthIndex != 0) {
            filterString = filterString + String.format(Locale.getDefault(),"%02d", monthIndex);
        }

        return filterString;
    }

    @Override
    public void OnButtonClicked(String buttonText, boolean isSelected) {
        Log.d(LOG_TAG,"user clicked on button with text " + buttonText + ". Button is selected: " + isSelected);
        if (buttonText.length() == 4) {
            Log.d(LOG_TAG,"user selected a button containing a year");

            // if the user selected the button, then update the months list
            // if the user unselected the button, then remove all items from the recycler view and hide the months list
            if (isSelected) {
                linLayMonths.setVisibility(View.VISIBLE);
                adapterMonths.setDatesList(viewModel.getMonthsByYear(buttonText), true);
            } else {
                linLayMonths.setVisibility(View.GONE);
                adapterMonths.setDatesList(new ArrayList<String>(), true);
            }
        }
    }
}