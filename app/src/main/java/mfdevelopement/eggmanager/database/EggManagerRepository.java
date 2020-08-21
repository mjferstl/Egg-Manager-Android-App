package mfdevelopement.eggmanager.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DailyBalanceDao;

public class EggManagerRepository {

    private SharedPreferences sharedPreferences;
    private final String PREFERENCE_FILE_KEY = "mfdevelopment.eggmanager.PREFERENCE_FILE_KEY";
    private final String KEY_PRICE_PER_EGG = "pricePerEgg";
    private final String KEY_FILTER_STRING = "filterString";
    private final String KEY_SORTING_ORDER = "sortingOrder";
    private final static String LOG_TAG = "EggManagerRepository";

    private Application application;

    private DailyBalanceDao dailyBalanceDao;
    private LiveData<List<DailyBalance>> mAllData, ldFilteredDailyBalance;
    private LiveData<Integer> ldFilteredEggsSold, ldFilteredEggsCollected, ldEntriesCount;
    private LiveData<Double> ldFilteredMoneyEarned;
    private LiveData<List<String>> ldDateKeys;

    private MutableLiveData<String> dateFilter = new MutableLiveData<>();

    public EggManagerRepository(Application application){
        this.application = application;
        EggManagerRoomDatabase db = EggManagerRoomDatabase.getDatabase(application);
        dailyBalanceDao = db.dailyBalanceDao();
        mAllData = dailyBalanceDao.getAscendingItems();
        ldDateKeys = dailyBalanceDao.getDateKeysLiveData();
        ldEntriesCount = dailyBalanceDao.getRowCount();

        dateFilter.setValue(getDataFilter());
        ldFilteredDailyBalance = Transformations.switchMap(dateFilter, filter -> dailyBalanceDao.getFilteredDailyBalance(filter));
        ldFilteredMoneyEarned = Transformations.switchMap(dateFilter, filter -> dailyBalanceDao.getFilteredMoneyEarned(filter));
        ldFilteredEggsCollected = Transformations.switchMap(dateFilter, filter -> dailyBalanceDao.getFilteredEggsCollected(filter));
        ldFilteredEggsSold = Transformations.switchMap(dateFilter, filter -> dailyBalanceDao.getFilteredEggsSold(filter));
    }

    public LiveData<List<DailyBalance>> getAllDailyBalances() {
        return mAllData;
    }

    public LiveData<Integer> getEntriesCount() {
        return ldEntriesCount;
    }

    public List<DailyBalance> getAllDataList() {
        List<DailyBalance> dailyBalances;

        try {
            dailyBalances = new getAllData(dailyBalanceDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"getAllDataList::ExecutionException");
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"getAllDataList::InterruptedException");
            return null;
        }

        return dailyBalances;
    }

    public void insert(DailyBalance dailyBalance) {
        setPricePerEgg(dailyBalance.getPricePerEgg());
        new insertAsyncTask(dailyBalanceDao).execute(dailyBalance);
    }

    public void delete(DailyBalance dailyBalance) {
        new deleteAsyncTask(dailyBalanceDao).execute(dailyBalance);
    }

    public void deleteAll() {
        new deleteAllAsyncTask(dailyBalanceDao).execute();
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

    public LiveData<List<String>> getDateKeys() { return ldDateKeys; }

    public List<String> getDateKeysList() {
        List<String> dateKeys = new ArrayList<>();
        try {
            dateKeys = new getAllDateKeys(dailyBalanceDao).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return dateKeys;
    }

    public List<DailyBalance> getDailyBalancesByDateKey(String dateKeyPattern) {
        List<DailyBalance> dailyBalanceList = new ArrayList<>();

        try {
            dailyBalanceList = new getDailyBalanceByDateKeyAsyncTask(dailyBalanceDao).execute(dateKeyPattern).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return dailyBalanceList;
    }

    private static class insertAsyncTask extends AsyncTask<DailyBalance, Void, Void> {

        private DailyBalanceDao mAsyncTaskDao;

        insertAsyncTask(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DailyBalance... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<DailyBalance, Void, Void> {

        private DailyBalanceDao mAsyncTaskDao;

        deleteAsyncTask(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final DailyBalance... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    /**
     * Async Task to deere
     */
    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private DailyBalanceDao mAsyncTaskDao;

        deleteAllAsyncTask(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(LOG_TAG, "Starting to delete all items");
            mAsyncTaskDao.deleteAll();
            Log.d(LOG_TAG, "All items deleted.");
            return null;
        }
    }

    private static class getDailyBalanceByDateKeyAsyncTask extends AsyncTask<String, Void, List<DailyBalance>> {

        private DailyBalanceDao mAsyncTaskDao;

        getDailyBalanceByDateKeyAsyncTask(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<DailyBalance> doInBackground(final String... params) {
            return mAsyncTaskDao.getDailyBalancesByDateKey(params[0]);
        }
    }

    private static class getAllData extends AsyncTask<Void, Void, List<DailyBalance>> {

        private DailyBalanceDao mAsyncTaskDao;

        getAllData(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<DailyBalance> doInBackground(Void... voids) {
            return mAsyncTaskDao.getAscendingItemsList();
        }
    }

    private static class getAllDateKeys extends AsyncTask<Void, Void, List<String>> {

        private DailyBalanceDao mAsyncTaskDao;

        getAllDateKeys(DailyBalanceDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return mAsyncTaskDao.getDateKeysList();
        }
    }

    public double getPricePerEgg() {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_PRICE_PER_EGG, 0);
    }

    public void setPricePerEgg(double pricePerEgg) {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(KEY_PRICE_PER_EGG, (float) pricePerEgg);
        editor.apply();
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
