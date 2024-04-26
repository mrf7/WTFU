package com.mfriend.wtfu.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@Composable
fun AlarmSetScreen() {
    Column {
        val context = LocalContext.current
        Button(onClick = {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: throw RuntimeException("Couldnt get alarm")
            val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("ID", 0) }
            val time = Clock.System.now().plus(15.seconds).epochSeconds
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }) {
            Text("Set Alarm")
        }
    }
}

@Preview
@Composable
fun AlarmSetPreview() {
    AlarmSetScreen()
}