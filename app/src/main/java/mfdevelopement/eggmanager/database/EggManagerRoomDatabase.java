package mfdevelopement.eggmanager.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import mfdevelopement.eggmanager.data_models.DailyBalance;
import mfdevelopement.eggmanager.data_models.DailyBalanceDao;

@Database(entities = {DailyBalance.class}, version = 2, exportSchema = false)
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
                                .addMigrations(MIGRATION_1_2)
                                .addCallback(sRoomDatabaseCallback).build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
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


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE dailyBalanceTable ADD COLUMN dateCreated BIGINT");
        }
    };
}