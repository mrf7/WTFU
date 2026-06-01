package com.mfriend.wtfu

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

object AlarmNotificationPermissions {
    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED

    /**
     * On Android 14+, [android.Manifest.permission.USE_FULL_SCREEN_INTENT] is not auto-granted;
     * the user must allow full-screen notifications in app settings for
     * [android.app.Notification.Builder.setFullScreenIntent] to launch the UI over the lock screen.
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?: return false
        return notificationManager.canUseFullScreenIntent()
    }

    /**
     * Creates an intent that leads to the setting to allow full screen notifications
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun fullScreenIntentSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    /**
     * [AlarmManager.canScheduleExactAlarms] can incorrectly return false on some OEMs after reboot
     * even when [Manifest.permission.USE_EXACT_ALARM] is held.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_EXACT_ALARM) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        val alarmManager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return false
        return alarmManager.canScheduleExactAlarms()
    }
}
