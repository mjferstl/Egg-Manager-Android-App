package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Arrays;
import java.util.List;

import mfdevelopement.eggmanager.FilterStringHandle;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class DatabaseActivityViewModel extends AndroidViewModel implements FilterStringHandle {

    private final String LOG_TAG = "DatabaseActivityViewMod";
    private final String NOT_SET_FILTER_STRING = "0000";

    private EggManagerRepository mRepository;
    private LiveData<List<DailyBalance>> mAllDailyBalances;
    private LiveData<Integer> mNumberEggsSold, mTotalEggsCollected;
    private LiveData<Double> mMoneyEarned;
    private LiveData<List<String>> ldDateKeys;
    private String filterString = NOT_SET_FILTER_STRING;
    private List<String> monthNamesReference;

    public DatabaseActivityViewModel(Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);
        mAllDailyBalances = mRepository.getFilteredDailyBalance(filterString);
        mNumberEggsSold = mRepository.getTotalEggsSold();
        mMoneyEarned = mRepository.getTotalMoneyEarned();
        mTotalEggsCollected = mRepository.getTotalEggsCollected();
        ldDateKeys = mRepository.getDateKeys();
        monthNamesReference = Arrays.asList(getApplication().getResources().getStringArray(R.array.month_names));
    }

    public LiveData<List<DailyBalance>> getAllDailyBalances() { return mAllDailyBalances; }

    public void insert(DailyBalance dailyBalance) { mRepository.insert(dailyBalance); }
    public void delete(DailyBalance dailyBalance) {
        mRepository.delete(dailyBalance);
    }

    public LiveData<Integer> getTotalEggsSold() { return  mNumberEggsSold; }

    public LiveData<Double> getTotalMoneyEarned() { return mMoneyEarned; }

    public LiveData<Integer> getTotalEggsCollected() { return mTotalEggsCollected; }

    public List<DailyBalance> getDailyBalanceByDateKey(String dateKeyPattern) {
        return mRepository.getDailyBalancesByDateKey(dateKeyPattern);
    }

    public List<DailyBalance> getFilteredDailyBalances() {
        return getDailyBalanceByDateKey(mRepository.getDataFilter());
    }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys;}

    public String getFilterString() {
        return mRepository.getDataFilter();
    }

    /**
     * get the name of a month by its index in the range 1..12
     * @param index ranges from 1 to 12
     * @return Name of the month (January .. December)
     */
    public String getMonthNameByIndex(int index) {
        return monthNamesReference.get(index-1);
    }
}
