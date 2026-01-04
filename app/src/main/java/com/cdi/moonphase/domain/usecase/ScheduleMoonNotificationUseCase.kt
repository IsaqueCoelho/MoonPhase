package com.cdi.moonphase.domain.usecase

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.cdi.moonphase.data.repository.MoonRepository
import com.cdi.moonphase.worker.MoonNotificationWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime

class ScheduleMoonNotificationUseCase(
    private val context: Context,
    private val repository: MoonRepository
) {
    /**
     * Schedule notifications 7 days before each of the next four major phases
     * (New, First Quarter, Full, Last Quarter).
     */
    suspend operator fun invoke(from: LocalDateTime = LocalDateTime.now()): Unit = withContext(Dispatchers.Default) {
        val wm = WorkManager.getInstance(context)
        val next = repository.getNextMajorPhases(from, count = 4)
        val now = LocalDateTime.now()
        next.forEach { (phaseDate, index) ->
            val notifyAt = phaseDate.minusDays(7)
            val delay = Duration.between(now, notifyAt)
            if (!delay.isNegative) {
                val name = repository.phaseName(index)
                val input = Data.Builder()
                    .putInt(MoonNotificationWorker.KEY_PHASE_INDEX, index)
                    .putString(MoonNotificationWorker.KEY_PHASE_NAME, name)
                    .putString(MoonNotificationWorker.KEY_PHASE_DATE_ISO, phaseDate.toString())
                    .build()
                val request = OneTimeWorkRequestBuilder<MoonNotificationWorker>()
                    .setInitialDelay(delay)
                    .setInputData(input)
                    .build()
                wm.enqueue(request)
            }
        }
    }
}
