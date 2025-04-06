package com.kartik.focuss.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.kartik.focuss.R
import com.kartik.focuss.data.local.PreferenceManager
import com.kartik.focuss.domain.usecase.MonitorUsageUseCase
import com.kartik.focuss.presentation.ui.components.getMessage
import com.kartik.focuss.presentation.ui.components.getVibrationPattern

class UsageMonitorServic : Service() {

    companion object {
        const val TAG = "UsageMonitorService"
        const val CHANNEL_ID = "UsageMonitorChannel"
        const val ALERT_CHANNEL_ID = "ALERT_CHANNEL_DALRMOF"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        const val CHECK_INTERVAL = 10000L
    }

    private var thresholdTime: Long = 30 * 1000L  // Default threshold; can be updated from prefs

    private var sessionTime: Long = 0L
    private var notificationCount: Int = 0
    private var instagramUsageStartTime: Long = 0

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var monitorUseCase: MonitorUsageUseCase

    // To persist Instagram state (optional)
    private var isInstagramPersistentlyForeground: Boolean = false

    private val monitorRunnable = object : Runnable {
        override fun run() {
            Log.d(TAG, "Monitor runnable executed")
            // Use our use-case to check if Instagram is foreground
            val now = System.currentTimeMillis()
            val isForeground = monitorUseCase.isInstagramForeground(now - 60 * 1000, now)
            if (isForeground) {
                if (instagramUsageStartTime == 0L) {
                    instagramUsageStartTime = now
                    notificationCount = 0
                    Log.d(
                        TAG,
                        "Instagram detected in foreground, starting timer at $instagramUsageStartTime"
                    )
                }
                sessionTime = now - instagramUsageStartTime
                Log.d(TAG, "Session duration: $sessionTime ms")
                if (sessionTime >= (notificationCount + 1) * thresholdTime) {
                    val extraMinutes = (sessionTime / (60 * 1000)).toInt()
                    Log.d(TAG, "Usage threshold exceeded, sending alert for $extraMinutes min")
                    showAlertNotification(extraMinutes)
                    notificationCount ++
                }
            }
            else {
                if (instagramUsageStartTime != 0L) {
                    Log.d(TAG, "Instagram is no longer in foreground, resetting timer")
                    clearAlertNotification()
                }
                instagramUsageStartTime = 0
                sessionTime = 0
                notificationCount = 0
            }
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate called, initializing service")
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        preferenceManager = PreferenceManager(this)
        monitorUseCase = MonitorUsageUseCase(
            repository = com.kartik.focuss.domain.repository.UsageRepository(this)
        )

        // Optionally update thresholdTime from preferences:
        thresholdTime = preferenceManager.getThresholdTime() * 60 * 1000L

        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        handler.post(monitorRunnable)
        Log.d(TAG, "Foreground service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy called, stopping monitor runnable")
        handler.removeCallbacks(monitorRunnable)
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Usage Monitor Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            Log.d(TAG, "Notification channels created")
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Instagram Monitor Running")
                .setContentText("Monitoring Instagram usage in the background")
                .setSmallIcon(R.drawable.icon)
                .build()
    }

    private fun showAlertNotification(extraMinutes: Int) {
        val vibration = getVibrationPattern(extraMinutes * 30)
        val intent = Intent(this, com.kartik.focuss.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dynamicChannelId = "$ALERT_CHANNEL_ID-$extraMinutes"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri: Uri =
                    "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/${R.raw.custom_sound}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            val alertChannel = NotificationChannel(
                dynamicChannelId,
                "Alert Channel for $extraMinutes min",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = vibration
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alertChannel)
        }

        val notification = NotificationCompat.Builder(this, dynamicChannelId)
                .setContentTitle("$extraMinutes" + getMessage(extraMinutes))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setVibrate(vibration)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                            .setBigContentTitle("$extraMinutes" + getMessage(extraMinutes))
                )
                .build()

        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or
                Notification.FLAG_ONGOING_EVENT or Notification.FLAG_AUTO_CANCEL

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        Log.d(TAG, "Alert notification displayed")
    }

    private fun clearAlertNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
    }
}
