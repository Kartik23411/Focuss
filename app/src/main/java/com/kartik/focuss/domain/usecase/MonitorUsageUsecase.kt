package com.kartik.focuss.domain.usecase

import android.app.usage.UsageEvents
import com.kartik.focuss.domain.repository.UsageRepository

class MonitorUsageUseCase(private val repository: UsageRepository) {

    companion object {
        const val TARGET_PACKAGE = "com.instagram.android"
    }

    fun isInstagramForeground(beginTime: Long, endTime: Long): Boolean {
        val usageEvents = repository.queryUsageEvents(beginTime, endTime)
        var lastForegroundTimestamp = 0L
        var lastBackgroundTimestamp = 0L

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event().apply { usageEvents.getNextEvent(this) }
            if (event.packageName == TARGET_PACKAGE) {
                when (event.eventType) {
                    1 -> lastForegroundTimestamp = maxOf(lastForegroundTimestamp, event.timeStamp)
                    2 -> lastBackgroundTimestamp = maxOf(lastBackgroundTimestamp, event.timeStamp)
                }
            }
        }
        return if (lastForegroundTimestamp == 0L && lastBackgroundTimestamp == 0L) {
            false  // Or use a persisted state if you wish
        }
        else {
            lastForegroundTimestamp > lastBackgroundTimestamp
        }
    }
}