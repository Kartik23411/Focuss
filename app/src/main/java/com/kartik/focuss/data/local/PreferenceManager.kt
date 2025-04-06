package com.kartik.focuss.data.local

import android.content.Context

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE)

    fun getThresholdTime(default: Int = 5): Int {
        return prefs.getInt("threshold_time", default)
    }

    fun setThresholdTime(time: Int) {
        prefs.edit().putInt("threshold_time", time).apply()
    }

    fun isMonitoringEnabled(): Boolean {
        return prefs.getBoolean("isEnable", false)
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("isEnable", enabled).apply()
    }
}