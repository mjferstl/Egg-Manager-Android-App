package mfdevelopement.eggmanager.data_models.daily_balance;

import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_DATE_CREATED;
import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_DATE_KEY;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import mfdevelopement.eggmanager.utils.type_converter.DateTypeConverter;

public class DailyBalanceJsonAdapter {

    private final static String LOG_TAG = "DailyBalanceJsonAdapter";

    private final DailyBalance dailyBalance;

    public DailyBalanceJsonAdapter(DailyBalance dailyBalance) {
        this.dailyBalance = dailyBalance;
    }

    public static DailyBalance fromJSON(JSONObject item) throws JSONException {

        String dateKeyPrimary = item.getString(DailyBalance.COL_DATE_PRIMARY_KEY);
        int eggsCollected = getJSONElementWithDefault(item, DailyBalance.COL_EGGS_COLLECTED_NAME, 0);
        int eggsSold = getJSONElementWithDefault(item, DailyBalance.COL_EGGS_SOLD_NAME, 0);
        double pricePerEgg = getJSONElementWithDefault(item, DailyBalance.COL_PRICE_PER_EGG, 0.0);
        int numHens = getJSONElementWithDefault(item, DailyBalance.COL_NUMBER_HENS, 0);
        String userCreated = getJSONElementWithDefault(item, DailyBalance.COL_USER_CREATED, "");
        Date dateCreated = null;

        try {
            long timeInMillis = item.getLong(COL_DATE_CREATED);
            dateCreated = DateTypeConverter.fromTimestamp(timeInMillis);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Could not convert timestamp (" + COL_DATE_CREATED + ") from item: " + item);
        }

        Date dateKey = null;
        try {
            long timeInMillis = item.getLong(COL_DATE_KEY);
            dateKey = DateTypeConverter.fromTimestamp(timeInMillis);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Could not convert timestamp (" + COL_DATE_KEY + ") from item: " + item);
        }

        // add DailyBalance object to the List
        return new DailyBalance(dateKeyPrimary, eggsCollected, eggsSold, pricePerEgg, numHens, dateCreated, userCreated, dateKey);
    }

    /**
     * Create a JSONObject, which represents the {@link DailyBalance} object
     *
     * @return {@link JSONObject} representing the DailyBalance object in JSON notation
     */
    public JSONObject toJSON() {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(DailyBalance.COL_DATE_PRIMARY_KEY, this.dailyBalance.getDateKey());
            jsonObject.put(DailyBalance.COL_EGGS_COLLECTED_NAME, this.dailyBalance.getEggsCollected());
            jsonObject.put(DailyBalance.COL_EGGS_SOLD_NAME, this.dailyBalance.getEggsSold());
            jsonObject.put(DailyBalance.COL_PRICE_PER_EGG, this.dailyBalance.getPricePerEgg());
            jsonObject.put(DailyBalance.COL_MONEY_EARNED, this.dailyBalance.getMoneyEarned());
            jsonObject.put(DailyBalance.COL_NUMBER_HENS, this.dailyBalance.getNumHens());
            jsonObject.put(DailyBalance.COL_USER_CREATED, this.dailyBalance.getUserCreated());
            jsonObject.put(COL_DATE_CREATED, DateTypeConverter.dateToTimestamp(this.dailyBalance.getDateCreated()));
            jsonObject.put(COL_DATE_KEY, DateTypeConverter.dateToTimestamp(this.dailyBalance.getDate()));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error when adding fields to JSONObject");
        }

        return jsonObject;
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
}
