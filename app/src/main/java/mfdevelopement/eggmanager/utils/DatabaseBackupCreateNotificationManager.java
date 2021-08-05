package mfdevelopement.eggmanager.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;

public class DatabaseBackupCreateNotificationManager extends AppNotificationManager {

    private static final String LOG_TAG = "DatabaseBackupCreateNot";

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private static final int notificationID = AppNotificationManagerID.CREATE_BACKUP_FILE.id;

    public DatabaseBackupCreateNotificationManager(@NonNull Context context) {
        super(context);
    }

    public void showFileCreateNotification(@Nullable String backupName) {
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
                .setContentTitle("Datensicherung")
                .setContentText("Datei wird erstellt")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        if (backupName != null && !backupName.isEmpty()) {
            notificationBuilder.setContentText("Datei \"" + backupName + "\" wird erstellt...");
        }

        // Issue the initial notification with zero progress
        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        notificationBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    public void setFileCreateNotificationFinished() {
        setFileCreateNotificationFinished(null);
    }

    public void setFileCreateNotificationFinished(@Nullable String notificationText) {

        if (notificationBuilder == null) {
            Log.e(LOG_TAG, "setImportNotificationFinished(): notificationBuilder = null");
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

        String contentText = "Datensicherung erstellt";
        if (notificationText != null && !notificationText.isEmpty()) {
            contentText = notificationText;
        }

        notificationBuilder.setContentText(contentText)
                .setProgress(100, 100, false)
                .setContentIntent(pendingIntent)
                // Multiline Notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }
}
