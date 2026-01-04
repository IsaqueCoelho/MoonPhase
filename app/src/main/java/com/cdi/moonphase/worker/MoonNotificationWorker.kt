package com.cdi.moonphase.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cdi.moonphase.R

class MoonNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val index = inputData.getInt(KEY_PHASE_INDEX, -1)
        val name = inputData.getString(KEY_PHASE_NAME) ?: "Moon Phase"
        val dateIso = inputData.getString(KEY_PHASE_DATE_ISO) ?: ""

        createChannel()
        val text = when (index) {
            0 -> "🌑 A New Moon is coming in 7 days!"
            2 -> "🌓 A First Quarter Moon is coming in 7 days!"
            4 -> "🌕 A Full Moon is coming in 7 days!"
            6 -> "🌗 A Last Quarter Moon is coming in 7 days!"
            else -> "🌙 $name is coming in 7 days!"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Moon Phase Reminder")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(applicationContext)) {
            try {
                notify(System.currentTimeMillis().toInt(), notification)
            } catch (e: SecurityException) {
                // Missing POST_NOTIFICATIONS permission
                return Result.failure()
            }
        }
        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Moon Phase Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming major moon phases"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "moon_channel"
        const val KEY_PHASE_INDEX = "key_phase_index"
        const val KEY_PHASE_NAME = "key_phase_name"
        const val KEY_PHASE_DATE_ISO = "key_phase_date_iso"
    }
}
