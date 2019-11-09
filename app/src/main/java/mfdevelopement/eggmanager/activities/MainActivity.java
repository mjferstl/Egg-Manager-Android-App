package mfdevelopement.eggmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment;
import mfdevelopement.eggmanager.list_adapters.DailyBalanceListAdapter;
import mfdevelopement.eggmanager.list_adapters.FilterDialogListAdapter;
import mfdevelopement.eggmanager.viewmodels.DailyBalanceViewModel;

public class MainActivity extends AppCompatActivity implements FilterDialogListAdapter.OnFilterSelectListener, FilterDialogFragment.OnClickListener{

    private DailyBalanceViewModel dailyBalanceViewModel;
    private DailyBalanceListAdapter adapter;
    private FilterDialogFragment filterDialogFragment;

    public static final int NEW_ENTITY_REQUEST_CODE = 2;
    public static final int EDIT_ENTITY_REQUEST_CODE = 3;

    public static final int NEW_ENTITY_RESULT_CODE = 2;
    public static final int EDITED_ENTITY_RESULT_CODE = 3;

    private List<String> allDateKeys;

    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_DAILY_BALANCE = "extraDailyBalance";
    private final String LOG_TAG = "MainActivity";
    private int totalEggsSold, totalEggsCollected;
    private double totalMoneyEarned;

    private TextView txtv_summary_eggs_collected, txtv_summary_eggs_sold, txtv_summary_money_earned;
    private TextView txtv_summary_extra_info;
    private LinearLayout linLay_summary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        linLay_summary = findViewById(R.id.fragment_summary);
        hideSummary();
        txtv_summary_eggs_collected = findViewById(R.id.txtv_summary_eggsCollected);
        txtv_summary_eggs_sold = findViewById(R.id.txtv_summary_eggsSold);
        txtv_summary_money_earned = findViewById(R.id.txtv_summary_money_earned);
        txtv_summary_extra_info = findViewById(R.id.txtv_summary_extra_info);

        initFab();

        dailyBalanceViewModel = new ViewModelProvider(this).get(DailyBalanceViewModel.class);

        setObservers();
    }

    private void setTotalEggsCollected(int amount) {
        this.totalEggsCollected = amount;
    }

    private void setTotalEggsSold(int amount) {
        this.totalEggsSold = amount;
    }

    private void setTotalMoneyEarned(double amount) {
        this.totalMoneyEarned = amount;
    }

    private void updateSummary() {
        txtv_summary_eggs_collected.setText(String.valueOf(totalEggsCollected));
        txtv_summary_eggs_sold.setText(String.valueOf(totalEggsSold));
        txtv_summary_money_earned.setText(String.format(Locale.getDefault(),"%.2f",totalMoneyEarned));

        // show summary if the recycler view contains 2 or more items
        if (adapter != null) {
            int numItems = adapter.getItemCount();
            String extraInfo = +numItems + " Einträge";
            if (numItems >= 2) {
                showSummary();
            } else {
                hideSummary();
                txtv_summary_extra_info.setText("");
            }
            txtv_summary_extra_info.setText(extraInfo);
        }
    }

    private void hideSummary() {
        Log.d(LOG_TAG,"hideSummary");
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.GONE);
    }

    private void showSummary() {
        Log.d(LOG_TAG,"showSummary");
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.VISIBLE);
    }

    private void setAllDateKeys(List<String> stringList) {
        allDateKeys = stringList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
            return false;
        }
        else if (id == R.id.action_main_filter) {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ft.commit();

            // Create and show the dialog.
            filterDialogFragment = FilterDialogFragment.newInstance(allDateKeys);

            //newFragment.setTargetFragment(this, 1);
            filterDialogFragment.show(getSupportFragmentManager(), "filterDialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG,"onActivityResult::requestCode=" + requestCode + ",resultCode=" + resultCode);

        String snackbarText = "";

        if (resultCode == RESULT_CANCELED) {
            snackbarText = getString(R.string.entry_not_added);
        }
        else if (requestCode == NEW_ENTITY_REQUEST_CODE && resultCode == NEW_ENTITY_RESULT_CODE) {
            snackbarText = getString(R.string.new_entity_saved);
        }
        else if (requestCode == EDIT_ENTITY_REQUEST_CODE && resultCode == EDITED_ENTITY_RESULT_CODE) {
            snackbarText = getString(R.string.changes_saved);
        }

        // create a snackbar and display it
        if (!snackbarText.isEmpty())
            Snackbar.make(findViewById(R.id.main_container),snackbarText,Snackbar.LENGTH_SHORT).show();
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewEntityActivity.class);
                intent.putExtra(EXTRA_REQUEST_CODE_NAME, NEW_ENTITY_REQUEST_CODE);
                //startActivity(intent);
                startActivityForResult(intent, NEW_ENTITY_REQUEST_CODE);
            }
        });
    }

    private void setObservers() {

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new DailyBalanceListAdapter(this, dailyBalanceViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dailyBalanceViewModel.getAllDailyBalances().observe(this, new Observer<List<DailyBalance>>() {
            @Override
            public void onChanged(@Nullable final List<DailyBalance> dailyBalanceList) {
                // Update the cached copy of the items in the adapter, if the list is not in filtered mode
                Log.d(LOG_TAG,"updating recycler view");
                adapter.setDailyBalances(dailyBalanceList);
                updateSummary();
            }
        });

        dailyBalanceViewModel.getTotalEggsSold().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer totalEggsSold) {
                setTotalEggsSold(totalEggsSold);
                updateSummary();
            }
        });

        dailyBalanceViewModel.getTotalMoneyEarned().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double totalMoneyEarned) {
                setTotalMoneyEarned(totalMoneyEarned);
                updateSummary();
            }
        });

        dailyBalanceViewModel.getTotalEggsCollected().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer totalEggsCollected) {
                setTotalEggsCollected(totalEggsCollected);
                updateSummary();
            }
        });

        dailyBalanceViewModel.getDateKeys().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                setAllDateKeys(strings);
            }
        });
    }

    @Override
    public void onFilterSelected(String filterString, int buttonPosition, boolean isSelected) {
        // update dialog fragment
        filterDialogFragment.setButtonSelected(filterString, buttonPosition, isSelected);
    }

    @Override
    public void onOkClicked(String selectedFilterString) {

        if (!selectedFilterString.equals(FilterDialogFragment.NOT_SET_FILTER_STRING))
            dailyBalanceViewModel.setFilterString(selectedFilterString);

        // dismiss dialog
        filterDialogFragment.dismiss();

        // update recycler view
        List<DailyBalance> filteredDailyBalances = dailyBalanceViewModel.getFilteredDailyBalances();
        adapter.setDailyBalances(filteredDailyBalances);

        // update summary
        int countEggsCollected = 0, countEggsSold = 0;
        double countMoneyEarned = 0;
        for(int i=0; i<filteredDailyBalances.size(); i++) {
            countEggsCollected = countEggsCollected + filteredDailyBalances.get(i).getEggsCollected();
            countEggsSold = countEggsSold + filteredDailyBalances.get(i).getEggsSold();
            countMoneyEarned = countMoneyEarned + filteredDailyBalances.get(i).getMoneyEarned();
        }
        setTotalEggsCollected(countEggsCollected);
        setTotalEggsSold(countEggsSold);
        setTotalMoneyEarned(countMoneyEarned);
        updateSummary();
    }

    @Override
    public void onCancelClicked() {
        // dismiss dialog
        dailyBalanceViewModel.setFilterString("");
        filterDialogFragment.dismiss();
    }
}
