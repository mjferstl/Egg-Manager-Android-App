package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.dialog_fragments.SortingDialogFragment;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

public class MainNavigationActivity extends AppCompatActivity
        implements SortingDialogFragment.OnSortingItemClickListener{

    private AppBarConfiguration mAppBarConfiguration;
    private final String LOG_TAG = "MainNavigationActivity";
    private NavController navController;
    private SharedViewModel viewModel;

    public SortingOrderChangedListener sortingOrderChangedListener;
    public BackupListener backupListener;

    /**
     * Interface for use in the fragments, if the sorting order gets changed
      */
    public interface SortingOrderChangedListener {
        void onSortingOrderReversed();
    }

    /**
     * Get the SortingOrderChangedListener of this activity
     * @return the instance of SortingOrderChangedListener of this activity
     */
    public SortingOrderChangedListener getSortingOrderChangedListener() {
        return sortingOrderChangedListener;
    }

    /**
     * Set the listener
     * @param sortingOrderChangedListener listener to be set
     */
    public void setSortingOrderChangedListener(SortingOrderChangedListener sortingOrderChangedListener) {
        this.sortingOrderChangedListener = sortingOrderChangedListener;
    }

    public interface BackupListener {
        void onBackupImportClicked(int position);
        void onBackupDeleteClicked(int position);
        void onBackupCreated(String filename);
    }

    public BackupListener getBackupListener() {
        return backupListener;
    }

    public void setBackupListener(BackupListener backupListener) {
        this.backupListener = backupListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"starting activity MainNavigationActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_main_database, R.id.nav_diagrams, R.id.nav_about,
                R.id.nav_backup)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void OnSortingItemClicked(String newSortingOrder) {
        Log.d(LOG_TAG,"saving new sorting order \"" + newSortingOrder + "\"");

        String savedSortingOrder = viewModel.getSortingOrder();

        // if the user wants to change the sorting order
        if (!savedSortingOrder.equals(newSortingOrder)) {

            // save the new sorting order
            viewModel.setSortingOrder(newSortingOrder);

            // if the new sorting order is the reverse of the current order
            if ((newSortingOrder.equals("ASC") && savedSortingOrder.equals("DESC")) ||
                    (newSortingOrder.equals("DESC") && savedSortingOrder.equals("ASC"))) {
                if (getSortingOrderChangedListener() != null)
                    getSortingOrderChangedListener().onSortingOrderReversed();
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String selectedOption = item.getTitle().toString();
        int position = item.getOrder();
        Log.d(LOG_TAG,"user clicked on the item with title: " + selectedOption + ", order/position: " + position);

        if (selectedOption.equals(getString(R.string.txt_option_import))) {
            getBackupListener().onBackupImportClicked(position);
            return true;
        } else if (selectedOption.equals(getString(R.string.txt_option_delete))) {
            getBackupListener().onBackupDeleteClicked(position);
            return true;
        } else
            return super.onContextItemSelected(item);
    }

    public void abc(String filename) {
        getBackupListener().onBackupCreated(filename);
    }
}
