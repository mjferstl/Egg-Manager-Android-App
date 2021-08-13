package mfdevelopement.eggmanager.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.List;

import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalanceDao;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalanceWorkerDataUtil;
import mfdevelopement.eggmanager.workmanager.CleanupDatabaseWorker;
import mfdevelopement.eggmanager.workmanager.DeleteDailyBalanceWorker;
import mfdevelopement.eggmanager.workmanager.InsertToDatabaseWorker;

public class EggManagerRepository {

    private SharedPreferences sharedPreferences;
    private final String PREFERENCE_FILE_KEY = "mfdevelopment.eggmanager.PREFERENCE_FILE_KEY";
    private final String KEY_PRICE_PER_EGG = "pricePerEgg";
    private final String KEY_FILTER_STRING = "filterString";
    private final String KEY_SORTING_ORDER = "sortingOrder";
    private final static String LOG_TAG = "EggManagerRepository";

    private final Application application;

    private final DailyBalanceDao dailyBalanceDao;
    private final LiveData<List<DailyBalance>> mAllData;
    private final LiveData<List<DailyBalance>> ldFilteredDailyBalance;
    private final LiveData<Integer> ldFilteredEggsSold;
    private final LiveData<Integer> ldFilteredEggsCollected;
    private final LiveData<Integer> ldEntriesCount;
    private final LiveData<Double> ldFilteredMoneyEarned;
    private final LiveData<List<String>> ldDateKeys;

    private final MutableLiveData<String> dateFilter = new MutableLiveData<>();

    public EggManagerRepository(Application application) {
        this.application = application;
        EggManagerRoomDatabase db = EggManagerRoomDatabase.getDatabase(application);
        dailyBalanceDao = db.dailyBalanceDao();
        mAllData = dailyBalanceDao.getAscendingItems();
        ldDateKeys = dailyBalanceDao.getDateKeysLiveData();
        ldEntriesCount = dailyBalanceDao.getRowCount();

        dateFilter.setValue(getDataFilter());
        ldFilteredDailyBalance = Transformations.switchMap(dateFilter, dailyBalanceDao::getFilteredDailyBalance);
        ldFilteredMoneyEarned = Transformations.switchMap(dateFilter, dailyBalanceDao::getFilteredMoneyEarned);
        ldFilteredEggsCollected = Transformations.switchMap(dateFilter, dailyBalanceDao::getFilteredEggsCollected);
        ldFilteredEggsSold = Transformations.switchMap(dateFilter, dailyBalanceDao::getFilteredEggsSold);
    }

    public LiveData<List<DailyBalance>> getAllDailyBalances() {
        return mAllData;
    }

    public LiveData<Integer> getEntriesCount() {
        return ldEntriesCount;
    }

    public void insert(DailyBalance dailyBalance) {
        Data inputData = DailyBalanceWorkerDataUtil.convertToData(dailyBalance);
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(InsertToDatabaseWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance(application.getBaseContext()).enqueue(workRequest);
    }

    public void delete(DailyBalance dailyBalance) {
        Data inputData = DailyBalanceWorkerDataUtil.convertToData(dailyBalance);
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(DeleteDailyBalanceWorker.class)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance(application.getBaseContext()).enqueue(workRequest);
    }

    public void deleteAll() {
        WorkRequest workRequest = new OneTimeWorkRequest.Builder(CleanupDatabaseWorker.class)
                .build();
        WorkManager.getInstance(application.getBaseContext()).enqueue(workRequest);
    }

    public LiveData<List<DailyBalance>> getFilteredDailyBalance(String dateFilter) {
        this.dateFilter.setValue(dateFilter);
        return ldFilteredDailyBalance;
    }

    public LiveData<Double> getFilteredMoneyEarned(String dateFilter) {
        this.dateFilter.setValue(dateFilter);
        return ldFilteredMoneyEarned;
    }

    public LiveData<Integer> getFilteredEggsSold(String dateFilter) {
        this.dateFilter.setValue(dateFilter);
        return ldFilteredEggsSold;
    }

    public LiveData<Integer> getFilteredEggsCollected(String dateFilter) {
        this.dateFilter.setValue(dateFilter);
        return ldFilteredEggsCollected;
    }

    public LiveData<List<String>> getDateKeys() {
        return ldDateKeys;
    }

    public LiveData<List<String>> getDateKeysList() {
        LiveData<List<String>> dateKeys;
        dateKeys = dailyBalanceDao.getDateKeysList();
        return dateKeys;
    }

    public void setDataFilter(String filterString) {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FILTER_STRING, filterString);
        editor.apply();
    }

    public String getDataFilter() {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_FILTER_STRING, "");
    }

    public void setSortingOrder(String sortingOrder) {
        if (sortingOrder.equals("ASC") || sortingOrder.equals("DESC")) {
            sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SORTING_ORDER, sortingOrder);
            editor.apply();
        } else {
            throw new IllegalArgumentException("The argument " + sortingOrder + " needs to be \"ASC\" or \"DESC\"");
        }
    }

    public String getSortingOrder() {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SORTING_ORDER, "ASC");
    }
}
