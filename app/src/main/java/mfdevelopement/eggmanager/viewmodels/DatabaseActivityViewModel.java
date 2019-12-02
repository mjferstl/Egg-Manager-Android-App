package mfdevelopement.eggmanager.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.FilterButtonHelper;
import mfdevelopement.eggmanager.database.EggManagerRepository;

import static mfdevelopement.eggmanager.dialog_fragments.FilterDialogFragment.NOT_SET_FILTER_STRING;

public class DatabaseActivityViewModel extends AndroidViewModel {

    private final String LOG_TAG = "DatabaseActivityViewMod";

    private EggManagerRepository mRepository;
    private LiveData<List<DailyBalance>> mAllDailyBalances;
    private LiveData<Integer> mNumberEggsSold, mTotalEggsCollected;
    private LiveData<Double> mMoneyEarned;
    private LiveData<List<String>> ldDateKeys;
    private String filterString = NOT_SET_FILTER_STRING;

    // Mutuable LiveData
    private MutableLiveData<FilterButtonHelper> liveButtonListener = new MutableLiveData<>();
    private MutableLiveData<Boolean> filterDialogOkClicked = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> filterDialogCancelClicked = new MutableLiveData<>(false);

    public DatabaseActivityViewModel(Application application) {
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
    public void delete(DailyBalance dailyBalance) {
        mRepository.delete(dailyBalance);
    }

    public LiveData<Integer> getTotalEggsSold() { return  mNumberEggsSold; }

    public LiveData<Double> getTotalMoneyEarned() { return mMoneyEarned; }

    public LiveData<Integer> getTotalEggsCollected() { return mTotalEggsCollected; }

    public List<DailyBalance> getDailyBalanceByDateKey(String dateKeyPattern) {
        return mRepository.getDailyBalancesByDateKey(dateKeyPattern);
    }

    public List<DailyBalance> getFilteredDailyBalances() {return getDailyBalanceByDateKey(filterString); }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys;}

    public void setFilterString(String string) {
        if (string.equals(NOT_SET_FILTER_STRING)) {
            resetFilterString();
        } else {
            filterString = string;
            mRepository.setDataFilter(string);
        }

        Log.d(LOG_TAG,"setFilterString::filterString = " + filterString);
        mAllDailyBalances = mRepository.getFilteredDailyBalance(filterString);
    }

    public void resetFilterString() {
        filterString = "";
        mRepository.setDataFilter("");
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterButtonListener(FilterButtonHelper filterButtonHelper) {
        filterString = filterButtonHelper.getFilterString();
        liveButtonListener.setValue(filterButtonHelper);
    }

    public LiveData<FilterButtonHelper> getLiveFilterButton() {
        return liveButtonListener;
    }


    public void setFilterDialogOkClicked(boolean isClicked) {
        filterDialogOkClicked.setValue(isClicked);
    }

    public LiveData<Boolean> getFilterDialogOkClicked() {
        return filterDialogOkClicked;
    }


    public void setFilterDialogCancelClicked(boolean isClicked) {
        filterDialogCancelClicked.setValue(isClicked);
    }

    public LiveData<Boolean> getFilterDialogCancelClicked() {
        return filterDialogCancelClicked;
    }
}
