package com.kartik.focuss

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kartik.focuss.view.components.getRandomMessage
import androidx.core.net.toUri
import com.kartik.focuss.view.components.getMessage

class UsageMonitorService : Service() {

    companion object {
        const val TAG = "UsageMonitorService"
        const val CHANNEL_ID = "UsageMonitorChannel"
        const val ALERT_CHANNEL_ID = "ALERT_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        const val TARGET_PACKAGE = "com.instagram.android"
        const val CHECK_INTERVAL = 10000L
    }

    private var thresholdTime: Long = 1 * 60 * 1000L

    private var sessionTime: Long = 0L
    private var notificationCount: Int = 0

    private var instagramUsageStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var usageStatsManager: UsageStatsManager


    private var isInstagramPersistentlyForeground: Boolean = false

    // Runnable that periodically checks the usage status
    private val monitorRunnable = object : Runnable {
        override fun run() {
            Log.d(TAG, "Monitor runnable executed")
            if (isInstagramForeground()) {
                if (instagramUsageStartTime == 0L) {
                    instagramUsageStartTime = System.currentTimeMillis()
                    notificationCount = 0
                    Log.d(TAG, "Instagram detected in foreground, starting timer at $instagramUsageStartTime")
                }
                    sessionTime = System.currentTimeMillis() - instagramUsageStartTime
                    Log.d(TAG, "Session duration: $sessionTime ms")
                    // Check if sessionTime has crossed an additional threshold interval.
                    if (sessionTime >= (notificationCount + 1) * thresholdTime) {
                        // Calculate the extra time used in minutes beyond the previous threshold.
                        val extraTime = sessionTime - (notificationCount * thresholdTime)
                        val extraMinutes = (sessionTime / (60 * 1000)).toInt()
                        Log.d(TAG, "Usage threshold exceeded, sending alert with extra usage: $extraMinutes min")
                        showAlertNotification(extraMinutes)
                        notificationCount++  // Increment the count so the next alert triggers after another threshold period.
                    }
            } else {
                if (instagramUsageStartTime != 0L) {
                    Log.d(TAG, "Instagram is no longer in foreground, resetting timer")
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
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val prefs = getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE)
        val thresholdMinutes = prefs.getInt("threshold_time", 5)
        thresholdTime = thresholdMinutes * 60 * 1000L
        Log.d(TAG, "Threshold time set to $thresholdTime ms")

        createNotificationChannels()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        handler.post(monitorRunnable)
        Log.d(TAG, "Foreground service started")
    }

    // added to make it run always
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy called, stopping monitor runnable")
        handler.removeCallbacks(monitorRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null


    private fun isInstagramForeground(): Boolean {
        val endTime = System.currentTimeMillis()

        val beginTime = endTime - 60 * 1000
        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)

        var lastForegroundTimestamp: Long = 0
        var lastBackgroundTimestamp: Long = 0


        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (event.packageName == TARGET_PACKAGE) {
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (event.timeStamp > lastForegroundTimestamp) {
                            lastForegroundTimestamp = event.timeStamp
                        }
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (event.timeStamp > lastBackgroundTimestamp) {
                            lastBackgroundTimestamp = event.timeStamp
                        }
                    }
                }
            }
        }

        val currentlyForeground = if (lastForegroundTimestamp > 0) {
            lastForegroundTimestamp > lastBackgroundTimestamp
        } else {
            false
        }

        // If no events are found, use the persistent state.
        val finalState = if (lastForegroundTimestamp == 0L && lastBackgroundTimestamp == 0L) {
            isInstagramPersistentlyForeground
        } else {
            currentlyForeground
        }

        // to Persist the current state for future checks.
        isInstagramPersistentlyForeground = finalState
        Log.d(TAG, "isInstagramForeground (persistent): $finalState (Last FG: $lastForegroundTimestamp, Last BG: $lastBackgroundTimestamp)")
        return finalState
    }

    // Creates notification channels for the foreground service and alert notifications.
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Usage Monitor Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val soundUri: Uri = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/${R.raw.custom_sound}".toUri()

            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alert Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alertChannel)
            Log.d(TAG, "Notification channels created")
        }
    }

    // Builds the persistent foreground notification.
    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Instagram Monitor Running")
                .setContentText("Monitoring Instagram usage in the background")
                .setSmallIcon(R.drawable.icon)
                .build()
    }

    // Displays the alert notification when the usage threshold is exceeded.
    private fun showAlertNotification(extraMinutes: Int) {
        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setContentTitle(getMessage(extraMinutes))
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        Log.d(TAG, "Alert notification displayed")
    }
}

//else {
//                    val elapsed = System.currentTimeMillis() - instagramUsageStartTime
//                    Log.d(TAG, "Instagram has been in foreground for $elapsed ms")
//                    if (elapsed >= thresholdTime) {
//                        Log.d(TAG, "Usage threshold exceeded, sending alert notification")
//                        showAlertNotification((elapsed/60000).toInt())
//                        Log.e("elapsed", "Time elapsed is ${elapsed.toInt().toString()}")
//                        // Reset the timer after alerting to avoid repeated alerts
//                        instagramUsageStartTime = System.currentTimeMillis()
//                    }