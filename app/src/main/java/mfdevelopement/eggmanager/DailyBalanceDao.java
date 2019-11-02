package mfdevelopement.eggmanager;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static mfdevelopement.eggmanager.DailyBalance.COL_DATE_PRIMARY_KEY;
import static mfdevelopement.eggmanager.DailyBalance.COL_EGGS_COLLECTED_NAME;
import static mfdevelopement.eggmanager.DailyBalance.COL_EGGS_SOLD_NAME;
import static mfdevelopement.eggmanager.DailyBalance.COL_MONEY_EARNED;

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

    @Query("SELECT * from " + tableName + " ORDER BY " + COL_DATE_PRIMARY_KEY + " ASC")
    LiveData<List<DailyBalance>> getAscendingItems();

    @Query("SELECT * FROM " + tableName + " WHERE " + COL_DATE_PRIMARY_KEY + " = :dateKey")
    DailyBalance getDailyBalance(String dateKey);

    @Query("SELECT SUM(" + COL_EGGS_SOLD_NAME + ") FROM " + tableName)
    LiveData<Integer> getTotalEggsSold();

    @Query("SELECT SUM(" + COL_MONEY_EARNED + ") FROM " + tableName)
    LiveData<Double> getTotalMoneyEarned();

    @Query("SELECT SUM(" + COL_EGGS_COLLECTED_NAME + ") FROM " + tableName)
    LiveData<Integer> getTotalEggsCollected();

    @Query("SELECT " + COL_DATE_PRIMARY_KEY + " FROM " + tableName)
    LiveData<List<String>> getDateKeys();

    @Query("SELECT * FROM " + tableName + " WHERE " + COL_DATE_PRIMARY_KEY + " LIKE :dateKey || '%'")
    List<DailyBalance> getDailyBalancesByDateKey(String dateKey);
}
