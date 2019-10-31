package mfdevelopement.eggmanager;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class NewEntityViewModel extends AndroidViewModel {

    private EggManagerRepository repository;
    private double pricePerEgg;
    private LiveData<List<DailyBalance>> mAllData;


    public NewEntityViewModel(Application application) {
        super(application);
        repository = new EggManagerRepository(application);
        pricePerEgg = repository.getPricePerEgg();
        mAllData = repository.getAllData();

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


}
