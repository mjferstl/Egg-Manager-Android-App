package mfdevelopement.eggmanager.data_models.daily_balance;

import androidx.annotation.NonNull;

public class DailyBalanceUpgradeHelper {

    public static boolean hasValidDate(@NonNull DailyBalance dailyBalance) {
        return (dailyBalance.getDateRaw() != null);
    }
}
