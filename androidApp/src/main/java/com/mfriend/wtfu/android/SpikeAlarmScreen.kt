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
import java.time.ZoneId

@Composable
fun AlarmSetScreen() {
    Column {
        val context = LocalContext.current
        Button(onClick = {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                ?: throw RuntimeException("Couldnt get alarm")
            val intent = Intent(context, MainActivity::class.java).let {
                it.setAction(Intent.ACTION_MAIN)
                it.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                PendingIntent.getActivity(context, 1, it, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, java.time.LocalDateTime.now().plusSeconds(30).atZone(
                    ZoneId.systemDefault()
                ).toEpochSecond(), intent
            )
        }) {
            Text("Set Alarm")
        }
    }
}

@Preview
@Composable
fun AlarmSetPreview() {

}