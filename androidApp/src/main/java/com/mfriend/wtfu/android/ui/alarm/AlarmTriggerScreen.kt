package com.mfriend.wtfu.android.ui.alarm

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mfriend.wtfu.android.WTFUTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun AlarmTrigger() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DateTimeHeader(Modifier.padding(vertical = 20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {/*TODO*/ },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(text = "Snooze")
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {/*TODO*/ },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun DateTimeHeader(modifier: Modifier = Modifier) {

    var dateTime: LocalDateTime by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            dateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    val dateString by remember { derivedStateOf { formatDate(dateTime.date) } }
    val timeString by remember { derivedStateOf { formatTime(dateTime.time) } }

    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            dateString,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Text(
            timeString,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDate(date: LocalDate): String {
    return date.format(LocalDate.Format {
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        dayOfMonth()
        char(' ')
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    })
}

private fun formatTime(date: LocalTime): String {
    return date.format(LocalTime.Format {
        amPmHour(Padding.NONE)
        char(':')
        minute()
        char(' ')
        amPmMarker("AM", "PM")
    })
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AlarmTriggerPreview() {
    WTFUTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AlarmTrigger()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DateTimeHeaderPreview() {
    WTFUTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DateTimeHeader()
        }
    }
}

