package mfdevelopement.eggmanager;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DailyBalanceViewModel extends AndroidViewModel {

    private EggManagerRepository mRepository;
    private LiveData<List<DailyBalance>> mAllDailyBalances;

    public DailyBalanceViewModel (Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);
        mAllDailyBalances = mRepository.getAllData();
    }

    LiveData<List<DailyBalance>> getAllWords() { return mAllDailyBalances; }

    public void insert(DailyBalance dailyBalance) { mRepository.insert(dailyBalance); }
    public void delete(DailyBalance dailyBalance) { mRepository.delete(dailyBalance);}
}
