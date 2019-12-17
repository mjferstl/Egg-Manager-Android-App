package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Arrays;
import java.util.List;

import mfdevelopement.eggmanager.FilterStringHandle;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.database.EggManagerRepository;

public class DatabaseActivityViewModel extends AndroidViewModel implements FilterStringHandle {

    private final String LOG_TAG = "DatabaseActivityViewMod";

    private EggManagerRepository mRepository;
    private LiveData<Integer> ldFilteredEggsCollected, ldFilteredEggsSold;
    private LiveData<Double> ldFilteredMoneyEarned;
    private List<String> monthNamesReference;
    private LiveData<List<DailyBalance>> filteredDailyBalances;

    private MutableLiveData<String> dataFilter = new MutableLiveData<>();

    public DatabaseActivityViewModel(Application application) {
        super(application);
        mRepository = new EggManagerRepository(application);

        // load month names
        monthNamesReference = Arrays.asList(getApplication().getResources().getStringArray(R.array.month_names));

        dataFilter.setValue(mRepository.getDataFilter());
        filteredDailyBalances = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredDailyBalance(filter));
        ldFilteredMoneyEarned = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredMoneyEarned(filter));
        ldFilteredEggsCollected = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredEggsCollected(filter));
        ldFilteredEggsSold = Transformations.switchMap(dataFilter, filter -> mRepository.getFilteredEggsSold(filter));
    }

    public LiveData<List<DailyBalance>> getFilteredDailyBalance() {
        return filteredDailyBalances;
    }

    public LiveData<Double> getFilteredMoneyEarned() {
        return ldFilteredMoneyEarned;
    }

    public LiveData<Integer> getFilteredEggsCollected() {
        return ldFilteredEggsCollected;
    }

    public LiveData<Integer> getFilteredEggsSold() {
        return ldFilteredEggsSold;
    }

    public void setDateFilter(String dateFilter) {
        Log.d(LOG_TAG,"setDateFilter(): new filter \"" + dateFilter + "\" applied");
        dataFilter.setValue(dateFilter);
    }

    public String getDateFilter() {
        return this.dataFilter.getValue();
    }

    public void insert(DailyBalance dailyBalance) {
        mRepository.insert(dailyBalance);
    }

    public void delete(DailyBalance dailyBalance) {
        mRepository.delete(dailyBalance);
    }

    public String loadDateFilter() {
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
