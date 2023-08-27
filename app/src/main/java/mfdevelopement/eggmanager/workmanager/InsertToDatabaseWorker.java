package mfdevelopement.eggmanager.workmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalanceWorkerDataUtil;
import mfdevelopement.eggmanager.database.EggManagerRoomDatabase;

public class InsertToDatabaseWorker extends Worker {

    public InsertToDatabaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        DailyBalance dailyBalance = DailyBalanceWorkerDataUtil.convertFromData(getInputData());
        EggManagerRoomDatabase.getDatabase(getApplicationContext()).dailyBalanceDao().insert(dailyBalance);
        return Result.success();
    }
}
