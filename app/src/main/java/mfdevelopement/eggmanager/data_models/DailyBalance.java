package mfdevelopement.eggmanager.data_models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mfdevelopement.eggmanager.utils.DateTypeConverter;

@Entity(tableName = "dailyBalanceTable")
public class DailyBalance implements Serializable, Comparable<DailyBalance> {

    public static final String COL_DATE_PRIMARY_KEY = "dateKey";
    public static final String COL_EGGS_COLLECTED_NAME = "eggsFetched";
    public static final String COL_EGGS_SOLD_NAME = "eggsSold";
    public static final String COL_PRICE_PER_EGG = "pricePerEgg";
    public static final String COL_NUMBER_HENS = "numberOfHens";
    public static final String COL_MONEY_EARNED = "moneyEarned";
    public static final String COL_DATE_CREATED = "dateCreated";
    public static final String DATE_KEY_FORMAT = "yyyyMMdd";
    public static final int NOT_SET = 0;

    private static final String LOG_TAG = "DailyBalance";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = COL_DATE_PRIMARY_KEY)
    private String dateKey;

    @ColumnInfo(name = COL_EGGS_COLLECTED_NAME)
    private int eggsCollected;

    @ColumnInfo(name = COL_EGGS_SOLD_NAME)
    private int eggsSold;

    @ColumnInfo(name = COL_PRICE_PER_EGG)
    private double pricePerEgg;

    @ColumnInfo(name = COL_MONEY_EARNED)
    private double moneyEarned;

    @ColumnInfo(name = COL_NUMBER_HENS)
    private int numHens;

    @TypeConverters(DateTypeConverter.class)
    @ColumnInfo(name = COL_DATE_CREATED)
    private Date dateCreated;

    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg) {
        setDateKey(dateKey);
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        this.numHens = 0;
        this.moneyEarned = calcMoneyEarned(eggsSold,pricePerEgg);
        this.dateCreated = getCurrentDate();
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens) {
        setDateKey(dateKey);
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        this.numHens = numHens;
        this.moneyEarned = calcMoneyEarned(eggsSold,pricePerEgg);
        this.dateCreated = getCurrentDate();
    }

    @Ignore
    public DailyBalance(String dateKey, int eggsCollected, int eggsSold, double pricePerEgg, int numHens, Date dateCreated) {
        setDateKey(dateKey);
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        this.numHens = numHens;
        this.moneyEarned = calcMoneyEarned(eggsSold,pricePerEgg);
        this.dateCreated = dateCreated;
    }

    private double calcMoneyEarned(int eggsSold, double pricePerEgg) {
        // calculate the earned money
        if (eggsSold != 0 && pricePerEgg != 0)
            return eggsSold*pricePerEgg;
        else
            return 0;
    }

    @NonNull
    public String getDateKey(){
        return this.dateKey;
    }

    private void setDateKey(String dateKey) {
        if (dateKey == null || dateKey.length() != DATE_KEY_FORMAT.length()) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());
            this.dateKey = sdf.format(cal.getTimeInMillis());
        } else {
            this.dateKey = dateKey;
        }
    }

    public int getEggsCollected() { return  this.eggsCollected; }

    public int getEggsSold() { return this.eggsSold; }

    public double getPricePerEgg() { return this.pricePerEgg; }

    public Date getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_KEY_FORMAT, Locale.getDefault());
        Date date;

        try {
            date = sdf.parse(this.dateKey);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public int getNumHens() { return this.numHens; }

    public void setNumHens(int numHens) {
        this.numHens = numHens;
    }

    public double getMoneyEarned() { return this.moneyEarned; }

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

    public static String getYearByDateKey(String dateKey) {
        if (dateKey.length() >= 4) {
            return dateKey.substring(0,4).trim();
        } else {
            return "";
        }
    }

    public static String getMonthByDateKey(String dateKey) {
        if (dateKey.length() >= 6) {
            return dateKey.substring(4, 6).trim();
        } else {
            return "";
        }
    }


    /**
     * create a JSONObject
     * @return JSONObject representing the DailyBalance object in JSON notation
     */
    public JSONObject toJSON() {

        // new JSONObject
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(DailyBalance.COL_DATE_PRIMARY_KEY, this.getDateKey());
            jsonObject.put(DailyBalance.COL_EGGS_COLLECTED_NAME, this.getEggsCollected());
            jsonObject.put(DailyBalance.COL_EGGS_SOLD_NAME, this.getEggsSold());
            jsonObject.put(DailyBalance.COL_PRICE_PER_EGG, this.getPricePerEgg());
            jsonObject.put(DailyBalance.COL_MONEY_EARNED, this.getMoneyEarned());
            jsonObject.put(DailyBalance.COL_NUMBER_HENS, this.getNumHens());

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error when adding fields to JSONObject");
        }

        return jsonObject;
    }


    /**
     * parse a JSONArray to a list of DailyBalance objects
     * @param jsonArray JSONArray containing data for a DailyBalance
     * @return List<DailyBalance> list of DailyBalance objects
     */
    public static List<DailyBalance> getDailyBalanceFromJSON(JSONArray jsonArray) {

        List<DailyBalance> dailyBalanceList = new ArrayList<>();

        JSONObject item;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                item = jsonArray.getJSONObject(i);
                String dateKey = item.getString(DailyBalance.COL_DATE_PRIMARY_KEY);
                int eggsCollected = item.getInt(DailyBalance.COL_EGGS_COLLECTED_NAME);
                int eggsSold = item.getInt(DailyBalance.COL_EGGS_SOLD_NAME);
                double pricePerEgg = item.getDouble(DailyBalance.COL_PRICE_PER_EGG);
                int numHens = item.getInt(DailyBalance.COL_NUMBER_HENS);

                dailyBalanceList.add(new DailyBalance(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens));
            } catch (JSONException e) {
                e.printStackTrace();
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
