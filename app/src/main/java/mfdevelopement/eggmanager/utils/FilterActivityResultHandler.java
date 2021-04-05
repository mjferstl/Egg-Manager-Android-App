package mfdevelopement.eggmanager.utils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import mfdevelopement.eggmanager.DatabaseActions;

public class FilterActivityResultHandler {

    private static final String LOG_TAG = "FilterActivityResultHan";


    public static void handleFilterActivityResult(int resultCode, @Nullable Intent data) {

        if (resultCode == DatabaseActions.Result.FILTER_OK.ordinal()) {
            Log.d(LOG_TAG, "FilterActivity finished. User changed filter string");

            // try to get the filter string from the FilterActivity
            if ((data != null) && (data.getData() != null)) {

                // compare received filter string from the FilterActivity and the filter string from the view model
                // they need to be the same!
                String newFilterString = data.getData().toString();

                Log.d(LOG_TAG, "new filter string: from activity: \"" + newFilterString + "\"");

            } else {
                Log.e(LOG_TAG, "Error when receiving new filter string from FilterActivity");
            }
        } else if (resultCode == DatabaseActions.Result.FILTER_CANCEL.ordinal()) {
            Log.d(LOG_TAG, "FilterActivity finished because user canceled the activity");
        }
    }

    public static void handleFilterActivityResult(DatabaseActions.Result resultAction, @Nullable Intent data) {
        FilterActivityResultHandler.handleFilterActivityResult(resultAction.ordinal(), data);
    }
}
