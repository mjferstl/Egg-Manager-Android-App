package mfdevelopement.eggmanager.viewmodels;

import androidx.lifecycle.LiveData;

import java.util.List;

import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;

public interface DailyBalanceViewModel {
    void insert(DailyBalance dailyBalance);

    void delete(DailyBalance dailyBalance);

    LiveData<List<DailyBalance>> getAllDailyBalances();
}
