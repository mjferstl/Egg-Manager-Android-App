package mfdevelopement.eggmanager.data_models.daily_balance;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.data_models.HasDateInterface;
import mfdevelopement.eggmanager.utils.type_converter.DateTypeConverter;

@Entity(tableName = "dailyBalanceTable")
public class DailyBalance implements Serializable, Comparable<DailyBalance>, HasDateInterface {

    public static final String COL_DATE_PRIMARY_KEY = "dateKey";
    public static final String COL_DATE_KEY = "date";
    public static final String COL_EGGS_COLLECTED_NAME = "eggsCollected";
    public static final String COL_EGGS_SOLD_NAME = "eggsSold";
    public static final String COL_PRICE_PER_EGG = "pricePerEgg";
    public static final String COL_NUMBER_HENS = "numberOfHens";
    public static final String COL_MONEY_EARNED = "moneyEarned";
    public static final String COL_DATE_CREATED = "dateCreated";
    public static final String COL_USER_CREATED = "userCreated";
    public static final int NOT_SET = 0;

    private static final String LOG_TAG = "DailyBalance";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = COL_DATE_PRIMARY_KEY)
    private String dateKey;

    @ColumnInfo(name = COL_DATE_KEY)
    private Date date;

    @ColumnInfo(name = COL_EGGS_COLLECTED_NAME, defaultValue = "0")
    private final int eggsCollected;

    @ColumnInfo(name = COL_EGGS_SOLD_NAME, defaultValue = "0")
    private final int eggsSold;

    @ColumnInfo(name = COL_PRICE_PER_EGG, defaultValue = "1.0")
    private final double pricePerEgg;

    @ColumnInfo(name = COL_MONEY_EARNED, defaultValue = "0.0")
    private double moneyEarned;

    @ColumnInfo(name = COL_NUMBER_HENS, defaultValue = "0")
    private int numHens;

    @TypeConverters(DateTypeConverter.class)
    @ColumnInfo(name = COL_DATE_CREATED)
    private Date dateCreated;

    @ColumnInfo(name = COL_USER_CREATED)
    private String userCreated;

    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg) {
        this(dateKey, eggsCollected, eggsSold, pricePerEgg, 0);
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens) {
        this(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens, null);
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens, Date dateCreated) {
        this(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens, dateCreated, null);
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens, Date dateCreated, String userCreated) {
        setDateKey(dateKey);
        setDateByDateKey();
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        setNumHens(numHens);
        setMoneyEarned(calcMoneyEarned(eggsSold, pricePerEgg));

        if (dateCreated != null) setDateCreated(dateCreated);
        else setDateCreated(getCurrentDate());

        if (userCreated != null) setUserCreated(userCreated);
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens, Date dateCreated, String userCreated, Date date) {
        setDateKey(dateKey);
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        setNumHens(numHens);
        setMoneyEarned(calcMoneyEarned(eggsSold, pricePerEgg));

        if (dateCreated != null) setDateCreated(dateCreated);
        else setDateCreated(getCurrentDate());

        if (userCreated != null) setUserCreated(userCreated);

        this.date = date;
    }

    private double calcMoneyEarned(int eggsSold, double pricePerEgg) {
        // calculate the earned money
        if (eggsSold != 0 && pricePerEgg != 0)
            return eggsSold * pricePerEgg;
        else
            return 0;
    }

    @NonNull
    public String getDateKey() {
        return this.dateKey;
    }

    private void setDateKey(String dateKey) {
        if (dateKey == null || dateKey.length() != DateKeyUtils.DATE_KEY_FORMAT.length()) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DateKeyUtils.DATE_KEY_FORMAT, Locale.getDefault());
            this.dateKey = sdf.format(cal.getTimeInMillis());
        } else {
            this.dateKey = dateKey;
        }
    }

    private void setDateByDateKey() {
        // newer way of solving things
        try {
            this.date = DateKeyUtils.getDateByDateKey(dateKey);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Could not parse the dateKey " + dateKey + " to a date");
        }
    }

    public void setDate(@Nullable Date date) {
        this.date = date;
    }

    public int getEggsCollected() {
        return this.eggsCollected;
    }

    public int getEggsSold() {
        return this.eggsSold;
    }

    public double getPricePerEgg() {
        return this.pricePerEgg;
    }

    public Date getDate() {
        if (this.date != null) return this.date;

        try {
            this.date = DateKeyUtils.getDateByDateKey(this.dateKey);
            return this.date;
        } catch (ParseException e) {
            return null;
        }
    }

    protected Date getDateRaw() {
        return this.date;
    }

    public int getNumHens() {
        return this.numHens;
    }

    public void setNumHens(int numHens) {
        this.numHens = numHens;
    }

    public double getMoneyEarned() {
        return this.moneyEarned;
    }

    public void setMoneyEarned(double money) {
        this.moneyEarned = money;
    }

    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    public String getUserCreated() {
        if (this.userCreated == null)
            return "";
        return this.userCreated;
    }

    public void setUserCreated(String username) {
        if (username != null) this.userCreated = username;
    }

    /**
     * parse a JSONArray to a list of DailyBalance objects
     *
     * @param jsonArray JSONArray containing data for a DailyBalance
     * @return List<DailyBalance> list of DailyBalance objects
     */
    public static List<DailyBalance> getDailyBalanceFromJSON(JSONArray jsonArray) {

        List<DailyBalance> dailyBalanceList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                dailyBalanceList.add(DailyBalanceJsonAdapter.fromJSON(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "");
                return dailyBalanceList;
            }
        }

        return dailyBalanceList;
    }

    @Override
    public int compareTo(DailyBalance otherDailyBalance) {

        int dateKeyInt1 = Integer.parseInt(this.dateKey);
        int dateKeyInt2 = Integer.parseInt(otherDailyBalance.getDateKey());
        int diff = dateKeyInt2 - dateKeyInt1;

        return Integer.compare(diff, 0);
    }
}
