package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class ImportExportViewModel extends AndroidViewModel {

    private EggManagerRepository repository;

    public ImportExportViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
    }

    public List<DailyBalance> getAllData() {
        return repository.getAllDataList();
    }

    public void insert(DailyBalance dailyBalance) { repository.insert(dailyBalance); }
}
