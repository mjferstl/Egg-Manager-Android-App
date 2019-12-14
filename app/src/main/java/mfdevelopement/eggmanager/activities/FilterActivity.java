package mfdevelopement.eggmanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;
import mfdevelopement.eggmanager.list_adapters.DateFilterListAdapter;
import mfdevelopement.eggmanager.viewmodels.FilterActivityViewModel;

public class FilterActivity extends AppCompatActivity {

    private final String LOG_TAG = "FilterActivity";
    private DateFilterListAdapter adapter;

    private FilterActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Log.d(LOG_TAG,"starting onCreate()");

        // get reference to the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_filter_activity);
        setSupportActionBar(toolbar);

        // add a button for moving up to the calling activity
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the view model
        viewModel = new ViewModelProvider(this).get(FilterActivityViewModel.class);

        //
        initObservers();

        // initialite the GUI
        initGUI();

        Log.d(LOG_TAG,"finished onCreate()");
    }

    private void initGUI() {
        RecyclerView recyclerViewYears = findViewById(R.id.filter_activity_recycler_view_years);
        recyclerViewYears.setLayoutManager(new GridLayoutManager(this,2));

        // check if the initial filter string is valid
        String initialFilterString = viewModel.getFilterString();
        if (initialFilterString == null) {
            Log.e(LOG_TAG,"cannot initialize GUI because initialFilterString is NULL");
            return;
        }

        List<String> yearsList = viewModel.getYearNames();

        adapter = new DateFilterListAdapter(this, yearsList, initialFilterString);
        recyclerViewYears.setAdapter(adapter);

        // copied from other class -->
        //yearsAdapter.setOnItemClickListener((FilterDialogListAdapter.OnFilterSelectListener) parentContext);
    }

    private void initObservers() {
        Log.d(LOG_TAG,"initializing observers");
        viewModel.getAllDateKeys().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> stringList) {
                List<String> yearNamesList = new ArrayList<>();
                for (String dateString : stringList) {
                    yearNamesList.add(dateString.substring(0,4));
                }
                viewModel.setYearNames(yearNamesList);
                adapter.setDatesList(viewModel.getYearNames());
            }
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
                String newFiterString = applyFilterString();
                Snackbar.make(findViewById(R.id.filter_activity_container), "Funktion noch nicht implementiert",Snackbar.LENGTH_SHORT).show();
                setResult(DatabaseFragment.FILTER_ACTIVITY_OK_RESULT_CODE, new Intent().setData(Uri.parse(newFiterString)));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String applyFilterString() {
        String yearsSelection = adapter.getCurrentSelection();
        Log.d(LOG_TAG,"user wants to apply the filter \"" + yearsSelection + "\"");
        viewModel.setFilterString(yearsSelection);
        return yearsSelection;
    }
}
