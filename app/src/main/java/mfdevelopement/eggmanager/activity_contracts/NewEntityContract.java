package mfdevelopement.eggmanager.activity_contracts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NewEntityContract extends ActivityResultContract<NewEntityIntentAdapter, Integer> {

    private static final String LOG_TAG = "NewEntityContract";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull NewEntityIntentAdapter adapter) {
        return adapter.toIntent(context);
    }

    @Override
    public Integer parseResult(int resultCode, @Nullable Intent result) {
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.v(LOG_TAG, "User cancelled the action");
        }
        if (resultCode == Activity.RESULT_OK) {
            Log.v(LOG_TAG, "Activity exited with code RESULT_OK");
        }
        return resultCode;
    }
}
