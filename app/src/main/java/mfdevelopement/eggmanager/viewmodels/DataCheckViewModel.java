package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import mfdevelopement.eggmanager.database.EggManagerRepository;

public class DataCheckViewModel extends AndroidViewModel {

    // String for assignment when printing out logs
    private final String LOG_TAG = "DataCheckViewModel";

    // Repository
    private EggManagerRepository repository;

    private LiveData<List<String>> allDateKeys;

    public DataCheckViewModel(Application application) {
        super(application);

        repository = new EggManagerRepository(application);

        allDateKeys = repository.getDateKeys();
    }

    public LiveData<List<String>> getAllDateKeys() {
        return this.allDateKeys;
    }
}
