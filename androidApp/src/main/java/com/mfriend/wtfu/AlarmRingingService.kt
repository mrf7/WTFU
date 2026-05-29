package com.mfriend.wtfu

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class AlarmRingingService : Service(), KoinComponent {

    private val database: DatabaseHelper by inject()

    private var currentAlarmId: Int? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
                if (alarmId < 0) {
                    Logger.withTag(TAG).w { "Start requested without valid alarm id" }
                    stopSelf()
                    return START_NOT_STICKY
                }
                if (currentAlarmId != null && currentAlarmId != alarmId) {
                    stopRinging()
                }
                startRinging(alarmId)
            }
            ACTION_STOP -> {
                val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
                if (currentAlarmId == alarmId || currentAlarmId == null) {
                    stopRinging()
                    stopSelf()
                }
            }
            ACTION_REPUBLISH_NOTIFICATION -> {
                val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
                if (alarmId >= 0 && currentAlarmId == alarmId) {
                    Logger.withTag(TAG).w { "Notification dismissed while ringing; re-posting for alarm $alarmId" }
                    startForeground(alarmId, buildNotification(alarmId))
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun startRinging(alarmId: Int) {
        if (!AlarmNotificationPermissions.canPostNotifications(this)) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted; foreground service may fail")
        }

        createNotificationChannel()
        val notification = buildNotification(alarmId)
        try {
            startForeground(alarmId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "startForeground failed", e)
            stopSelf()
            return
        }

        currentAlarmId = alarmId
        val alarm = runBlocking { database.getAlarm(alarmId).first() }
        val soundUri = resolveSoundUri(alarm?.sound ?: "random")
        startAudio(soundUri)
        startVibration()
        Logger.withTag(TAG).d { "Ringing started for alarm $alarmId" }
    }

    private fun stopRinging() {
        releaseAudio()
        stopVibration()
        currentAlarmId?.let { alarmId ->
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(alarmId)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        currentAlarmId = null
        Logger.withTag(TAG).d { "Ringing stopped" }
    }

    private fun resolveSoundUri(sound: String): Uri =
        when (sound) {
            "random" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            else -> sound.toUri()
        }

    private fun startAudio(uri: Uri) {
        releaseAudio()
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .build()
        audioManager.requestAudioFocus(audioFocusRequest!!)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setDataSource(this@AlarmRingingService, uri)
            isLooping = true
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                true
            }
            prepare()
            start()
        }
    }

    private fun releaseAudio() {
        mediaPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    @SuppressLint("MissingPermission")
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 500)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    @SuppressLint("FullScreenIntentPolicy")
    private fun buildNotification(alarmId: Int): Notification {
        val contentIntent = alarmContentPendingIntent(alarmId)
        val fullScreenAllowed = AlarmNotificationPermissions.canUseFullScreenIntent(this)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm")
            .setContentText("Tap to dismiss")
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .setDeleteIntent(notificationDismissedPendingIntent(alarmId))
            .apply {
                if (fullScreenAllowed) {
                    setFullScreenIntent(contentIntent, true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                }
            }
        return builder.build().apply {
            flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        }
    }

    private fun notificationDismissedPendingIntent(alarmId: Int): PendingIntent {
        val intent = Intent(this, AlarmRingingNotificationReceiver::class.java).apply {
            action = ACTION_NOTIFICATION_DISMISSED
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            this,
            notificationDismissRequestCode(alarmId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun notificationDismissRequestCode(alarmId: Int) = alarmId + NOTIFICATION_DISMISS_REQUEST_CODE_OFFSET

    private fun alarmContentPendingIntent(alarmId: Int): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://mrfiend.com/$alarmId".toUri(),
            this,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Alarm ringing", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Ongoing alarm notifications"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(null, null)
            enableVibration(false)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.mfriend.wtfu.action.RINGING_START"
        const val ACTION_STOP = "com.mfriend.wtfu.action.RINGING_STOP"
        const val ACTION_REPUBLISH_NOTIFICATION = "com.mfriend.wtfu.action.REPUBLISH_NOTIFICATION"
        const val ACTION_NOTIFICATION_DISMISSED = "com.mfriend.wtfu.action.NOTIFICATION_DISMISSED"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val CHANNEL_ID = "alarm_ringing"
        private const val NOTIFICATION_DISMISS_REQUEST_CODE_OFFSET = 2_000_000
        private const val TAG = "AlarmRingingService"
    }
}
