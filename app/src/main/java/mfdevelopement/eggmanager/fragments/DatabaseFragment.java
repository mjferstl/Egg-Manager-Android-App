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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.DataCompletenessCheckActivity;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;
import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.activity_contracts.NewEntityContract;
import mfdevelopement.eggmanager.activity_contracts.NewEntityIntentAdapter;
import mfdevelopement.eggmanager.activity_contracts.OpenFilterActivityContract;
import mfdevelopement.eggmanager.data_models.SortingItem;
import mfdevelopement.eggmanager.data_models.SortingItemCollection;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DateKeyUtils;
import mfdevelopement.eggmanager.dialog_fragments.DeleteDatabaseDialog;
import mfdevelopement.eggmanager.dialog_fragments.SortingDialogFragment;
import mfdevelopement.eggmanager.list_adapters.DailyBalanceListAdapter;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class DatabaseFragment extends Fragment {

    public static final String EXTRA_REQUEST_CODE_NAME = "requestCode";
    public static final String EXTRA_DAILY_BALANCE = "extraDailyBalance";
    public static final String EXTRA_ENTITY_DATE = "dailyBalanceDate";
    private final String LOG_TAG = "DatabaseFragment";
    private SharedViewModel viewModel;
    private DailyBalanceListAdapter adapter;
    private TextView txtv_summary_eggs_collected, txtv_summary_eggs_sold, txtv_summary_money_earned;
    private TextView txtv_summary_extra_info;
    private TextView txtv_empty_recyclerview;
    private ConstraintLayout linLay_summary;

    private View rootView;

    /**
     * Handle the results when creating a new entity
     */
    private final ActivityResultLauncher<NewEntityIntentAdapter> newEntityResultLauncher = registerForActivityResult(new NewEntityContract(), result -> {
        if (result == IntentCodes.Result.NEW_ENTITY.id) {
            Log.d(LOG_TAG, "user created a new entity");
            showSnackbarText(getString(R.string.new_entity_saved));
        }
    });

    /**
     * Variable to store the context
     */
    private Context mainContext;
    /**
     * Number of entries visible to the user
     */
    private int entriesCount;

    /**
     * RecyclerView to show the daily balance objects
     */
    private RecyclerView recyclerView;


    private final ActivityResultLauncher<Long> showFilterActivity = registerForActivityResult(new OpenFilterActivityContract(), result -> {

        // Update the filter string
        String filterString = viewModel.getDateFilter();

        if (result == IntentCodes.Result.FILTER_OK.id) {
            Log.v(LOG_TAG, "filter has been set successfully");

            // show a Snack bar to inform the user about the new filter
            if (filterString.isEmpty()) {
                showSnackbarText(getString(R.string.snachkbar_database_not_filtered));
            } else {
                String filterName = "";
                if (filterString.length() == 4) {
                    filterName = filterString;
                }
                if (filterString.length() >= 6) {
                    String year = DateKeyUtils.getYearByDateKey(filterString);

                    int indexMonth = Integer.parseInt(DateKeyUtils.getMonthByDateKey(filterString));
                    String month = viewModel.getMonthNameByIndex(indexMonth);

                    filterName = month + " " + year;
                }

                if (!filterName.isEmpty()) {
                    String snackBarMessage = String.format(Locale.getDefault(), getString(R.string.snackbar_database_filtered_by), filterName);
                    showSnackbarText(snackBarMessage);
                }
            }
        } else if (result == IntentCodes.Result.FILTER_CANCEL.id) {
            Log.v(LOG_TAG, "Editing the filter has been cancelled");
        } else if (result == IntentCodes.Result.FILTER_REMOVED.id) {
            Log.v(LOG_TAG, "filter has been removed");
        }
    });

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

        // initialize floating action button
        initFab();

        // set all observers for receiving LiveData from the database
        initObservers();

        return root;
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
                menuInflater.inflate(R.menu.menu_database, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

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

                // Return false if the selection has not been handled
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.setItemsUnselected();
    }

    /**
     * Update the UI everywhere, where the number of collected eggs is displayed
     *
     * @param numEggs number of collected eggs
     */
    private void updateEggsCollected(int numEggs) {
        Log.d(LOG_TAG, "updateEggsCollected(): updating number of collected eggs. New value: " + numEggs);
        txtv_summary_eggs_collected.setText(String.format(Locale.getDefault(), "%d", numEggs));
        updateSummary();
    }

    /**
     * Update the UI everywhere, where the number of sold eggs is displayed
     *
     * @param numEggs number of sold eggs
     */
    private void updateEggsSold(int numEggs) {
        Log.d(LOG_TAG, "updateEggsSold(): updating number of sold eggs. New value: " + numEggs);
        txtv_summary_eggs_sold.setText(String.format(Locale.getDefault(), "%d", numEggs));
        updateSummary();
    }

    /**
     * Update the UI everywhere, where the amount of money earned is displayed
     *
     * @param amountMoney amount of money which has been earned
     */
    private void updateMoneyEarned(double amountMoney) {
        Log.d(LOG_TAG, "updateMoneyEarned(): updating the amount of money earned. New value: " + amountMoney);
        txtv_summary_money_earned.setText(String.format(Locale.getDefault(), "%.2f", amountMoney));
        updateSummary();
    }

    /**
     * Update the summary UI element
     */
    private void updateSummary() {
        // hide summary and show text field if the recycler view is not visible or contains less than 2 items
        // show summary if the recycler view contains 2 or more items
        boolean recyclerViewNotVisible = (recyclerView.getVisibility() == View.GONE) || (recyclerView.getVisibility() == View.INVISIBLE);
        showTextEmptyRecyclerview(false);

        if (adapter != null) {
            int numItems = adapter.getItemCount();
            String extraInfo = String.format(Locale.getDefault(), getString(R.string.text_formatted_num_entries), numItems);
            if (numItems >= 2)
                showSummary();
            else {
                hideSummary();
                if (adapter.getItemCount() == 0) {
                    Log.d(LOG_TAG, "recyclerView contains no items");
                    showTextEmptyRecyclerview(true);
                    txtv_empty_recyclerview.setText(getString(R.string.text_empty_recyclerview));
                } else if (recyclerViewNotVisible) {
                    Log.d(LOG_TAG, "recyclerView is not visible");
                    //showTextEmptyRecyclerview(true);
                    txtv_empty_recyclerview.setText(getString(R.string.text_recyclerview_not_visible));
                }
            }
            txtv_summary_extra_info.setText(extraInfo);
        }
    }

    /**
     * Hide the summary UI element
     */
    private void hideSummary() {
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.GONE);
    }

    /**
     * Show the summary UI element
     */
    private void showSummary() {
        if (linLay_summary != null)
            linLay_summary.setVisibility(View.VISIBLE);
    }

    /**
     * Show a dialog for deleting the content of the database
     */
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

    /**
     * Show a dialog for changing the sorting order of the items in the recycler view
     */
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
            sortingList.addItem(new SortingItem(getString(R.string.text_sort_ascending), "ASC", false));
            sortingList.addItem(new SortingItem(getString(R.string.text_sort_descending), "DESC", false));
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
        intent.putExtra(EXTRA_REQUEST_CODE_NAME, IntentCodes.Request.EDIT_FILTER.id);
        //setupExitSlideAnimation();

        showFilterActivity.launch(null);
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

    /**
     * Initialize the {@link RecyclerView}
     */
    private void initRecyclerView() {
        adapter = new DailyBalanceListAdapter(getActivity());
        adapter.addOnItemActionClickListener(new DailyBalanceListAdapter.OnItemActionClickListener() {
            @Override
            public boolean onEditClicked(int position) {
                Log.v(LOG_TAG, "user wants to edit the item at position " + position);

                // Get the element
                DailyBalance data = adapter.getItem(position);

                // start the activity to edit the database item
                newEntityResultLauncher.launch(new NewEntityIntentAdapter(data));
                return true;
            }

            @Override
            public boolean onDeleteClicked(int position) {
                Log.v(LOG_TAG, "user wants to delete the item at position " + position);

                viewModel.delete(adapter.getItem(position));
                return true;
            }
        });
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

    /**
     * Show a {@link Snackbar}
     *
     * @param text String to be displayed in the Snackbar
     */
    private void showSnackbarText(@NonNull String text) {
        // create a snackbar and display it
        if (!text.isEmpty())
            Snackbar.make(rootView.findViewById(R.id.main_container), text, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Initialize the {@link FloatingActionButton}
     */
    private void initFab() {
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(mainContext, NewEntityActivity.class);
            intent.putExtra(EXTRA_REQUEST_CODE_NAME, IntentCodes.Request.NEW_ENTITY.id);

            newEntityResultLauncher.launch(new NewEntityIntentAdapter());
        });
    }

    /**
     * Initialize observers for LiveData using the Room Database
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

                viewModel.upgradeDailyBalancesInDatabase(dailyBalanceList);
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
