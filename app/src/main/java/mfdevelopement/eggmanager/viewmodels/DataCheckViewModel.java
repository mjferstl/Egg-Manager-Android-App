package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mfdevelopement.eggmanager.database.EggManagerRepository;

public class DataCheckViewModel extends AndroidViewModel {

    // String for assignment when printing out logs
    private final String LOG_TAG = "DataCheckViewModel";

    // Repository
    private final EggManagerRepository repository;

    private final LiveData<List<String>> allDateKeys;

    private List<Date> storedMissingDates = new ArrayList<>();

    public DataCheckViewModel(Application application) {
        super(application);

        repository = new EggManagerRepository(application);

        allDateKeys = repository.getDateKeys();
    }

    public LiveData<List<String>> getAllDateKeys() {
        return this.allDateKeys;
    }

    public void storeMissingDates(List<Date> missingDates) {
        this.storedMissingDates = missingDates;
    }

    public List<Date> getStoredMissingDates() {
        return this.storedMissingDates;
    }
}
