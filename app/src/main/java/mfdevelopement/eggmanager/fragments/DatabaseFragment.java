package mfdevelopement.eggmanager.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.list_adapters.DailyBalanceListAdapter;
import mfdevelopement.eggmanager.viewmodels.DatabaseActivityViewModel;

import static android.app.Activity.RESULT_CANCELED;

public class DatabaseFragment extends Fragment {

    private DatabaseActivityViewModel databaseActivityViewModel;
    private DailyBalanceListAdapter adapter;

    public static final int NEW_ENTITY_REQUEST_CODE = 2;
    public static final int EDIT_ENTITY_REQUEST_CODE = 3;
    public static final int EDIT_FILTER_STRING_REQUEST_CODE = 4;

    public static final int NEW_ENTITY_RESULT_CODE = 2;
    public static final int EDITED_ENTITY_RESULT_CODE = 3;
    public static final int FILTER_ACTIVITY_OK_RESULT_CODE = 4;
    public static final int FILTER_ACTIVITY_CANCEL_RESULT_CODE = 5;

    private List<String> allDateKeys;

    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_DAILY_BALANCE = "extraDailyBalance";
    private final String LOG_TAG = "DatabaseFragment";
    private int totalEggsSold, totalEggsCollected;
    private double totalMoneyEarned;

    private TextView txtv_summary_eggs_collected, txtv_summary_eggs_sold, txtv_summary_money_earned;
    private TextView txtv_summary_extra_info;
    private TextView txtv_empty_recyclerview;
    private LinearLayout linLay_summary;

    private View mainView;
    private Context mainContext;

    private RecyclerView recyclerView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_database, container, false);
        mainView = root;

        // get view model
        if (getActivity() != null) {
            databaseActivityViewModel = new ViewModelProvider(getActivity()).get(DatabaseActivityViewModel.class);
        } else {
            String errorMsg = "Es ist ein Fehler beim Laden des ViewModels aufgetreten";
            Log.wtf(LOG_TAG,errorMsg);
            Snackbar.make(mainView.findViewById(R.id.main_container), errorMsg, Snackbar.LENGTH_LONG).show();
        }

        // this framgent has its own options menu
        setHasOptionsMenu(true);

        // get all GUI elements
        linLay_summary = root.findViewById(R.id.fragment_summary);
        hideSummary();
        txtv_summary_eggs_collected = root.findViewById(R.id.txtv_summary_eggsCollected);
        txtv_summary_eggs_sold = root.findViewById(R.id.txtv_summary_eggsSold);
        txtv_summary_money_earned = root.findViewById(R.id.txtv_summary_money_earned);
        txtv_summary_extra_info = root.findViewById(R.id.txtv_summary_extra_info);
        txtv_empty_recyclerview = root.findViewById(R.id.txtv_database_empty_recyclerview);

        // Recyclerview - get reference, init data
        recyclerView = root.findViewById(R.id.database_recyclerview);
        initRecyclerView();

        // initalize floating action button
        initFab(root);

        setObservers();

        return root;
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

        // update recycler view
        List<DailyBalance> filteredDailyBalances = databaseActivityViewModel.getFilteredDailyBalances();
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

        txtv_summary_eggs_collected.setText(String.format(Locale.getDefault(), "%d", totalEggsCollected));
        txtv_summary_eggs_sold.setText(String.format(Locale.getDefault(), "%d", totalEggsSold));
        txtv_summary_money_earned.setText(String.format(Locale.getDefault(), "%.2f", totalMoneyEarned));

        // show summary if the recycler view contains 2 or more items
        if (adapter != null) {
            int numItems = adapter.getItemCount();
            String extraInfo = numItems + " Einträge";
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_database, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * action when the user clicks a item on the action bar
     * @param item MenuItem of the ation bar
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_main_filter:
                openFilterActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFilterActivity() {
        Intent intent = new Intent(mainContext, FilterActivity.class);
        intent.putExtra(EXTRA_REQUEST_CODE_NAME, EDIT_FILTER_STRING_REQUEST_CODE);
        //setupExitSlideAnimation();
        startActivityForResult(intent, EDIT_FILTER_STRING_REQUEST_CODE);
    }

    private void setupExitSlideAnimation() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        if (getActivity() != null) {
            Log.e(LOG_TAG,"setupExitSlideAnimation():: getActivity() == null");
            getActivity().getWindow().setExitTransition(slide);
        }
    }

    private void initRecyclerView() {
        adapter = new DailyBalanceListAdapter(getActivity(), databaseActivityViewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        showTextEmptyRecyclerview(true);
    }

    private void showTextEmptyRecyclerview(boolean show) {
        if (txtv_empty_recyclerview != null) {
            if (show) {
                txtv_empty_recyclerview.setVisibility(View.VISIBLE);
            } else {
                txtv_empty_recyclerview.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        // results from FilterActivity
        else if (requestCode == EDIT_FILTER_STRING_REQUEST_CODE) {
            if (resultCode == FILTER_ACTIVITY_OK_RESULT_CODE) {
                Log.d(LOG_TAG, "activity finished. User changed filter string");

                // try to get the filter string from the FilterActivity
                if ((data != null) && (data.getData() != null)) {

                    // compare received filter string from the FilterActivity and the filter string from the view model
                    // they need to be the same!
                    String newFilterString = data.getData().toString();
                    String loadedFilterString = databaseActivityViewModel.getFilterString();
                    Log.d(LOG_TAG, "new filter string: from activity: \"" + newFilterString + "\", from viewModel: \"" + loadedFilterString + "\"");
                    if (!newFilterString.equals(loadedFilterString)) {
                        Log.e(LOG_TAG, "error when receiving the filter string, because string from the activity and string from the view model are not the same");
                        return;
                    }

                    // load filtered list of daily balances and update the list
                    List<DailyBalance> filteredDailyBalances = databaseActivityViewModel.getFilteredDailyBalances();
                    Log.d(LOG_TAG, "filtered list of daily balanced with filter key \"" + loadedFilterString + "\" has " + filteredDailyBalances.size() + " items");
                    adapter.setDailyBalances(filteredDailyBalances);
                    if (adapter.getItemCount() == 0) {
                        showTextEmptyRecyclerview(true);
                    } else {
                        showTextEmptyRecyclerview(false);
                    }

                    // update the summary
                    updateSummary();

                    // show a Snachbar to inform the user about the filter
                    if (loadedFilterString.isEmpty()) {
                        snackbarText = "Daten nicht gefiltert";
                    } else {
                        String filterName = "";
                        if (loadedFilterString.length() == 4) {
                            filterName = loadedFilterString;
                        }
                        if (loadedFilterString.length() >= 6) {
                            String year = DailyBalance.getYearByDateKey(loadedFilterString);

                            int indexMonth = Integer.parseInt(DailyBalance.getMonthByDateKey(loadedFilterString));
                            String month = databaseActivityViewModel.getMonthNameByIndex(indexMonth);

                            filterName = month + " " + year;
                        }

                        if (!filterName.isEmpty()) {
                            snackbarText = "Daten nach " + filterName + " gefiltert";
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Error when receiving new filter string from FilterActivity");
                }
            } else if (resultCode == FILTER_ACTIVITY_CANCEL_RESULT_CODE) {
                Log.d(LOG_TAG,"activity finished because user canceled the activity");
            }
        }

        // create a snackbar and display it
        if (!snackbarText.isEmpty()) {
            Snackbar.make(mainView.findViewById(R.id.main_container), snackbarText, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initFab(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mainContext, NewEntityActivity.class);
                intent.putExtra(EXTRA_REQUEST_CODE_NAME, NEW_ENTITY_REQUEST_CODE);
                startActivityForResult(intent, NEW_ENTITY_REQUEST_CODE);
            }
        });
    }

    /**
     * set observers for LiveData using the Room Database
     */
    private void setObservers() {

        databaseActivityViewModel.getAllDailyBalances().observe(getViewLifecycleOwner(), new Observer<List<DailyBalance>>() {
            @Override
            public void onChanged(@Nullable final List<DailyBalance> dailyBalanceList) {
                // Update the cached copy of the items in the adapter, if the list is not in filtered mode
                Log.d(LOG_TAG,"updating recycler view");
                if (dailyBalanceList != null) {
                    adapter.setDailyBalances(dailyBalanceList);
                    updateSummary();
                    if (adapter.getItemCount() == 0) {
                        showTextEmptyRecyclerview(true);
                    } else {
                        showTextEmptyRecyclerview(false);
                    }

                    // hide summary and show text field if the recycler view is not visible
                    boolean recyclerViewNotVisibile = (recyclerView.getVisibility() == View.GONE) || (recyclerView.getVisibility() == View.INVISIBLE);
                    if (recyclerViewNotVisibile) {
                        Log.d(LOG_TAG,"recyclerView is not visible");
                        txtv_empty_recyclerview.setText(getString(R.string.text_recyclerview_not_visible));
                        hideSummary();
                        showTextEmptyRecyclerview(true);
                    }
                }
            }
        });

        databaseActivityViewModel.getTotalEggsSold().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer totalEggsSold) {
                Log.d(LOG_TAG,"amount of total eggs sold changed. New value: " + String.format("%.4f",totalMoneyEarned));
                if (totalEggsSold != null) {
                    setTotalEggsSold(totalEggsSold);
                    updateSummary();
                }
            }
        });

        databaseActivityViewModel.getTotalMoneyEarned().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double totalMoneyEarned) {
                Log.d(LOG_TAG,"amount of total money earned changed. New value: " + String.format("%.4f",totalMoneyEarned));
                if (totalMoneyEarned != null) {
                    setTotalMoneyEarned(totalMoneyEarned);
                    updateSummary();
                }
            }
        });

        databaseActivityViewModel.getTotalEggsCollected().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer totalEggsCollected) {
                Log.d(LOG_TAG,"amount of total eggs collected changed. New value: " + String.format("%.4f",totalMoneyEarned));
                if (totalEggsCollected != null) {
                    setTotalEggsCollected(totalEggsCollected);
                    updateSummary();
                }
            }
        });

        databaseActivityViewModel.getDateKeys().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                setAllDateKeys(strings);
            }
        });
    }
}
