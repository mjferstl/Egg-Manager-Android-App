package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class DatabaseImportExportViewModel extends AndroidViewModel {

    private EggManagerRepository repository;

    public DatabaseImportExportViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
    }

    public List<DailyBalance> getAllData() {
        return repository.getAllDataList();
    }

    public void insert(DailyBalance dailyBalance) { repository.insert(dailyBalance); }
}
