package mfdevelopement.eggmanager.utils

import android.content.Context
import kotlinx.coroutines.*
import mfdevelopement.eggmanager.R
import mfdevelopement.eggmanager.data_models.DatabaseBackup
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance
import mfdevelopement.eggmanager.utils.notifications.DatabaseBackupCreateNotificationManager
import org.json.JSONArray
import java.util.*

/**
 * Enumeration which provides values for the possible results of creating a backup
 */
enum class Result {
    SUCCESS,
    ERROR
}

class BackupCreateCoroutine {

    /**
     * Interface to define the methods, which can be called after trying to create a backup
     */
    interface BackupCreateListener {
        /**
         * Method which will be called, when creating the backup has been executed successfully
         */
        fun onSuccess()

        /**
         * Method which will be called, when an error occurred while trying to create a backup
         */
        fun onFailure()
    }

    /**
     * List for storing the listeners
     */
    private val listeners: MutableList<BackupCreateListener> = mutableListOf()

    /**
     * Create a backup file in a coroutine context
     * @param backupFilename name of the backup file, which will be created
     * @param dailyBalanceList list of the daily balance object, which will be stored in the backup
     * @param context Context
     */
    private suspend fun createBackup(
        backupFilename: String, dailyBalanceList: List<DailyBalance>, context: Context
    ) {

        // Move the execution of the coroutine to the I/O dispatcher
        // val result =
        withContext(Dispatchers.IO) {

            // Create a new backup
            val backup = DatabaseBackup()
            backup.backupName = backupFilename

            // Create a notification

            // Create a notification
            val notificationManager = DatabaseBackupCreateNotificationManager(context)
            notificationManager.showFileCreateNotification(backup.filename)

            // Create a JSONArray and write it to the file

            // Create a JSONArray and write it to the file
            val jsonArray: JSONArray =
                DailyBalanceJsonUtils.createJsonArrayFromDailyBalance(dailyBalanceList)
            //val status =
            FileUtil.writeContentToFile(context, backup.filename, jsonArray.toString())

            // Update the notification
            val msg = String.format(
                Locale.getDefault(),
                context.getString(R.string.notification_backup_created),
                backup.backupName,
                dailyBalanceList.size
            )
            notificationManager.setFileCreateNotificationFinished(msg)

            // TODO: Handle errors
        }
        withContext(Dispatchers.Main) {
            //update UI here.
            this@BackupCreateCoroutine.notifyListeners(Result.SUCCESS)
        }
    }

    /**
     * Call the corresponding methods of the listeners
     * @param result defines which method of the listeners will be called
     */
    private fun notifyListeners(result: Result) {
        when (result) {
            Result.SUCCESS -> listeners.forEach { it.onSuccess() }
            Result.ERROR -> listeners.forEach { it.onFailure() }
        }
    }

    /**
     * Add a listener for creating backups
     * @param backupCreateListener listener to be added
     */
    fun addBackupCreateListener(backupCreateListener: BackupCreateListener) {
        listeners.add(backupCreateListener)
    }

    /**
     * Create the backup.
     * The backup will be created in a coroutine context.
     * @param backupFilename name of the backup file, which will be created
     * @param dailyBalanceList list of the daily balance object, which will be stored in the backup
     * @param context Context
     */
    fun create(backupFilename: String, dailyBalanceList: List<DailyBalance>, context: Context) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch { createBackup(backupFilename, dailyBalanceList, context) }
    }
}