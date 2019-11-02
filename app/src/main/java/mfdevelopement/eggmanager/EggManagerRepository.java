package mfdevelopement.eggmanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class EggManagerRepository {

    private SharedPreferences sharedPreferences;
    private final String PREFERENCE_FILE_KEY = "mfdevelopment.eggmanager.PREFERENCE_FILE_KEY";
    private final String PRICE_PER_EGG = "pricePerEgg";
    private double pricePerEgg;

    private Application application;

    private DailyBalanceDao dailyBalanceDao;
    private LiveData<List<DailyBalance>> mAllData;
    private LiveData<Integer> ldTotalEggsSold, ldTotalEggsCollected;
    private LiveData<Double> ldTotalMoneyEarned;
    private LiveData<List<String>> ldDateKeys;

    EggManagerRepository(Application application) {
        this.application = application;
        EggManagerRoomDatabase db = EggManagerRoomDatabase.getDatabase(application);
        dailyBalanceDao = db.dailyBalanceDao();
        mAllData = dailyBalanceDao.getAscendingItems();
        ldTotalEggsSold = dailyBalanceDao.getTotalEggsSold();
        ldTotalMoneyEarned = dailyBalanceDao.getTotalMoneyEarned();
        ldTotalEggsCollected = dailyBalanceDao.getTotalEggsCollected();
        ldDateKeys = dailyBalanceDao.getDateKeys();

        pricePerEgg = getPricePerEgg();
    }

    LiveData<List<DailyBalance>> getAllData() {
        return mAllData;
    }

    public void insert(DailyBalance dailyBalance) {
        setPricePerEgg(dailyBalance.getPricePerEgg());
        new insertAsyncTask(dailyBalanceDao).execute(dailyBalance);
    }

    public void delete(DailyBalance dailyBalance) {
        new deleteAsyncTask(dailyBalanceDao).execute(dailyBalance);
    }

    public LiveData<Integer> getTotalEggsSold() {
        return ldTotalEggsSold;
    }

    public LiveData<Double> getTotalMoneyEarned() {
        return ldTotalMoneyEarned;
    }

    public LiveData<Integer> getTotalEggsCollected() {
        return ldTotalEggsCollected;
    }

    public LiveData<List<String>> getDateKeys() { return ldDateKeys; }

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

    public double getPricePerEgg() {
        sharedPreferences = application.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(PRICE_PER_EGG, 0);
    }

    public void setPricePerEgg(double pricePerEgg) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(PRICE_PER_EGG, (float) pricePerEgg);
        editor.apply();
    }
}
