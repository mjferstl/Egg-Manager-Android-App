package mfdevelopement.eggmanager;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {DailyBalance.class}, version = 1, exportSchema = false)
public abstract class EggManagerRoomDatabase extends RoomDatabase {

    public abstract DailyBalanceDao dailyBalanceDao();

    private static volatile EggManagerRoomDatabase INSTANCE;
    private static final String dataBaseName = "eggManagerDatabase";

    static EggManagerRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EggManagerRoomDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EggManagerRoomDatabase.class, dataBaseName).addCallback(sRoomDatabaseCallback).build();
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
}
