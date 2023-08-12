package com.mfriend.wtfu.android

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mfriend.wtfu.Alarm
import com.mfriend.wtfu.RepeatMode
import com.mfriend.wtfu.localizeString
import com.mfriend.wtfu.toTimeString
import java.time.DayOfWeek

@Composable
fun AlarmListScreen(alarms: List<Alarm>, onAlarmClicked: (Alarm) -> Unit) {
    LazyColumn {
        items(alarms) {
            AlarmCard(
                alarm = it,
                Modifier
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .clickable { onAlarmClicked(it) }
            )
        }
    }
}

// TODO colors are weird af
@Composable
fun AlarmCard(alarm: Alarm, modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.padding(10.dp)) {
                val repeatText = alarm.repeat.localizeString()
                Text(repeatText, style = MaterialTheme.typography.labelSmall)
                Text(alarm.toTimeString(), style = MaterialTheme.typography.headlineLarge)
            }
            Spacer(Modifier.weight(1f))
            Switch(checked = alarm.enabled, onCheckedChange = {}, modifier = Modifier.padding(end = 10.dp))
        }
    }
}


val sampleAlarms = listOf(
    Alarm(4, 0, RepeatMode.Weekends, enabled = false),
    Alarm(12, 10, RepeatMode.Weekdays),
    Alarm(8, 8, RepeatMode.OneTime, enabled = false),
    Alarm(10, 10, RepeatMode.Custom(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)))
)

@Preview(showBackground = true, device = "id:pixel_6", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 300)
//@Preview(showBackground = true, device = "id:pixel_6", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 300)
@Composable
fun AlarmListPreview() {
    WTFUTheme {
        AlarmListScreen(
            alarms = sampleAlarms,
            {}
        )
    }
}
