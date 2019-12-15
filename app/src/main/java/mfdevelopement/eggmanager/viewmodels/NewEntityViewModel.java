package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class NewEntityViewModel extends AndroidViewModel {

    private EggManagerRepository repository;
    private double pricePerEgg;
    private LiveData<List<DailyBalance>> mAllData;
    private LiveData<List<String>> ldDateKeys;

    public NewEntityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        pricePerEgg = repository.getPricePerEgg();
        mAllData = repository.getAllDailyBalances();
        ldDateKeys = repository.getDateKeys();
    }

    public double getPricePerEgg() {
        return this.pricePerEgg;
    }

    public void addDailyBalance(DailyBalance dailyBalance) {
        pricePerEgg = dailyBalance.getPricePerEgg();
        repository.insert(dailyBalance);
    }

    public void deleteDailyBalance(DailyBalance dailyBalance) {
        repository.delete(dailyBalance);
    }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys;}
}
