package mfdevelopement.eggmanager.data_models.daily_balance;

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

import mfdevelopement.eggmanager.data_models.HasDateInterface;
import mfdevelopement.eggmanager.utils.DateTypeConverter;

@Entity(tableName = "dailyBalanceTable")
public class DailyBalance implements Serializable, Comparable<DailyBalance>, HasDateInterface {

    public static final String COL_DATE_PRIMARY_KEY = "dateKey";
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
        this.eggsCollected = eggsCollected;
        this.eggsSold = eggsSold;
        this.pricePerEgg = pricePerEgg;
        setNumHens(numHens);
        setMoneyEarned(calcMoneyEarned(eggsSold, pricePerEgg));

        if (dateCreated != null) setDateCreated(dateCreated);
        else setDateCreated(getCurrentDate());

        if (userCreated != null) setUserCreated(userCreated);
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
        SimpleDateFormat sdf = new SimpleDateFormat(DateKeyUtils.DATE_KEY_FORMAT, Locale.getDefault());
        Date date;

        try {
            date = sdf.parse(this.dateKey);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
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
     * Create a JSONObject, which represents the {@link DailyBalance} object
     *
     * @return {@link JSONObject} representing the DailyBalance object in JSON notation
     */
    public JSONObject toJSON() {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(DailyBalance.COL_DATE_PRIMARY_KEY, this.getDateKey());
            jsonObject.put(DailyBalance.COL_EGGS_COLLECTED_NAME, this.getEggsCollected());
            jsonObject.put(DailyBalance.COL_EGGS_SOLD_NAME, this.getEggsSold());
            jsonObject.put(DailyBalance.COL_PRICE_PER_EGG, this.getPricePerEgg());
            jsonObject.put(DailyBalance.COL_MONEY_EARNED, this.getMoneyEarned());
            jsonObject.put(DailyBalance.COL_NUMBER_HENS, this.getNumHens());
            jsonObject.put(DailyBalance.COL_USER_CREATED, this.getUserCreated());
            DateTypeConverter dtc = new DateTypeConverter();
            jsonObject.put(DailyBalance.COL_DATE_CREATED, dtc.dateToTimestamp(this.getDateCreated()));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error when adding fields to JSONObject");
        }

        return jsonObject;
    }


    /**
     * parse a JSONArray to a list of DailyBalance objects
     *
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
                int eggsCollected = getJSONElementWithDefault(item, DailyBalance.COL_EGGS_COLLECTED_NAME, 0);
                int eggsSold = getJSONElementWithDefault(item, DailyBalance.COL_EGGS_SOLD_NAME, 0);
                double pricePerEgg = getJSONElementWithDefault(item, DailyBalance.COL_PRICE_PER_EGG, 0.0);
                int numHens = getJSONElementWithDefault(item, DailyBalance.COL_NUMBER_HENS, 0);
                String userCreated = getJSONElementWithDefault(item, DailyBalance.COL_USER_CREATED, "");
                Date dateCreated = null;

                try {
                    long timeInMillis = item.getLong(DailyBalance.COL_DATE_CREATED);
                    DateTypeConverter dtc = new DateTypeConverter();
                    dateCreated = dtc.fromTimestamp(timeInMillis);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // add DailyBalance object to the List
                dailyBalanceList.add(new DailyBalance(dateKey, eggsCollected, eggsSold, pricePerEgg, numHens, dateCreated, userCreated));
            } catch (JSONException e) {
                e.printStackTrace();
                return dailyBalanceList;
            }
        }

        return dailyBalanceList;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getJSONElementWithDefault(JSONObject jsonObject, String elementName, T defaultValue) {
        try {
            if (defaultValue instanceof String) {
                return (T) jsonObject.getString(elementName);
            } else if (defaultValue instanceof Integer) {
                return (T) (Integer) jsonObject.getInt(elementName);
            } else if (defaultValue instanceof Double) {
                return (T) (Double) jsonObject.getDouble(elementName);
            } else if (defaultValue instanceof Long) {
                return (T) (Long) jsonObject.getLong(elementName);
            } else if (defaultValue instanceof Boolean) {
                return (T) (Boolean) jsonObject.getBoolean(elementName);
            } else {
                return (T) jsonObject.get(elementName);
            }
        } catch (JSONException e) {
            Log.d(LOG_TAG, String.format("The json object has no element named \"%s\". The default value %s will be returned.", elementName, defaultValue));
            return defaultValue;
        }
    }


    @Override
    public int compareTo(DailyBalance otherDailyBalance) {

        int dateKeyInt1 = Integer.parseInt(this.dateKey);
        int dateKeyInt2 = Integer.parseInt(otherDailyBalance.getDateKey());
        int diff = dateKeyInt2 - dateKeyInt1;

        return Integer.compare(diff, 0);
    }
}
