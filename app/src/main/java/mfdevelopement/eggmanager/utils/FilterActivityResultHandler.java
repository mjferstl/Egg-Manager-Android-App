package mfdevelopement.eggmanager.utils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import mfdevelopement.eggmanager.FilterStringHandle;

import static mfdevelopement.eggmanager.fragments.DatabaseFragment.FILTER_ACTIVITY_CANCEL_RESULT_CODE;
import static mfdevelopement.eggmanager.fragments.DatabaseFragment.FILTER_ACTIVITY_OK_RESULT_CODE;

public class FilterActivityResultHandler {

    private static final String LOG_TAG = "FilterActivityResultHan";

    public static void handleFilterActivityResult(int resultCode, @Nullable Intent data, FilterStringHandle viewModel) {

        if (resultCode == FILTER_ACTIVITY_OK_RESULT_CODE) {
            Log.d(LOG_TAG, "FilterActivity finished. User changed filter string");

            // try to get the filter string from the FilterActivity
            if ((data != null) && (data.getData() != null)) {

                // compare received filter string from the FilterActivity and the filter string from the view model
                // they need to be the same!
                String newFilterString = data.getData().toString();
                String loadedFilterString = viewModel.getFilterString();

                Log.d(LOG_TAG, "new filter string: from activity: \"" + newFilterString + "\", from viewModel: \"" + loadedFilterString + "\"");

                if (!newFilterString.equals(loadedFilterString)) {
                    Log.e(LOG_TAG, "Error when receiving the filter string, because string from the activity and string from the view model are not the same");
                }
            } else {
                Log.e(LOG_TAG, "Error when receiving new filter string from FilterActivity");
            }
        } else if (resultCode == FILTER_ACTIVITY_CANCEL_RESULT_CODE) {
            Log.d(LOG_TAG,"FilterActivity finished because user canceled the activity");
        }
    }
}
