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
    private List<DailyBalance> filteredDailyBalanceList;
    private LiveData<List<String>> ldDateKeys;

    public DailyBalanceViewModel (Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);
        mAllDailyBalances = mRepository.getAllData();
        mNumberEggsSold = mRepository.getTotalEggsSold();
        mMoneyEarned = mRepository.getTotalMoneyEarned();
        mTotalEggsCollected = mRepository.getTotalEggsCollected();
        ldDateKeys = mRepository.getDateKeys();
    }

    LiveData<List<DailyBalance>> getAllDailyBalances() { return mAllDailyBalances; }

    public void insert(DailyBalance dailyBalance) { mRepository.insert(dailyBalance); }
    public void delete(DailyBalance dailyBalance) { mRepository.delete(dailyBalance);}

    LiveData<Integer> getTotalEggsSold() { return  mNumberEggsSold; }

    LiveData<Double> getTotalMoneyEarned() { return mMoneyEarned; }

    LiveData<Integer> getTotalEggsCollected() { return mTotalEggsCollected; }

    public List<DailyBalance> getFilteredDailyBalanceList(String dateKeyPattern) {
        filteredDailyBalanceList = mRepository.getDailyBalancesByDateKey(dateKeyPattern);
        return  filteredDailyBalanceList;
    }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys;}
}
