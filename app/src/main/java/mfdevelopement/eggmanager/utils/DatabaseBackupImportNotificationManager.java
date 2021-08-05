package mfdevelopement.eggmanager.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;

/**
 * Class for creating notifications which correspond to database import actions
 */
public class DatabaseBackupImportNotificationManager extends AppNotificationManager {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;

    /**
     * ID of the notification
     */
    private static final int notificationId = AppNotificationManagerID.IMPORT_BACKUP_FILE.id;

    public DatabaseBackupImportNotificationManager(Context context) {
        super(context);
    }

    public void showImportNotification(String backupName) {
        if (this.getContext() == null)
            return;

        notificationManager = NotificationManagerCompat.from(this.getContext());

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this.getContext(), MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(INTENT_ACTION_OPEN_BACKUP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getContext(), 0, intent, 0);


        notificationBuilder = new NotificationCompat.Builder(this.getContext(), NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_eggs_black)
                .setContentTitle(this.getContext().getString(R.string.notification_import_title_prefix) + " \"" + backupName + "\"")
                .setContentText(this.getContext().getString(R.string.notification_importing_backup))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        notificationBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void updateImportNotification(int progress) {
        updateImportNotification(progress, 100);
    }

    public void updateImportNotification(int progress, int maxProgress) {

        Log.d(LOG_TAG, "updating notification. current progress: " + progress);

        if (this.getContext() == null)
            return;

        if (notificationBuilder == null) {
            Log.e(LOG_TAG, "updateImportNotification(): importNotificationBuilder = null");
            return;
        }

        if (notificationManager == null)
            notificationManager = NotificationManagerCompat.from(this.getContext());

        notificationBuilder.setProgress(maxProgress, progress, false)
                .setContentText(progress + "%")
                .setAutoCancel(false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void setImportNotificationFinished() {
        setImportNotificationFinished(this.getContext().getString(R.string.notification_import_finished));
    }

    public void setImportNotificationFinished(String notificationText) {

        if (notificationBuilder == null) {
            Log.e(LOG_TAG, "setImportNotificationFinished(): importNotificationBuilder = null");
            return;
        }

        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(this.getContext());
        }

        // start the database overview if the import finished
        Intent intent = new Intent(this.getContext(), MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(INTENT_ACTION_OPEN_DATABASE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.getContext(), 0, intent, 0);

        notificationBuilder.setContentText(notificationText)
                .setProgress(0, 0, false)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
