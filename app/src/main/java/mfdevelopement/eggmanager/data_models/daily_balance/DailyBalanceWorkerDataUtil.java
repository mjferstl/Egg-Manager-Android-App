package mfdevelopement.eggmanager.data_models.daily_balance;

import android.util.Log;

import androidx.work.Data;

import java.util.Date;

import mfdevelopement.eggmanager.utils.type_converter.DateTypeConverter;

public class DailyBalanceWorkerDataUtil {

    private static final String LOG_TAG = "DailyBalanceWorkerDataU";

    private static final String KEY_DATE_PRIMARY = "dateKey";
    private static final String KEY_DATE = "date";
    private static final String KEY_EGGS_COLLECTED = "eggsCollected";
    private static final String KEY_EGGS_SOLD = "eggsSold";
    private static final String KEY_PRICE_PER_EGG = "pricePerEgg";
    private static final String KEY_NUMBER_HENS = "numberOfHens";
    private static final String KEY_MONEY_EARNED = "moneyEarned";
    private static final String KEY_DATE_CREATED = "dateCreated";
    private static final String KEY_USER_CREATED = "userCreated";

    /**
     * Convert a {@link DailyBalance} object to a {@link Data} object for use with {@link androidx.work.WorkManager}.
     * For backwards conversion see {@link #convertFromData}
     *
     * @param dailyBalance {@link DailyBalance} object to convert
     * @return The {@link Data} object with key value pairs storing the data of the {@link DailyBalance} object
     * @see #convertFromData
     */
    public static Data convertToData(DailyBalance dailyBalance) {
        return new Data.Builder()
                .putString(KEY_DATE_PRIMARY, dailyBalance.getDateKey())
                .putLong(KEY_DATE, DateTypeConverter.dateToTimestamp(dailyBalance.getDate()))
                .putInt(KEY_EGGS_COLLECTED, dailyBalance.getEggsCollected())
                .putInt(KEY_EGGS_SOLD, dailyBalance.getEggsSold())
                .putDouble(KEY_PRICE_PER_EGG, dailyBalance.getPricePerEgg())
                .putInt(KEY_NUMBER_HENS, dailyBalance.getNumHens())
                .putDouble(KEY_MONEY_EARNED, dailyBalance.getMoneyEarned())
                .putLong(KEY_DATE_CREATED, DateTypeConverter.dateToTimestamp(dailyBalance.getDateCreated()))
                .putString(KEY_USER_CREATED, dailyBalance.getUserCreated())
                .build();
    }

    /**
     * Convert a {@link Data} object to a {@link DailyBalance} object
     *
     * @param data {@link Data} object which contains key-value pairs containing the information of a {@link DailyBalance}
     * @return The {@link DailyBalance} object, which has been created with the data
     * @see #convertToData
     */
    public static DailyBalance convertFromData(Data data) {

        String dateKey = data.getString(KEY_DATE_PRIMARY);
        if (dateKey == null) {
            Log.e(LOG_TAG, "convertFromData(): dateKey is null");
            return null;
        }
        Date date = DateTypeConverter.fromTimestamp(data.getLong(KEY_DATE, 0));
        int eggsCollected = data.getInt(KEY_EGGS_COLLECTED, 0);
        int eggsSold = data.getInt(KEY_EGGS_SOLD, 0);
        double pricePerEgg = data.getDouble(KEY_PRICE_PER_EGG, 0);
        int numHens = data.getInt(KEY_NUMBER_HENS, 0);
        //double moneyEarned = data.getDouble(KEY_MONEY_EARNED, eggsCollected*pricePerEgg);
        Date dateCreated = DateTypeConverter.fromTimestamp(data.getLong(KEY_DATE_CREATED, 0));
        String userCreated = data.getString(KEY_USER_CREATED);

        return new DailyBalance(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens, dateCreated, userCreated, date);
    }
}
