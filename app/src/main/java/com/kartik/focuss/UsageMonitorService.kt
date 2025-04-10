package com.kartik.focuss

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.kartik.focuss.presentation.ui.components.getMessage
import com.kartik.focuss.presentation.ui.components.getVibrationPattern

class UsageMonitorService : Service() {

    companion object {
        const val TAG = "UsageMonitorService"
        const val CHANNEL_ID = "UsageMonitorChannel"
        const val ALERT_CHANNEL_ID = "ALERT_CHANNEL_QWEMOF"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        const val TARGET_PACKAGE = "com.instagram.android"
        const val CHECK_INTERVAL = 10000L
    }

    private var thresholdTime: Long = 1 * 30 * 1000L
    private var lastNotificationTime: Long = 1 * 30 * 1000L

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
                    Log.d(
                        TAG,
                        "Instagram detected in foreground, starting timer at $instagramUsageStartTime"
                    )
                }
                sessionTime = System.currentTimeMillis() - instagramUsageStartTime
                Log.d(TAG, "Session duration: $sessionTime ms")

                if (sessionTime >= thresholdTime) {
                    val elapsedSinceLast = System.currentTimeMillis() - lastNotificationTime
                    if (notificationCount == 0 || elapsedSinceLast >= 5 * 60 * 1000L) {
                        val extraMinutes = (sessionTime / (60 * 1000)).toInt()
                        startVibration(notificationCount + 1)
                        showAlertNotification(extraMinutes, notificationCount + 1)
                        notificationCount++
                        lastNotificationTime = System.currentTimeMillis()
                    }
                }
//                if (sessionTime >= ((notificationCount * 5) - 5) + thresholdTime) {
////                        val extraTime = sessionTime - (notificationCount * thresholdTime)
//                    val extraMinutes = (sessionTime / (60 * 1000)).toInt()
//                    Log.d(
//                        TAG,
//                        "Usage threshold exceeded, sending alert with extra usage: $extraMinutes min"
//                    )
//                    startVibration(notificationCount + 1)
//                    showAlertNotification(extraMinutes, notificationCount + 1)
//                    notificationCount ++
//                }
            }
            else {
                if (instagramUsageStartTime != 0L) {
                    Log.d(TAG, "Instagram is no longer in foreground, resetting timer")
                    clearAlertNotification()
                }
                instagramUsageStartTime = 0
                sessionTime = 0
                lastNotificationTime = 0L
                notificationCount = 0
            }
            handler.postDelayed(this, CHECK_INTERVAL)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate called, initializing service")
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        val prefs = getSharedPreferences("focuss_prefs", MODE_PRIVATE)
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
                    // 1 is for UsageEvents.Event.MOVE_TO_FOREGROUND
                    1 -> {
                        if (event.timeStamp > lastForegroundTimestamp) {
                            lastForegroundTimestamp = event.timeStamp
                        }
                    }
                    // 2 is for UsageEvents.Event.MOVE_TO_BACKGROUND
                    2 -> {
                        if (event.timeStamp > lastBackgroundTimestamp) {
                            lastBackgroundTimestamp = event.timeStamp
                        }
                    }
                }
            }
        }

        val currentlyForeground = if (lastForegroundTimestamp > 0) {
            lastForegroundTimestamp > lastBackgroundTimestamp
        }
        else {
            false
        }

        // If no events are found, use the persistent state.
        val finalState = if (lastForegroundTimestamp == 0L && lastBackgroundTimestamp == 0L) {
            isInstagramPersistentlyForeground
        }
        else {
            currentlyForeground
        }

        // to Persist the current state for future checks.
        isInstagramPersistentlyForeground = finalState
        Log.d(
            TAG,
            "isInstagramForeground (persistent): $finalState (Last FG: $lastForegroundTimestamp, Last BG: $lastBackgroundTimestamp)"
        )
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

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
//            manager.createNotificationChannel(alertChannel)
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

    private fun startVibration(notificationCount: Int) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        }
        else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = getVibrationPattern(notificationCount)
        Log.e("Vibration, Uservice", "$notificationCount")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, - 1)
            vibrator.vibrate(effect)
            Log.e("vibration", "${pattern.toULongArray()}")
        }
        else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, - 1)
        }
        Log.d(TAG, "Started vibration with pattern for $notificationCount minutes")
    }

    // Displays the alert notification when the usage threshold is exceeded.
    private fun showAlertNotification(extraMinutes: Int, notificationCount: Int) {

        Log.e("notificaton Us", "$notificationCount")
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

//        val dynamicChannelId = "$ALERT_CHANNEL_ID-$extraMinutes"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri: Uri =
                    "${ContentResolver.SCHEME_ANDROID_RESOURCE}://$packageName/${R.raw.custom_sound}".toUri()
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alert Channel for $extraMinutes min",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(soundUri, audioAttributes)
                enableVibration(false)  // Disabled the vibration here as we are using different function for this
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(alertChannel)
        }

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setContentTitle(
                    "$extraMinutes" + getMessage(notificationCount)
                )
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                            .setBigContentTitle(
                                "$extraMinutes" + getMessage(notificationCount)
                            )
                )
                .build()

        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        Log.d(TAG, "Alert notification displayed")
    }

    private fun clearAlertNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ALERT_NOTIFICATION_ID)
    }
}