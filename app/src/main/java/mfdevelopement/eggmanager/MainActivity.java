package mfdevelopement.eggmanager;

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
import androidx.fragment.app.DialogFragment;
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

public class MainActivity extends AppCompatActivity implements FilterDialogFragment.OnAddFilterListener{

    private DailyBalanceViewModel dailyBalanceViewModel;
    public static final int NEW_ENTITY_REQUEST_CODE = 2;
    public static final int EDIT_ENTITY_REQUEST_CODE = 3;

    public static final int NEW_ENTITY_RESULT_CODE = 2;
    public static final int EDITED_ENTITY_RESULT_CODE = 3;

    private List<String> allDateKeys;

    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_PRIMATRY_KEY_NAME = "dateKeyPrimary";
    public static final String EXTRA_DAILY_BALANCE = "extraDailyBalance";
    private final String LOG_TAG = "MainActivity";
    private int totalEggsSold, totalEggsCollected;
    private double totalMoneyEarned;

    private TextView txtv_summary_eggs_collected, txtv_summary_eggs_sold, txtv_summary_money_earned;
    private TextView txtv_summary_extra_info;
    private LinearLayout linLay_summary;

    private boolean filtered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        linLay_summary = findViewById(R.id.linLay_summary);
        hideSummary();
        txtv_summary_eggs_collected = findViewById(R.id.txtv_summary_eggsCollected);
        txtv_summary_eggs_sold = findViewById(R.id.txtv_summary_eggsSold);
        txtv_summary_money_earned = findViewById(R.id.txtv_summary_money_earned);
        txtv_summary_extra_info = findViewById(R.id.txtv_summary_extra_info);

        initFab();

        dailyBalanceViewModel = new ViewModelProvider(this).get(DailyBalanceViewModel.class);

        Log.d(LOG_TAG,"get number of entries for october 2019: number = " + dailyBalanceViewModel.getFilteredDailyBalanceList("201910").size());

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
    }

    private void hideSummary() {
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.GONE);
    }

    private void displaySummary() {
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
            DialogFragment newFragment = FilterDialogFragment.newInstance(allDateKeys, dailyBalanceViewModel);

            //newFragment.setTargetFragment(this, 1);
            newFragment.show(getSupportFragmentManager(), "filterDialog");
            return false;
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
        final DailyBalanceListAdapter adapter = new DailyBalanceListAdapter(this, dailyBalanceViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dailyBalanceViewModel.getAllDailyBalances().observe(this, new Observer<List<DailyBalance>>() {
            @Override
            public void onChanged(@Nullable final List<DailyBalance> dailyBalanceList) {
                // Update the cached copy of the items in the adapter, if the list is not in filtered mode
                if (!filtered) { adapter.setDailyBalances(dailyBalanceList);}

                // show summary if the recycler view contains 2 or more items
                int numItems = adapter.getItemCount();
                if (numItems >= 2) {
                    displaySummary();
                    String extraInfo = "(" + numItems + " Einträge)";
                    txtv_summary_extra_info.setText(extraInfo);
                } else {
                    hideSummary();
                    txtv_summary_extra_info.setText("");
                }
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
    public void onAddFilterListenerSubmit(String filterString) {

    }
}
