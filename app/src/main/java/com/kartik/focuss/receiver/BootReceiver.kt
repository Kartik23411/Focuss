package com.kartik.focuss.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.kartik.focuss.UsageMonitorService

class BootReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
                intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device booted, starting UsageMonitorService")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ! alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Exact alarms are not allowed; service will not be scheduled")
                return
            }

            val alarmIntent = Intent(context, BootAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000,
                pendingIntent
            )

        }
    }
}

class BootAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "BootAlarmReceiver triggered, starting UsageMonitorService")
        val serviceIntent = Intent(context, UsageMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }
        else {
            context.startService(serviceIntent)
        }
    }
}