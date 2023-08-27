package mfdevelopement.eggmanager.utils.notifications;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import mfdevelopement.eggmanager.IntentCodes;
import mfdevelopement.eggmanager.R;
import mfdevelopement.eggmanager.activities.MainNavigationActivity;

public abstract class AppNotificationManager {

    protected static final String NOTIFICATION_CHANNEL_ID = "EggManagerNotificationChannel";
    protected final String LOG_TAG = "AppNotificationManager";
    // App context
    private final Context context;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private PendingIntent pendingIntent = null;

    public AppNotificationManager(@NonNull Context context) {
        this.context = context;
        createNotificationChannel();

        // Initialize the notification manager
        initNotificationManager(context);
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
            if (this.getContext() != null) {
                android.app.NotificationManager notificationManager = this.getContext().getSystemService(android.app.NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Get the {@link Context}, which has been passed to the constructor when the object has been created
     *
     * @return stored {@link Context}
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Initialize the NotificationManager
     *
     * @param context Context
     */
    protected void initNotificationManager(@NonNull Context context) {
        setNotificationManager(NotificationManagerCompat.from(context));
        getNotificationManager();
    }

    /**
     * Get the current {@link NotificationManagerCompat}. If it has not been initialized yet, a new one will be created.
     *
     * @return {@link NotificationManagerCompat}
     */
    protected NotificationManagerCompat getNotificationManager() {
        if (this.notificationManager == null) {
            Context context = getContext();
            if (context == null) {
                Log.e(LOG_TAG, "getNotificationManager(): this.notificationManager == null and getContext() == null\nCannot initialize the notificationManager");
            } else {
                initNotificationManager(context);
            }
        }

        return this.notificationManager;
    }

    private void setNotificationManager(NotificationManagerCompat notificationManager) {
        this.notificationManager = notificationManager;
    }

    /**
     * Create a new {@link NotificationCompat.Builder} with default specs
     *
     * @param context {@link Context}
     * @return new {@link NotificationCompat.Builder} object
     */
    private NotificationCompat.Builder createNotificationBuilder(@NonNull Context context) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_eggs_black)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false);

        // Set the intent that will fire when the user taps the notification
        if (getPendingIntent() != null) notificationBuilder.setContentIntent(getPendingIntent());
        return notificationBuilder;
    }

    /**
     * Get the {@link NotificationCompat.Builder}
     *
     * @return current {@link NotificationCompat.Builder} object. If it has not been initialized yet, a new object will be created.
     */
    protected NotificationCompat.Builder getNotificationBuilder() {
        if (this.notificationBuilder == null) {
            this.notificationBuilder = createNotificationBuilder(getContext());
        }
        return this.notificationBuilder;
    }

    /**
     * Initialize the {@link PendingIntent} for the notification.
     * This intent will try to open the main activity of the app.
     * This method calls the overloaded method {@link AppNotificationManager#initPendingIntent} with {@link IntentCodes.NotificationActions#OPEN_MAIN_ACTIVITY} as argument
     */
    private void initPendingIntent() {
        initPendingIntent(IntentCodes.NotificationActions.OPEN_MAIN_ACTIVITY);
    }

    /**
     * Initialize the {@link PendingIntent} for the notification
     *
     * @param notificationAction {@link IntentCodes.NotificationActions} as code for the action, which will be executed when clicking the notification
     */
    private void initPendingIntent(@NonNull IntentCodes.NotificationActions notificationAction) {
        Intent intent = new Intent(getContext(), MainNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(notificationAction.actionName);

        // For Android Versions S+  the mutability flags needs to be set
        int mutabilityFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PendingIntent.FLAG_MUTABLE : 0;
        this.pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, mutabilityFlag);
    }


    /**
     * Get the {@link PendingIntent}
     *
     * @return {@link PendingIntent} object
     */
    protected PendingIntent getPendingIntent() {
        if (this.pendingIntent == null) initPendingIntent();
        return this.pendingIntent;
    }

    /**
     * Set the {@link PendingIntent} for the notification
     *
     * @param notificationAction {@link IntentCodes.NotificationActions} to specify the action, which should be executed when clicking the notification
     */
    protected void setPendingIntent(@NonNull IntentCodes.NotificationActions notificationAction) {
        initPendingIntent(notificationAction);
        getNotificationBuilder().setContentIntent(getPendingIntent());
    }

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
}
