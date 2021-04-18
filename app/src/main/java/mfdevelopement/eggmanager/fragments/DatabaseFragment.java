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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.DataCompletenessCheckActivity;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;
import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.data_models.SortingItem;
import mfdevelopement.eggmanager.data_models.SortingItemCollection;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.dialog_fragments.DeleteDatabaseDialog;
import mfdevelopement.eggmanager.dialog_fragments.SortingDialogFragment;
import mfdevelopement.eggmanager.list_adapters.DailyBalanceListAdapter;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

import static mfdevelopement.eggmanager.utils.FilterActivityResultHandler.handleFilterActivityResult;

public class DatabaseFragment extends Fragment {

    private SharedViewModel viewModel;
    private DailyBalanceListAdapter adapter;

    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_DAILY_BALANCE = "extraDailyBalance";
    public static final String EXTRA_ENTITY_DATE = "dailyBalanceDate";
    private final String LOG_TAG = "DatabaseFragment";

    private TextView txtv_summary_eggs_collected, txtv_summary_eggs_sold, txtv_summary_money_earned;
    private TextView txtv_summary_extra_info;
    private TextView txtv_empty_recyclerview;
    private ConstraintLayout linLay_summary;

    private View rootView;
    private Context mainContext;

    private int entriesCount;

    private RecyclerView recyclerView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainContext = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_database, container, false);
        rootView = root;

        // get view model
        if (getActivity() != null) {
            viewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        } else {
            String errorMsg = "Es ist ein Fehler beim Laden des ViewModels aufgetreten";
            Log.wtf(LOG_TAG, errorMsg);
            Snackbar.make(rootView.findViewById(R.id.main_container), errorMsg, Snackbar.LENGTH_LONG).show();
        }

        // add sortingOrderChangedListener
        // when the user changes the sorting order, then the recycler view needs to be updated manually
        ((MainNavigationActivity) getActivity()).setSortingOrderChangedListener(this::reverseRecyclerView);

        // this framgent has its own options menu
        setHasOptionsMenu(true);

        // get all GUI elements
        linLay_summary = root.findViewById(R.id.fragment_summary);
        txtv_summary_eggs_collected = root.findViewById(R.id.txtv_summary_eggsCollected);
        txtv_summary_eggs_sold = root.findViewById(R.id.txtv_summary_eggsSold);
        txtv_summary_money_earned = root.findViewById(R.id.txtv_summary_money_earned);
        txtv_summary_extra_info = root.findViewById(R.id.txtv_summary_extra_info);
        txtv_empty_recyclerview = root.findViewById(R.id.txtv_database_empty_recyclerview);

        // Recyclerview - get reference, init data
        recyclerView = root.findViewById(R.id.database_recyclerview);
        initRecyclerView();

        // make sure the filter is up to date, e.g. when the user switched to this fragment
        viewModel.setDateFilter(viewModel.loadDateFilter());

        // initalize floating action button
        initFab();

        // set all observers for receiving LiveData from the database
        initObservers();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.setItemsUnselected();

        // update the data filter string
        updateDataFilter();
    }


    private void updateEggsCollected(int numEggs) {
        Log.d(LOG_TAG, "updateEggsCollected(): updating number of collected eggs. New value: " + numEggs);
        txtv_summary_eggs_collected.setText(String.format(Locale.getDefault(), "%d", numEggs));
        updateSummary();
    }

    private void updateEggsSold(int numEggs) {
        Log.d(LOG_TAG, "updateEggsSold(): updating number of sold eggs. New value: " + numEggs);
        txtv_summary_eggs_sold.setText(String.format(Locale.getDefault(), "%d", numEggs));
        updateSummary();
    }

    private void updateMoneyEarned(double amountMoney) {
        Log.d(LOG_TAG, "updateMoneyEarned(): updating the amount of money earned. New value: " + amountMoney);
        txtv_summary_money_earned.setText(String.format(Locale.getDefault(), "%.2f", amountMoney));
        updateSummary();
    }

    private void updateSummary() {
        // hide summary and show text field if the recycler view is not visible or contains less than 2 items
        // show summary if the recycler view contains 2 or more items
        boolean recyclerViewNotVisibile = (recyclerView.getVisibility() == View.GONE) || (recyclerView.getVisibility() == View.INVISIBLE);
        showTextEmptyRecyclerview(false);

        if (adapter != null) {
            int numItems = adapter.getItemCount();
            String extraInfo = numItems + " Einträge";
            if (numItems >= 2)
                showSummary();
            else {
                hideSummary();
                if (adapter.getItemCount() == 0) {
                    Log.d(LOG_TAG, "recyclerView contains no items");
                    showTextEmptyRecyclerview(true);
                    txtv_empty_recyclerview.setText(getString(R.string.text_empty_recyclerview));
                } else if (recyclerViewNotVisibile) {
                    Log.d(LOG_TAG, "recyclerView is not visible");
                    //showTextEmptyRecyclerview(true);
                    txtv_empty_recyclerview.setText(getString(R.string.text_recyclerview_not_visible));
                }
            }
            txtv_summary_extra_info.setText(extraInfo);
        }
    }

    private void hideSummary() {
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.GONE);
    }

    private void showSummary() {
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_database, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (!(entriesCount > 0)) {
            Snackbar.make(rootView.findViewById(R.id.main_container), getString(R.string.snackbar_no_data_to_filter), Snackbar.LENGTH_SHORT).show();
        } else {
            if (id == R.id.action_data_filter) {// Open filter activity, if there's some data in the database
                openFilterActivity();
                return true;
            } else if (id == R.id.action_data_sort) {// Show options for list sorting, if there's some data in the database
                showSortingDialog();
                return true;
            } else if (id == R.id.action_completeness_check) {// Show completeness check activity, if there's some data in the database
                openCompletenessCheckActivity();
                return true;
            } else if (id == R.id.action_delete_database) {// Delete all items of the database
                showDeleteDialog();
                return true;
            }
        }

        // call the super method, if the item selection has not been handeled
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        if (getActivity() != null) {
            // FragmentActivity.getSupportFragmentManager()
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = DeleteDatabaseDialog.newInstance();
            newFragment.show(ft, "dialog");
        } else {
            Log.e(LOG_TAG, "getActivity() = null");
        }
    }

    private void showSortingDialog() {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        if (getActivity() != null) {
            // FragmentActivity.getSupportFragmentManager()
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            //
            SortingItemCollection sortingList = new SortingItemCollection();
            sortingList.addItem(new SortingItem("Aufsteigend", "ASC", false));
            sortingList.addItem(new SortingItem("Absteigend", "DESC", false));
            String savedSortingOrder = viewModel.getSortingOrder();
            for (SortingItem item : sortingList.getItems()) {
                if (item.getSortingOrder().equals(savedSortingOrder))
                    item.setSelected(true);
            }

            // Create and show the dialog.
            DialogFragment newFragment = SortingDialogFragment.newInstance(sortingList);
            newFragment.show(ft, "dialog");
        } else {
            Log.e(LOG_TAG, "getActivity() = null");
        }
    }

    private void openFilterActivity() {
        Intent intent = new Intent(mainContext, FilterActivity.class);
        intent.putExtra(EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.EDIT_FILTER.ordinal());
        //setupExitSlideAnimation();
        startActivityForResult(intent, DatabaseActions.Request.EDIT_FILTER.ordinal());
    }

    private void openCompletenessCheckActivity() {
        Intent intent = new Intent(mainContext, DataCompletenessCheckActivity.class);
        startActivity(intent);
    }

    private void setupExitSlideAnimation() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        if (getActivity() != null) {
            Log.e(LOG_TAG, "setupExitSlideAnimation():: getActivity() == null");
            getActivity().getWindow().setExitTransition(slide);
        }
    }

    private void initRecyclerView() {
        adapter = new DailyBalanceListAdapter(getActivity(), viewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        showTextEmptyRecyclerview(true);
    }

    private void showTextEmptyRecyclerview(boolean show) {
        if (txtv_empty_recyclerview != null && recyclerView != null) {
            if (show) {
                recyclerView.setVisibility(View.GONE);
                txtv_empty_recyclerview.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                txtv_empty_recyclerview.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Re-Sort the items of the recycler view, if the sorting order changed
     */
    private void reverseRecyclerView() {
        Log.d(LOG_TAG, "reverseRecyclerView(): updating recycler view items, because the sorting order may has changed");
        List<DailyBalance> currentList = adapter.getItems();
        Collections.reverse(currentList);
        adapter.setDailyBalances(currentList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG, "onActivityResult::requestCode=" + requestCode + ",resultCode=" + resultCode);

        String snackbarText = "";

        if (requestCode == DatabaseActions.Request.NEW_ENTITY.ordinal() && resultCode == DatabaseActions.Result.NEW_ENTITY.ordinal()) {
            snackbarText = getString(R.string.new_entity_saved);
        } else if (requestCode == DatabaseActions.Request.EDIT_ENTITY.ordinal() && resultCode == DatabaseActions.Result.ENTITY_EDITED.ordinal()) {
            snackbarText = getString(R.string.changes_saved);
        }
        // results from FilterActivity
        else if (requestCode == DatabaseActions.Request.EDIT_FILTER.ordinal()) {
            handleFilterActivityResult(resultCode, data);

            if (resultCode == DatabaseActions.Result.FILTER_OK.ordinal()) {

                Log.d(LOG_TAG, "filtered list of daily balanced with filter key \"" + viewModel.loadDateFilter() + "\"");
                updateDataFilter();

                // get the new filter string
                String newFilterString = viewModel.getDateFilter();

                // show a Snackbar to inform the user about the new filter
                if (newFilterString.isEmpty()) {
                    snackbarText = "Daten nicht gefiltert";
                } else {
                    String filterName = "";
                    if (newFilterString.length() == 4) {
                        filterName = newFilterString;
                    }
                    if (newFilterString.length() >= 6) {
                        String year = DailyBalance.getYearByDateKey(newFilterString);

                        int indexMonth = Integer.parseInt(DailyBalance.getMonthByDateKey(newFilterString));
                        String month = viewModel.getMonthNameByIndex(indexMonth);

                        filterName = month + " " + year;
                    }

                    if (!filterName.isEmpty())
                        snackbarText = "Daten nach " + filterName + " gefiltert";
                }
            } else if (resultCode == DatabaseActions.Result.FILTER_REMOVED.ordinal()) {
                updateDataFilter();
                Log.d(LOG_TAG, "user removed the filter");
            }
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        }

        // create a snackbar and display it
        if (!snackbarText.isEmpty())
            Snackbar.make(rootView.findViewById(R.id.main_container), snackbarText, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * update the filter string for the displayed data in the view model
     * load the filter string from the repository to update the filter string in the view model
     */
    private void updateDataFilter() {
        viewModel.setDateFilter(viewModel.loadDateFilter());
    }

    private void initFab() {
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(mainContext, NewEntityActivity.class);
            intent.putExtra(EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.NEW_ENTITY.ordinal());
            startActivityForResult(intent, DatabaseActions.Request.NEW_ENTITY.ordinal());
        });
    }

    /**
     * set observers for LiveData using the Room Database
     */
    private void initObservers() {

        viewModel.getFilteredDailyBalance().observe(getViewLifecycleOwner(), dailyBalanceList -> {
            // Update the cached copy of the items in the adapter
            Log.d(LOG_TAG, "getFilteredDailyBalance() received updated data");
            if (dailyBalanceList != null) {
                Log.d(LOG_TAG, "new list has " + dailyBalanceList.size() + " items");

                // if the user wants to get the items in the recycler view in descending order, then reverse the list
                String savedSortingOrder = viewModel.getSortingOrder();
                if (savedSortingOrder.equals("DESC"))
                    Collections.reverse(dailyBalanceList);

                // update the recycler view
                adapter.setDailyBalances(dailyBalanceList);

                // update the summary
                updateSummary();
            }
        });

        viewModel.getFilteredEggsCollected().observe(getViewLifecycleOwner(), eggsCollected -> {
            Log.d(LOG_TAG, "getFilteredEggsCollected() received updated data");
            if (eggsCollected != null)
                updateEggsCollected(eggsCollected);
        });

        viewModel.getFilteredEggsSold().observe(getViewLifecycleOwner(), eggsSold -> {
            Log.d(LOG_TAG, "getFilteredEggsSold() received updated data");
            if (eggsSold != null)
                updateEggsSold(eggsSold);
        });

        viewModel.getFilteredMoneyEarned().observe(getViewLifecycleOwner(), moneyEarned -> {
            Log.d(LOG_TAG, "getFilteredMoneyEarned() received updated data");
            if (moneyEarned != null)
                updateMoneyEarned(moneyEarned);
        });

        viewModel.getEntriesCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null)
                entriesCount = count;
        });
    }
}
