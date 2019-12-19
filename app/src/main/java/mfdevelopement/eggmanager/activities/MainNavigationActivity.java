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
import mfdevelopement.eggmanager.dialog_fragments.SortingDialogFragment;
import mfdevelopement.eggmanager.viewmodels.DatabaseActivityViewModel;

public class MainNavigationActivity extends AppCompatActivity implements SortingDialogFragment.OnSortingItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private final String LOG_TAG = "MainNavigationActivity";
    private NavController navController;
    private DatabaseActivityViewModel viewModel;

    public SortingOrderChangedListener sortingOrderChangedListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"starting activity MainNavigationActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(DatabaseActivityViewModel.class);

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
}
