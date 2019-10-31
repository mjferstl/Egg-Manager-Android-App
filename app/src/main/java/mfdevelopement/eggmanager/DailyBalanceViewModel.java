package mfdevelopement.eggmanager;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DailyBalanceViewModel extends AndroidViewModel {

    private final String LOG_TAG = "DailyBalanceViewModel";

    private EggManagerRepository mRepository;
    private LiveData<List<DailyBalance>> mAllDailyBalances;
    private LiveData<Integer> mNumberEggsSold, mTotalEggsCollected;
    private LiveData<Double> mMoneyEarned;

    public DailyBalanceViewModel (Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);
        mAllDailyBalances = mRepository.getAllData();
        mNumberEggsSold = mRepository.getTotalEggsSold();
        mMoneyEarned = mRepository.getTotalMoneyEarned();
        mTotalEggsCollected = mRepository.getTotalEggsCollected();
    }

    LiveData<List<DailyBalance>> getAllDailyBalances() { return mAllDailyBalances; }

    public void insert(DailyBalance dailyBalance) { mRepository.insert(dailyBalance); }
    public void delete(DailyBalance dailyBalance) { mRepository.delete(dailyBalance);}

    LiveData<Integer> getTotalEggsSold() { return  mNumberEggsSold; }

    LiveData<Double> getTotalMoneyEarned() { return mMoneyEarned; }

    LiveData<Integer> getTotalEggsCollected() { return mTotalEggsCollected; }
}
