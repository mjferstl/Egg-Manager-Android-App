package mfdevelopement.eggmanager.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance;
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalanceDao;

@Database(entities = {DailyBalance.class}, version = 3)
public abstract class EggManagerRoomDatabase extends RoomDatabase {

    public abstract DailyBalanceDao dailyBalanceDao();

    private static volatile EggManagerRoomDatabase INSTANCE;
    private static final String dataBaseName = "eggManagerDatabase";

    public static EggManagerRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EggManagerRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EggManagerRoomDatabase.class, dataBaseName)
                            .fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final DailyBalanceDao mDao;

        PopulateDbAsync(EggManagerRoomDatabase db) {
            mDao = db.dailyBalanceDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.getAscendingItems();
            return null;
        }
    }
    
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dailyBalanceTable ADD COLUMN dateCreated INTEGER");
        }
    };

    // Renaming columns and adding a new column containing the name of the user who created the entry
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE new_dailyBalanceTable (" +
                    "dateKey TEXT PRIMARY KEY NOT NULL," +
                    "eggsCollected INTEGER NOT NULL DEFAULT 0," +
                    "eggsSold INTEGER NOT NULL DEFAULT 0," +
                    "pricePerEgg REAL NOT NULL DEFAULT 1.0," +
                    "moneyEarned REAL NOT NULL DEFAULT 0.0," +
                    "numberOfHens INTEGER NOT NULL DEFAULT 0," +
                    "dateCreated INTEGER," +
                    "userCreated TEXT DEFAULT ' '" +
                    ")");
            database.execSQL("INSERT INTO new_dailyBalanceTable (dateKey, eggsCollected, eggsSold, pricePerEgg, moneyEarned, numberOfHens, dateCreated) " +
                    "SELECT dateKey, eggsFetched, eggsSold, pricePerEgg, moneyEarned, numberOfHens, dateCreated FROM dailyBalanceTable");
            database.execSQL("DROP TABLE dailyBalanceTable");
            database.execSQL("ALTER TABLE new_dailyBalanceTable RENAME TO dailyBalanceTable");
        }
    };
}