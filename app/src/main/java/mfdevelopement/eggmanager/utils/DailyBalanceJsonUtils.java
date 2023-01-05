package mfdevelopement.eggmanager.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalanceJsonAdapter;

public class DailyBalanceJsonUtils {

    private static final String LOG_TAG = "DailyBalanceJsonUtils";

    /**
     * Create a JSONArray from a list of DailyBalance objects
     *
     * @param dailyBalanceList List of DailyBalance objects
     * @return JSONArray
     */
    public static JSONArray createJsonArrayFromDailyBalance(@NonNull List<DailyBalance> dailyBalanceList) {

        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dailyBalanceList.size(); i++) {
            try {
                DailyBalanceJsonAdapter adapter = new DailyBalanceJsonAdapter(dailyBalanceList.get(i));
                jsonArray.put(i, adapter.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "createJsonArrayFromDailyBalance(): Error encountered while creating JSONArray");
            }
        }
        return jsonArray;
    }
}
