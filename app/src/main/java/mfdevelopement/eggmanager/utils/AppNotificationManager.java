package mfdevelopement.eggmanager.utils;

import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

public abstract class AppNotificationManager {

    /**
     * Enum containing the IDs of the different notification types
     */
    protected enum AppNotificationManagerID {
        IMPORT_BACKUP_FILE(0),
        CREATE_BACKUP_FILE(1);

        public final int id;

        AppNotificationManagerID(int id) {
            this.id = id;
        }
    }

    // App context
    private final Context context;

    protected static final String NOTIFICATION_CHANNEL_ID = "EggManagerNotificationChannel";

    protected final String LOG_TAG = "AppNotificationManager";

    // TODO: Move to another class
    public static final String INTENT_ACTION_OPEN_DATABASE = "OpenDatabaseFragment";
    // TODO: Move to another class
    public static final String INTENT_ACTION_OPEN_BACKUP = "OpenDatabaseBackupFragment";

    public AppNotificationManager(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EggManagerNotificationChannel";
            String description = "Notification for mfdevelopment.EggManager";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (this.context != null) {
                android.app.NotificationManager notificationManager = this.context.getSystemService(android.app.NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }
        }
    }

    protected Context getContext() {
        return context;
    }
}
