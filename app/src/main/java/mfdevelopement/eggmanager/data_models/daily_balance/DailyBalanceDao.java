package mfdevelopement.eggmanager.data_models.daily_balance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_DATE_PRIMARY_KEY;
import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_EGGS_COLLECTED_NAME;
import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_EGGS_SOLD_NAME;
import static mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance.COL_MONEY_EARNED;

@Dao
public interface DailyBalanceDao {

    String tableName = "dailyBalanceTable";

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyBalance dailyBalance);

    @Query("DELETE FROM " + tableName)
    void deleteAll();

    @Delete
    void delete(DailyBalance dailyBalance);

    @Query("SELECT COUNT(" + COL_DATE_PRIMARY_KEY + ") FROM " + tableName)
    LiveData<Integer> getRowCount();

    @Query("SELECT * from " + tableName + " ORDER BY " + COL_DATE_PRIMARY_KEY + " ASC")
    LiveData<List<DailyBalance>> getAscendingItems();

    @Query("SELECT * from " + tableName + " ORDER BY " + COL_DATE_PRIMARY_KEY + " ASC")
    LiveData<DailyBalance> getAscendingItemsList();

    @Query("SELECT SUM(" + COL_EGGS_SOLD_NAME + ") FROM " + tableName + " WHERE "+ COL_DATE_PRIMARY_KEY + " LIKE :dateFilter || '%' ")
    LiveData<Integer> getFilteredEggsSold(String dateFilter);

    @Query("SELECT SUM(" + COL_MONEY_EARNED + ") FROM " + tableName + " WHERE "+ COL_DATE_PRIMARY_KEY + " LIKE :dateFilter || '%' ")
    LiveData<Double> getFilteredMoneyEarned(String dateFilter);

    @Query("SELECT SUM(" + COL_EGGS_COLLECTED_NAME + ") FROM " + tableName + " WHERE "+ COL_DATE_PRIMARY_KEY + " LIKE :dateFilter || '%' ")
    LiveData<Integer> getFilteredEggsCollected(String dateFilter);

    @Query("SELECT " + COL_DATE_PRIMARY_KEY + " FROM " + tableName)
    LiveData<List<String>> getDateKeysLiveData();

    @Query("SELECT " + COL_DATE_PRIMARY_KEY + " FROM " + tableName)
    LiveData<List<String>> getDateKeysList();

    @Query("SELECT * FROM " + tableName + " WHERE " + COL_DATE_PRIMARY_KEY + " LIKE :dateKey || '%' ORDER BY " + COL_DATE_PRIMARY_KEY + " ASC")
    List<DailyBalance> getDailyBalancesByDateKey(String dateKey);

    @Query("SELECT * FROM " + tableName + " WHERE " + COL_DATE_PRIMARY_KEY + " LIKE (:dateKey || '%') ORDER BY " + COL_DATE_PRIMARY_KEY + " ASC")
    LiveData<List<DailyBalance>> getFilteredDailyBalance(String dateKey);
}
