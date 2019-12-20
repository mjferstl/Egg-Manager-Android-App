package mfdevelopement.eggmanager.activities;

import android.os.Bundle;
import android.util.Log;

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
import mfdevelopement.eggmanager.data_models.DatabaseBackup;
import mfdevelopement.eggmanager.dialog_fragments.BackupOptionsDialogFragment;
import mfdevelopement.eggmanager.dialog_fragments.SortingDialogFragment;
import mfdevelopement.eggmanager.list_adapters.DatabaseBackupListAdapter;
import mfdevelopement.eggmanager.viewmodels.SharedViewModel;

import static mfdevelopement.eggmanager.dialog_fragments.BackupOptionsDialogFragment.OPTION_DELETE;
import static mfdevelopement.eggmanager.dialog_fragments.BackupOptionsDialogFragment.OPTION_IMPORT;

public class MainNavigationActivity extends AppCompatActivity
        implements SortingDialogFragment.OnSortingItemClickListener, DatabaseBackupListAdapter.BackupItemClickListener, BackupOptionsDialogFragment.BackupOptionClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private final String LOG_TAG = "MainNavigationActivity";
    private NavController navController;
    private SharedViewModel viewModel;

    public SortingOrderChangedListener sortingOrderChangedListener;
    public BackupSelectedListener backupSelectedListener;

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

    public interface BackupSelectedListener {
        void onBackupSelected(DatabaseBackup backup);
        void onBackupDeleteClicked(DatabaseBackup backup);
        void onBackupImportClicked(DatabaseBackup backup);
    }

    public BackupSelectedListener getBackupSelectedListener() {
        return backupSelectedListener;
    }

    public void setBackupSelectedListener(BackupSelectedListener backupSelectedListener) {
        this.backupSelectedListener = backupSelectedListener;
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
    public void onBackupItemClicked(DatabaseBackup backup) {
        Log.d(LOG_TAG,"user clicked on backup with the name \"" + backup.getName() + "\"");
        getBackupSelectedListener().onBackupSelected(backup);
    }

    @Override
    public void onOptionClicked(int option, DatabaseBackup backup) {
        if (option == OPTION_DELETE)
            getBackupSelectedListener().onBackupDeleteClicked(backup);
        else if (option == OPTION_IMPORT)
            getBackupSelectedListener().onBackupImportClicked(backup);
        else
            Log.e(LOG_TAG,"onOptionClicked(): no valid option clicked");
    }
}
