package mfdevelopement.eggmanager.utils;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;

public class AppNotificationManager {

    private final Context context;

    private NotificationCompat.Builder importNotificationBuilder;
    private NotificationManagerCompat notificationManager;

    private static final String NOTIFICATION_CHANNEL_ID = "EggManagerNotificationChannel";
    private final int NOTIFICATION_ID_IMPORT = 0;

    private final String LOG_TAG = "AppNotificationManager";

    public static final String INTENT_ACTION_OPEN_DATABASE = "OpenDatabaseFragment";
    public static final String INTENT_ACTION_OPEN_BACKUP = "OpenDatabaseBackupFragment";


    public AppNotificationManager(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void createNotificationChannel() {
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


    public void showImportNotification(String backupName) {
        if (this.context == null)
            return;

        notificationManager = NotificationManagerCompat.from(this.context);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this.context, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(INTENT_ACTION_OPEN_BACKUP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);


        importNotificationBuilder = new NotificationCompat.Builder(this.context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eggs_black)
                .setContentTitle(this.context.getString(R.string.notification_import_title_prefix) + " \"" + backupName + "\"")
                .setContentText(this.context.getString(R.string.notification_importing_backup))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        importNotificationBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(NOTIFICATION_ID_IMPORT, importNotificationBuilder.build());
    }

    public void updateImportNotification(int progress) {
        updateImportNotification(progress, 100);
    }

    public void updateImportNotification(int progress, int maxProgress) {

        Log.d(LOG_TAG,"updating notification. current progress: " + progress);

        if (this.context == null)
            return;

        if (importNotificationBuilder == null) {
            Log.e(LOG_TAG, "updateImportNotification(): importNotificationBuilder = null");
            return;
        }

        if (notificationManager == null)
            notificationManager = NotificationManagerCompat.from(this.context);

        importNotificationBuilder.setProgress(maxProgress, progress, false)
            .setContentText(progress + "%")
            .setAutoCancel(false);
        notificationManager.notify(NOTIFICATION_ID_IMPORT, importNotificationBuilder.build());
    }

    public void setImportNotificationFinished() {
        setImportNotificationFinished(this.context.getString(R.string.notification_import_finished));
    }

    public void setImportNotificationFinished(String notificationText) {

        if (importNotificationBuilder == null) {
            Log.e(LOG_TAG, "setImportNotificationFinished(): importNotificationBuilder = null");
            return;
        }

        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(this.context);
        }

        // start the database overview if the import finished
        Intent intent = new Intent(this.context, MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(INTENT_ACTION_OPEN_DATABASE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);

        importNotificationBuilder.setContentText(notificationText)
                .setProgress(0, 0, false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID_IMPORT, importNotificationBuilder.build());
    }
}
