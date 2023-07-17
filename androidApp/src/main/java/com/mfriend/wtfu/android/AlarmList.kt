package com.mfriend.wtfu.android

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mfriend.wtfu.Alarm
import com.mfriend.wtfu.RepeatMode
import com.mfriend.wtfu.toTimeString
import java.time.DayOfWeek

@Composable
fun AlarmList(alarms: List<Alarm>) {
    LazyColumn {
        items(alarms) {
            AlarmCard(alarm = it, Modifier.padding(horizontal = 5.dp, vertical = 5.dp))
        }
    }
}

// TODO colors are weird af
@Composable
private fun AlarmCard(alarm: Alarm, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                val repeatText = when (val repeat = alarm.repeat) {
                    is RepeatMode.Custom -> repeat.days.joinToString { it.name.take(3) }
                    RepeatMode.Weekdays -> "Weekdays"
                    RepeatMode.Weekends -> "Weekends"
                    RepeatMode.OneTime -> "One Time"
                }
                Text(repeatText, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onSurface)
                Text(alarm.toTimeString(), style = MaterialTheme.typography.h4, color = MaterialTheme.colors.onSurface)
            }
            Column(Modifier.align(Alignment.CenterVertically), Arrangement.Center) {
                Switch(checked = alarm.enabled, onCheckedChange = {})
            }
        }

    }
}

@Preview(showBackground = true, device = "id:pixel_6", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 300)
//@Preview(showBackground = true, device = "id:pixel_6", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 300)
@Composable
fun AlarmListPreview() {
    WTFUTheme {
        AlarmList(
            alarms = listOf(
                Alarm(4, 0, RepeatMode.Weekends, false),
                Alarm(12, 10, RepeatMode.Weekdays),
                Alarm(8, 8, RepeatMode.OneTime, false),
                Alarm(10, 10, RepeatMode.Custom(listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)))
            )
        )
    }
}
