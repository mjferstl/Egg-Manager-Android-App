package mfdevelopement.eggmanager.workmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import mfdevelopement.eggmanager.database.EggManagerRoomDatabase;

public class CleanupDatabaseWorker extends Worker {

    private static final String LOG_TAG = "CleanupDatabaseWorker";

    public CleanupDatabaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(LOG_TAG, "Starting to delete all items");
        EggManagerRoomDatabase.getDatabase(getApplicationContext()).dailyBalanceDao().deleteAll();
        Log.d(LOG_TAG, "All items deleted.");
        return Result.success();
    }
}
