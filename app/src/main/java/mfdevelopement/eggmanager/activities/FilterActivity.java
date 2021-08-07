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
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.daily_balance.DateKeyUtils;
import mfdevelopement.eggmanager.list_adapters.DateFilterListAdapter;
import mfdevelopement.eggmanager.viewmodels.FilterActivityViewModel;

public class FilterActivity extends AppCompatActivity implements DateFilterListAdapter.OnButtonClickListener {

    private final String LOG_TAG = "FilterActivity";
    private DateFilterListAdapter adapterYears, adapterMonths;

    private FilterActivityViewModel viewModel;

    private LinearLayout linLayMonths;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);
        //setupEnterFadeAnimation();

        Log.d(LOG_TAG, "starting onCreate()");

        // get reference to the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_filter_activity);
        setSupportActionBar(toolbar);

        // add a button for moving up to the calling activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get the view model
        viewModel = new ViewModelProvider(this).get(FilterActivityViewModel.class);

        // initialize the observers for handling LiveData
        initObservers();

        // initialize the GUI
        initGUI();

        Log.d(LOG_TAG, "finished onCreate()");
    }

    private void setupEnterFadeAnimation() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }

    private void initGUI() {
        progressBar = findViewById(R.id.progress_loading_filter);
        initRecyclerViewYears();
        initRecyclerViewMonths();
    }

    private LinearLayout linLayYears;

    private void initRecyclerViewYears() {
        Log.d(LOG_TAG, "starting initRecyclerViewYears()");
        RecyclerView recyclerViewYears = findViewById(R.id.filter_activity_recycler_view_years);
        recyclerViewYears.setLayoutManager(new GridLayoutManager(this, 2));

        // check if the initial filter string is valid
        String initialFilterString = viewModel.getFilterString();
        Log.d(LOG_TAG, "initial filter key: " + initialFilterString);
        if (initialFilterString == null) {
            Log.e(LOG_TAG, "cannot initialize GUI because initialFilterString is NULL");
            return;
        }

        List<String> yearsList = viewModel.getYearNames();

        adapterYears = new DateFilterListAdapter(this, yearsList, DateKeyUtils.getYearByDateKey(initialFilterString));
        recyclerViewYears.setAdapter(adapterYears);

        // Hide the layout, until the data has been loaded
        linLayYears = findViewById(R.id.linLay_filter_activity_years);
        linLayYears.setVisibility(View.GONE);
    }

    private void initRecyclerViewMonths() {
        Log.d(LOG_TAG, "starting initRecyclerViewMonths()");
        RecyclerView recyclerViewMonths = findViewById(R.id.filter_activity_recycler_view_months);
        recyclerViewMonths.setLayoutManager(new GridLayoutManager(this, 2));

        // load the last saved filter string
        String initialFilterString = viewModel.getFilterString();
        Log.d(LOG_TAG, "initial filter key: " + initialFilterString);
        if (initialFilterString == null) {
            Log.e(LOG_TAG, "cannot initialize GUI because initialFilterString is NULL");
            return;
        }

        // add an empty adapter to the recycler view
        // set the initial month name from the loaded filter string, if the filter string contains information about the month
        String initialMonthName = "";
        if (!initialFilterString.isEmpty() && initialFilterString.length() >= 6) {
            int indexMonth = Integer.parseInt(DateKeyUtils.getMonthByDateKey(initialFilterString));
            initialMonthName = viewModel.getMonthNameByIndex(indexMonth);
        }
        adapterMonths = new DateFilterListAdapter(this, new ArrayList<>(), initialMonthName);
        recyclerViewMonths.setAdapter(adapterMonths);

        // change visibility depending on the initial filter string
        linLayMonths = findViewById(R.id.linLay_filter_activity_months);
        if (initialFilterString.isEmpty() || initialFilterString.length() < 4) {
            linLayMonths.setVisibility(View.GONE);
            Log.d(LOG_TAG, "hiding recycler view containing month names, because initialFilterString is empty or has less than 4 characters");
            return;
        } else {
            linLayMonths.setVisibility(View.VISIBLE);
        }

        Log.d(LOG_TAG, "initialFilterString = \"" + initialFilterString + "\"");
        String initialYear = DateKeyUtils.getYearByDateKey(initialFilterString);
        Log.d(LOG_TAG, "year of initialFilterString = \"" + initialYear + "\"");
    }

    private void updateLinLayMonthsVisibility() {
        if (linLayYears.getVisibility() == View.GONE || linLayYears.getVisibility() == View.INVISIBLE) {
            linLayMonths.setVisibility(View.GONE);
            return;
        }

        if (!adapterYears.getCurrentSelection().equals("")) {
            linLayMonths.setVisibility(View.VISIBLE);
        }
    }

    private List<String> yearMonthList = new ArrayList<>();

    /**
     * Method to initialize all observers for getting live data from the database
     */
    private void initObservers() {
        Log.d(LOG_TAG, "initializing observers");
        viewModel.getAllDateKeys().observe(this, stringList -> {
            List<String> yearNamesList = new ArrayList<>();
            for (String dateString : stringList) {
                yearNamesList.add(dateString.substring(0, 4));
            }
            viewModel.setYearNames(yearNamesList);
            viewModel.setYearMonthNames(stringList);
            adapterYears.setDatesList(viewModel.getYearNames(), false);
        });

        viewModel.getYearMonthNames().observe(this, stringList -> {
            yearMonthList = stringList;
            List<String> monthNames = viewModel.getMonthsByYear(DateKeyUtils.getYearByDateKey(viewModel.getFilterString()), stringList);
            // Update the Recycler View adapter
            adapterMonths.setDatesList(monthNames, false);
            // Hide the progress bar, when the filters are loaded
            progressBar.setVisibility(View.GONE);
            linLayYears.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Method to create the options menu specified in the menu_filter.xml
     *
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_apply) {
            String newFilterString = parseSelectedFilter();

            // apply the filter string to the view model
            viewModel.setFilterString(newFilterString);

            // finish the activity with a result code
            endActivity(IntentCodes.Result.FILTER_OK, new Intent().setData(Uri.parse(newFilterString)));
            return true;
        } else if (itemId == R.id.action_remove_filter) {
            viewModel.setFilterString("");
            endActivity(IntentCodes.Result.FILTER_REMOVED, null);
            return true;
        } else if (itemId == android.R.id.home) {// action when clicking on the home up button
            endActivity(IntentCodes.Result.FILTER_CANCEL, null);
            return true;
        }
        return false;
    }

    /**
     * Method for ending the activity
     * This method is deprecated.
     * Consider using endActivity(FilterAction action, @Nullable Intent data)
     *
     * @param resultCode Integer representing the result code, which is sent to the caller activity
     * @param data       Intent containing data to be sent to the caller activity
     */
    @Deprecated
    private void endActivity(int resultCode, @Nullable Intent data) {
        if (data != null)
            setResult(resultCode, data);
        else
            setResult(resultCode);
        super.onBackPressed();
    }

    /**
     * Method for ending the activity
     *
     * @param action {@link IntentCodes.Result}
     * @param data   Intent containing data to be sent to the caller activity
     */
    private void endActivity(IntentCodes.Result action, @Nullable Intent data) {
        if (data != null)
            setResult((int) action.id, data);
        else
            setResult((int) action.id);
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        endActivity(IntentCodes.Result.FILTER_CANCEL, null);
    }

    private String parseSelectedFilter() {
        String yearsSelection = adapterYears.getCurrentSelection();
        String monthSelection = adapterMonths.getCurrentSelection();
        Log.d(LOG_TAG, "user wants to apply a filter: \"" + yearsSelection + "\" and \"" + monthSelection + "\"");

        String filterString = yearsSelection;

        // add optional month index to the filter string
        int monthIndex = viewModel.getMonthIndexByName(monthSelection);
        if (monthIndex != 0) {
            filterString = filterString + String.format(Locale.getDefault(), "%02d", monthIndex);
        }

        return filterString;
    }

    /**
     * Actions, when a filter button is clicked
     * When the text of the filter button is an integer, then the user has clicked on a button containing a year
     * Depending on the current state of the button (selected or unselected), the list with the corresponding
     * months is shown or hidden
     *
     * @param buttonText String containing the text of the clicked button
     * @param isSelected boolean which indicates, if the button is selected
     */
    @Override
    public void OnButtonClicked(String buttonText, boolean isSelected) {
        Log.d(LOG_TAG, "user clicked on button with text " + buttonText + ". Button is selected: " + isSelected);
        if (isInt(buttonText)) {
            Log.d(LOG_TAG, "user selected a button containing a year");

            // if the user selected the button, then update the months list
            // if the user unselected the button, then remove all items from the recycler view and hide the months list
            if (isSelected) {
                linLayMonths.setVisibility(View.VISIBLE);
                adapterMonths.setDatesList(viewModel.getMonthsByYear(buttonText, yearMonthList), true);
            } else {
                linLayMonths.setVisibility(View.GONE);
                adapterMonths.setDatesList(new ArrayList<>(), true);
            }
            updateLinLayMonthsVisibility();
        }
    }

    /**
     * Check if an string can be converted to an integer
     *
     * @param string String to be converted
     * @return boolean to indicate, if the string represents an integer
     */
    private boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
