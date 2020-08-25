package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import java.util.List;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class NewEntityViewModel extends AndroidViewModel {

    private final String LOG_TAG = "NewEntityViewModel";

    private EggManagerRepository repository;
    private LiveData<List<String>> ldDateKeys;

    public NewEntityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        ldDateKeys = repository.getDateKeys();
    }

    /**
     * Load the price per egg from the preferences
     *
     * @return price per egg
     */
    public double getPricePerEgg() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String stringValue = sharedPreferences.getString(getApplication().getString(R.string.preferences_key_pricePerEgg_cent), getApplication().getString(R.string.preferences_pricePerEgg_cent_default));
        double value = Double.parseDouble(stringValue) / 100;
        Log.d(LOG_TAG, String.format("loaded price per egg from preferences: %f", value));
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
