package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class NewEntityViewModel extends AndroidViewModel {

    private final String LOG_TAG = "NewEntityViewModel";

    private final EggManagerRepository repository;
    private final LiveData<List<String>> ldDateKeys;

    private final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());

    public NewEntityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        ldDateKeys = repository.getDateKeys();
    }

    /**
     * Load the username from the apps preferences
     *
     * @return username
     */
    public String getUsername() {
        String keyUsername = getApplication().getString(R.string.preferences_key_username);
        String defaultUsername = getApplication().getString(R.string.preferences_username_default);
        String username = sharedPreferences.getString(keyUsername, defaultUsername);
        Log.d(LOG_TAG, String.format("loaded username from preferences: %s", username));
        return username;
    }

    /**
     * Load the price per egg from the preferences
     *
     * @return price per egg
     */
    public double getPricePerEgg() {
        String keyPricePerEgg = getApplication().getString(R.string.preferences_key_pricePerEgg_cent);
        String defaultPricePerEgg = getApplication().getString(R.string.preferences_pricePerEgg_cent_default);
        String stringValue = sharedPreferences.getString(keyPricePerEgg, defaultPricePerEgg);
        double value = Double.parseDouble(stringValue) / 100;
        Log.d(LOG_TAG, String.format("loaded price per egg in Euro from preferences: %f", value));
        return value;
    }

    /**
     * Add a Daily Balance to the database
     *
     * @param dailyBalance DailyBalance
     */
    public void addDailyBalance(DailyBalance dailyBalance) {
        repository.insert(dailyBalance);
    }

    /**
     * Delete a specific daily balance from the database
     *
     * @param dailyBalance Daily Balance to be deleted
     */
    public void deleteDailyBalance(DailyBalance dailyBalance) {
        repository.delete(dailyBalance);
    }

    public LiveData<List<String>> getDateKeys() {
        return ldDateKeys;
    }
}
