package mfdevelopement.eggmanager.utils

import android.content.Context
import kotlinx.coroutines.*
import mfdevelopement.eggmanager.data_models.DatabaseBackup
import mfdevelopement.eggmanager.data_models.daily_balance.DailyBalance
import mfdevelopement.eggmanager.utils.notifications.DatabaseBackupCreateNotificationManager
import org.json.JSONArray
import java.util.*

enum class Result {
    SUCCESS,
    ERROR
}

class BackupCreateCoroutine {

    interface BackupCreateListener {
        fun onSuccess()
        fun onFailure()
    }

    private val listeners: MutableList<BackupCreateListener> = mutableListOf()

    private suspend fun createBackup(
        backupFilename: String, dailyBalanceList: List<DailyBalance>, context: Context
    ) {

        // Move the execution of the coroutine to the I/O dispatcher
        val result = withContext(Dispatchers.IO) {

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
            val status =
                FileUtil.writeContentToFile(context, backup.filename, jsonArray.toString())

            // Update the notification
            val msg = String.format(
                Locale.getDefault(),
                "Datensicherung \"%s\" mit %d Einträgen erstellt.",
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

    var result: String = "";

    private fun notifyListeners(result: Result) {
        when (result) {
            Result.SUCCESS -> listeners.forEach { it.onSuccess() }
            Result.ERROR -> listeners.forEach { it.onFailure() }
        }
    }

    fun addBackupCreateListener(backupCreateListener: BackupCreateListener) {
        listeners.add(backupCreateListener)
    }

    fun create(backupFilename: String, dailyBalanceList: List<DailyBalance>, context: Context) {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch { createBackup(backupFilename, dailyBalanceList, context) }
    }
}