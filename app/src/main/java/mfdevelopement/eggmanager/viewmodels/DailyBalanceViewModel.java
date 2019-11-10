package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;
import mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment;

public class DailyBalanceViewModel extends AndroidViewModel {

    private final String LOG_TAG = "DailyBalanceViewModel";

    private EggManagerRepository mRepository;
    private LiveData<List<DailyBalance>> mAllDailyBalances;
    private LiveData<Integer> mNumberEggsSold, mTotalEggsCollected;
    private LiveData<Double> mMoneyEarned;
    private List<DailyBalance> filteredDailyBalanceList;
    private LiveData<List<String>> ldDateKeys;
    private String filterString = FilterDialogFragment.NOT_SET_FILTER_STRING;

    public DailyBalanceViewModel (Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);
        mAllDailyBalances = mRepository.getFilteredDailyBalance(filterString);
        mNumberEggsSold = mRepository.getTotalEggsSold();
        mMoneyEarned = mRepository.getTotalMoneyEarned();
        mTotalEggsCollected = mRepository.getTotalEggsCollected();
        ldDateKeys = mRepository.getDateKeys();
    }

    public LiveData<List<DailyBalance>> getAllDailyBalances() { return mAllDailyBalances; }

    public void insert(DailyBalance dailyBalance) { mRepository.insert(dailyBalance); }
    public void delete(DailyBalance dailyBalance) { mRepository.delete(dailyBalance);}

    public LiveData<Integer> getTotalEggsSold() { return  mNumberEggsSold; }

    public LiveData<Double> getTotalMoneyEarned() { return mMoneyEarned; }

    public LiveData<Integer> getTotalEggsCollected() { return mTotalEggsCollected; }

    public List<DailyBalance> getDailyBalanceByDateKey(String dateKeyPattern) {
        filteredDailyBalanceList = mRepository.getDailyBalancesByDateKey(dateKeyPattern);
        return  filteredDailyBalanceList;
    }

    public List<DailyBalance> getFilteredDailyBalances() {return getDailyBalanceByDateKey(filterString); }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys;}

    public void setFilterString(String string) {
        filterString = string;
        Log.d(LOG_TAG,"setFilterString::filterString = " + string);
        mAllDailyBalances = mRepository.getFilteredDailyBalance(filterString);
    }

    public void resetFilterString() { filterString = ""; }

    public String getFilterString() {
        return filterString;
    }
}
