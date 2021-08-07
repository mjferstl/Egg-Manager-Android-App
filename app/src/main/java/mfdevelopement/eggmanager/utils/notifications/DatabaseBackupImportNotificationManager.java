package mfdevelopement.eggmanager.utils.notifications;

import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

import mfdevelopement.eggmanager.R;

/**
 * Class for creating notifications which correspond to database import actions
 */
public class DatabaseBackupImportNotificationManager extends AppNotificationManager {

    /**
     * ID of the notification
     */
    private static final int notificationId = AppNotificationManagerID.IMPORT_BACKUP_FILE.id;

    public DatabaseBackupImportNotificationManager(Context context) {
        super(context);
    }

    public void showImportNotification(String backupName) {

        Log.v(LOG_TAG, "showImportNotification(): " + backupName);

        String contentTitle = String.format(getContext().getString(R.string.notification_import_title_prefix), backupName);
        String contentText = getContext().getString(R.string.notification_importing_backup);

        // Customize the notification
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                // Issue the initial notification with zero progress
                .setProgress(100, 0, false)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                // vibrate for 100ms
                .setVibrate(new long[]{100});

        // Show the notification
        NotificationManagerCompat notificationManager = getNotificationManager();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void updateImportNotification(float progress, int maxProgress) {

        float progressPercent = progress / maxProgress * 100;
        String contentText = String.format(Locale.getDefault(), "Import %.2f%%", progressPercent);

        // Customize the notification
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setProgress(maxProgress, (int) progress, false)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                // no vibration
                .setVibrate(new long[]{0L});

        // Show/Update the notification
        NotificationManagerCompat notificationManager = getNotificationManager();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void setImportNotificationFinished(String notificationText) {

        Log.v(LOG_TAG, "setImportNotificationFinished(): " + notificationText);

        // Customize the notification
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder.setContentText(notificationText)
                .setProgress(100, 100, false)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                // vibrate for 100ms
                .setVibrate(new long[]{100L});

        // Show/Update the notification
        NotificationManagerCompat notificationManager = getNotificationManager();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
