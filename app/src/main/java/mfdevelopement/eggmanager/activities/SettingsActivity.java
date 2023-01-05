package mfdevelopement.eggmanager.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.utils.notifications.DatabaseBackupImportNotificationManager;

public class SettingsActivity extends AppCompatActivity {

    //private final String LOG_TAG = "SettingsActivity";

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


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            /*
             * Add functionality to the preference field, which specifies the price per egg
             */
            EditTextPreference editTextPricePerEgg = findPreference(getString(R.string.preferences_key_pricePerEgg_cent));
            if (editTextPricePerEgg != null) {
                // change the keyboard layout to make the user input a number
                editTextPricePerEgg.setOnBindEditTextListener(editText -> {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editText.setSelection(editText.getText().length());
                });

                // action when a new value has been set for the preference
                editTextPricePerEgg.setOnPreferenceChangeListener((preference, newValue) -> {
                    Log.d(LOG_TAG, String.format("New value \"%s\" set for preference \"%s\"", newValue.toString(), preference.getTitle().toString()));

                    // check if the new value is a numeric value
                    double newPricePerEgg = Double.parseDouble(newValue.toString());
                    Log.d(LOG_TAG, String.format("Saved new value for price per egg: %f cents", newPricePerEgg));
                    return true;
                });
            } else {
                Log.d(LOG_TAG, "The EditTextPreference is null. The field may not work as expected.");
            }

            Preference openNotificationSettings = findPreference(getString(R.string.preference_key_backup_import_notifications));
            if (openNotificationSettings != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getContext() != null) {

                    openNotificationSettings.setOnPreferenceClickListener(preference -> {
                        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, DatabaseBackupImportNotificationManager.CHANNEL_ID);
                        startActivity(intent);
                        return true;
                    });
                } else {
                    openNotificationSettings.setEnabled(false);
                }
            }
        }
    }
}