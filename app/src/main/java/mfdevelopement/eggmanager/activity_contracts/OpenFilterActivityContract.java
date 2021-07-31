package mfdevelopement.eggmanager.activity_contracts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mfdevelopement.eggmanager.DatabaseActions;
import mfdevelopement.eggmanager.activities.FilterActivity;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;

public class OpenFilterActivityContract extends ActivityResultContract<Void, Integer> {

    private final String LOG_TAG = "OpenFilterActivityContr";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void v) {
        Log.v(LOG_TAG, "creating an intent for starting FilterActivity");
        Intent intent = new Intent(context, FilterActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, DatabaseActions.Request.EDIT_FILTER.id);
        return intent;
    }

    @Override
    public Integer parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.v(LOG_TAG, "User cancelled the action");
        }
        if (resultCode == Activity.RESULT_OK) {
            Log.v(LOG_TAG, "Activity exited with code RESULT_OK");
        }
        if (intent != null) {
            Log.d(LOG_TAG, "received an intent: " + intent);
        }
        return resultCode;
    }
}
