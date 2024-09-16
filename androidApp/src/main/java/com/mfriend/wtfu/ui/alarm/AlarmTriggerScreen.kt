package com.mfriend.wtfu.ui.alarm

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfriend.wtfu.Alarm
import com.mfriend.wtfu.AlarmViewModel
import com.mfriend.wtfu.MathMission
import com.mfriend.wtfu.RepeatMode
import com.mfriend.wtfu.WTFUTheme
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun AlarmTriggerScreen(alarmId: Int, viewModel: AlarmViewModel, onDismiss: () -> Unit) {
    val alarm by viewModel.getAlarm(alarmId).collectAsState(null)
    AlarmScreenInner(alarm, onDismiss)
}

@Composable
private fun AlarmScreenInner(alarm: Alarm?, onDismiss: () -> Unit) {
    var dismiss by remember { mutableStateOf(false) }
    if (dismiss) {
        AlarmDismiss(alarm, onDismiss)
    } else {
        AlarmTrigger { dismiss = true }
    }
}

@Composable
private fun AlarmDismiss(alarm: Alarm?, onDismiss: () -> Unit) {
    MathMissionScreen(onDismiss = onDismiss)
}

@Composable
private fun MathMissionScreen(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    var answerText by remember { mutableStateOf("") }

    val numbers = remember {
        Pair(Random.nextInt(10, 100), Random.nextInt(10, 100))
    }

    fun checkAnswer() = answerText.toInt() == numbers.first + numbers.second
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().weight(0.5f), verticalArrangement = Arrangement.Center) {
            Text(
                "${numbers.first} + ${numbers.second} = ",
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                answerText,
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 50.dp)
                    .border(1.dp, Color.Black)
                    .sizeIn(200.dp),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.End
            )
        }
        NumberEntry(
            Modifier.fillMaxWidth().weight(0.5f),
            answerText,
            onValueChange = { answerText = it },
            onSubmit = { if (checkAnswer()) onDismiss() }
        )
    }
}

// Using a string here is jank, but made pulling it into a function quicker. TODO make this better/more generic to use
@Composable
private fun NumberEntry(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier) {
        (9 downTo 1).chunked(3).forEach { rowNums ->
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.Center) {
                rowNums.reversed().forEach {
                    Button(
                        onClick = { onValueChange(value + it.toString()) },
                        Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(5)
                    ) {
                        Text(it.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { onValueChange(value.dropLast(1)) },
                Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(5)
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }

            Button(
                onClick = { onValueChange(value + 0.toString()) },
                Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(5)
            ) {
                Text("0", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = onSubmit,
                Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(5)
            ) {
                Icon(Icons.Default.Check, "Done")
            }
        }
    }
}

@Composable
private fun AlarmTrigger(onDismiss: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DateTimeHeader(Modifier.padding(vertical = 20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { /*TODO*/ },
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(text = "Snooze")
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onDismiss,
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
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
fun MathMissionPreview() {
    WTFUTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MathMissionScreen {}
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AlarmTriggerPreview() {
    WTFUTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AlarmTrigger {}
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun AlarmScreenPreview() {
    WTFUTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AlarmScreenInner(alarm = Alarm(3, 11, repeat = RepeatMode.OneTime, missions = MathMission())) {}
        }
    }
}
