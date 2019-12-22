package mfdevelopement.eggmanager.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONUtil {

    private static final String LOG_TAG = "JSONUtil";

    /**
     * parse a String to a JSONArray
     * @param content String in JSON notation
     * @return JSONArray
     */
    public static JSONArray getJSONObject(String content) {

        // create a new JSONObject
        JSONArray jsonArray = new JSONArray();

        // convert Stirng to JSONObject
        try {
            jsonArray = new JSONArray(content);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "getJSONObject::error when converting String to JSONObject");
        }
        return jsonArray;
    }

}
