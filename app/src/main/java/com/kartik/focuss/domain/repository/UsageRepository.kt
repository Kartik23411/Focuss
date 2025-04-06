package com.kartik.focuss.domain.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import com.kartik.focuss.data.local.PreferenceManager

class UsageRepository(context: Context) {
    private val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val preferenceManager = PreferenceManager(context)

    // Add functions to query usage data, for example:
    fun queryUsageEvents(beginTime: Long, endTime: Long) =
            usageStatsManager.queryEvents(beginTime, endTime)
}