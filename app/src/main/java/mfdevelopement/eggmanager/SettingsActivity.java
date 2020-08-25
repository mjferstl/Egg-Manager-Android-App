package mfdevelopement.eggmanager;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import mfdevelopement.eggmanager.viewmodels.NewEntityViewModel;

public class SettingsActivity extends AppCompatActivity {

    private final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final String LOG_TAG = "SettingsFragment";

        private NewEntityViewModel viewModel;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // initialize the view model
            viewModel = new ViewModelProvider(this).get(NewEntityViewModel.class);

            /*
             * Add functionality to the preference field, which specifies the price per egg
             */
            EditTextPreference editTextPricePerEgg = findPreference(getString(R.string.preferences_key_pricePerEgg));
            if (editTextPricePerEgg != null) {
                // change the keyboard layout to make the user input a number
                editTextPricePerEgg.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL));

                // action when a new value has been set for the preference
                editTextPricePerEgg.setOnPreferenceChangeListener((preference, newValue) -> {
                    Log.d(LOG_TAG, String.format("New value \"%s\" set for preference \"%s\"", newValue.toString(), preference.getTitle().toString()));

                    // check if the new value is a numeric value
                    double newPricePerEgg = Double.parseDouble(newValue.toString());
                    savePricePerEgg(newPricePerEgg);
                    return true;
                });
            } else {
                Log.d(LOG_TAG, "The EditTextPreference is null. The field may not work as expected.");
            }

        }

        private void savePricePerEgg(double value) {
            viewModel.setPricePerEgg(value/100);
            Log.d(LOG_TAG, String.format("Saved new value for price per egg: %f cents", value));
        }
    }
}