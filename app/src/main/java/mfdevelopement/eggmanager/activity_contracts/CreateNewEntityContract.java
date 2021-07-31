package mfdevelopement.eggmanager.activity_contracts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import mfdevelopement.eggmanager.activities.NewEntityActivity;
import mfdevelopement.eggmanager.fragments.DatabaseFragment;

public class CreateNewEntityContract extends ActivityResultContract<Long, Integer> {

    private final String LOG_TAG = "CreateNewEntity";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull Long requestCode) {
        Intent intent = new Intent(context, NewEntityActivity.class);
        intent.putExtra(DatabaseFragment.EXTRA_REQUEST_CODE_NAME, requestCode);
        return intent;
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
