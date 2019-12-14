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
import mfdevelopement.eggmanager.data_models.FilterButtonHelper;
import mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment;
import mfdevelopement.eggmanager.list_adapters.FilterDialogListAdapter;
import mfdevelopement.eggmanager.viewmodels.DatabaseActivityViewModel;

import static mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment.NOT_SET_FILTER_STRING;

public class MainNavigationActivity extends AppCompatActivity implements FilterDialogFragment.OnFilterDialogClickListener, FilterDialogListAdapter.OnFilterSelectListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DatabaseActivityViewModel viewModel;
    private final String LOG_TAG = "MainNavigationActivity";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public void onFilterDialogOkClicked(String filterString) {
        Log.d(LOG_TAG,"onFilterDialogOkClicked: filterString = " + filterString);
        if (filterString.equals(NOT_SET_FILTER_STRING)) {
            viewModel.setFilterString("");
        } else {
            viewModel.setFilterString(filterString);
        }

        viewModel.setFilterDialogOkClicked(true);
    }

    @Override
    public void onFilterDialogCancelClicked() {
        viewModel.setFilterDialogCancelClicked(true);
    }

    @Override
    public void onFilterSelected(String filterString, int buttonPosition, boolean isSelected) {
        FilterButtonHelper fbh = new FilterButtonHelper(filterString,buttonPosition,isSelected);

        Log.d(LOG_TAG,"Button with the filterString " + filterString + " at position " + buttonPosition + " has been selected: " + isSelected);

        // override the filterString, if the button has been unselected
        if (!isSelected) {
            if (filterString.length() == 4) {
                fbh.setFilterString(NOT_SET_FILTER_STRING);
            } else if (filterString.length() == 6) {
                fbh.setFilterString(filterString.substring(0,4));
            }
        }
        viewModel.setFilterButtonListener(fbh);
    }
}
