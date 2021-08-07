package mfdevelopement.eggmanager.utils.notifications;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;

/**
 * Class for creating notifications when creating a backup file
 */
public class DatabaseBackupCreateNotificationManager extends AppNotificationManager {

    private static final String LOG_TAG = "DatabaseBackupCreateNot";

    /**
     * ID of the notification
     */
    private static final int notificationID = AppNotificationManagerID.CREATE_BACKUP_FILE.id;

    public DatabaseBackupCreateNotificationManager(@NonNull Context context) {
        super(context);
    }

    public void showFileCreateNotification(@Nullable String backupName) {

        Log.v(LOG_TAG, "showFileCreateNotification(): " + backupName);

        String contentTitle = getContext().getString(R.string.notification_backup_create_title);
        String contentText = String.format(Locale.getDefault(), getContext().getString(R.string.notification_backup_create_string_formatter), backupName);

        // Customize the notification
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                // Issue the initial notification with zero progress
                .setProgress(100, 0, false);

        // Show the notification
        NotificationManagerCompat notificationManager = getNotificationManager();
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    public void setFileCreateNotificationFinished(@Nullable String notificationText) {

        Log.v(LOG_TAG, "setFileCreateNotificationFinished(): " + notificationText);

        // Create the notification
        String contentText = notificationText;
        if (notificationText == null || notificationText.isEmpty()) {
            contentText = getContext().getString(R.string.notification_backup_create_finished_text);
        }

        // Customize the notification
        NotificationCompat.Builder notificationBuilder = getNotificationBuilder();
        notificationBuilder
                .setContentText(contentText)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setProgress(100, 100, false);

        // Change the pending Intent
        setPendingIntent(IntentCodes.NotificationActions.OPEN_BACKUP_ACTIVITY);

        // Show/Update the notification
        NotificationManagerCompat notificationManager = getNotificationManager();
        notificationManager.notify(notificationID, notificationBuilder.build());
    }
}
