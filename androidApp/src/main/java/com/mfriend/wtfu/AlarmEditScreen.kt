package com.mfriend.wtfu

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlarmEditScreen(alarmId: Int?, viewModel: AlarmViewModel = koinViewModel(), alarmSaved: () -> Unit) {
    val alarm by viewModel.getAlarm(alarmId?: -1).collectAsState(null)
    Scaffold { padding ->
        AlarmEdit(
            Modifier.padding(padding), alarm,
            {
                viewModel.saveAlarm(it)
                alarmSaved()
            },
            viewModel::scheduleAlarm
        )
    }
}

@Composable
private fun AlarmEdit(
    modifier: Modifier = Modifier,
    alarm: Alarm? = null,
    addAlarm: (Alarm) -> Unit,
    onSchedule: (Alarm) -> Unit
) {
    val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var tempAlarm by remember(alarm) {
        mutableStateOf(
            alarm?.copy() ?: Alarm(
                hour = time.hour,
                minute = time.minute,
                repeat = RepeatMode.OneTime
            )
        )
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        var showRepeatMode by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        if (showTimePicker) {
            TimePickerViewDialog(
                initialHour = tempAlarm.hour,
                initialMinute = tempAlarm.minute,
                onCancel = { showTimePicker = false },
                onConfirm = { hour, minute ->
                    tempAlarm = tempAlarm.copy(hour, minute)
                    showTimePicker = false
                })
        }
        if (showRepeatMode) {
            RepeatPickerDialog(
                repeatMode = tempAlarm.repeat,
                onDismiss = { showRepeatMode = false },
                onConfirm = { tempAlarm = tempAlarm.copy(repeat = it) }
            )
        }

        TimeRepeatCard(
            tempAlarm,
            showRepeatDialog = { showRepeatMode = true },
            showTimePicker = { showTimePicker = true }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .height(50.dp)
                    .padding(10.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Snooze")
                Spacer(modifier = Modifier.weight(1f))
                Text(text = tempAlarm.snooze?.let { "$it minutes" } ?: "Off")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        MissionsCard(
            mission = tempAlarm.missions,
            onNewMissions = {},
            addMission = { tempAlarm = tempAlarm.copy(missions = MathMission()) })
        Spacer(modifier = Modifier.height(10.dp))

        /**todo navigate**/
        SoundCard(tempAlarm.sound) { /**todo navigate**/ }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { addAlarm(tempAlarm) }) {
            Text("Save")
        }
        Button(onClick = { onSchedule(tempAlarm) }) {
            Text("Schedule")
        }
    }
}

@Composable
private fun TimeRepeatCard(
    tempAlarm: Alarm,
    showRepeatDialog: () -> Unit,
    showTimePicker: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = tempAlarm.toTimeString(),
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = showTimePicker),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = showRepeatDialog, onClickLabel = "Change Repeat")
        ) {
            Text(text = "Repeat", Modifier.padding(10.dp))
            Text(text = tempAlarm.repeat.localizeString(), Modifier.padding(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatPickerDialog(
    repeatMode: RepeatMode,
    onDismiss: () -> Unit,
    onConfirm: (RepeatMode) -> Unit
) {
    val days = if (repeatMode is RepeatMode.Custom) repeatMode.days else emptyList()
    // just switch to list
    val selected = remember {
        mutableStateListOf<Boolean>().also { list ->
            DayOfWeek.values().forEach {
                list.add(days.contains(it))
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp)
    ) {
        Column {
            Text(
                "Repeat",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Card(
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.clickable {
                        RepeatMode.Weekdays.days.forEach {
                            selected[it.ordinal] = true
                        }
                    }
                ) {
                    Text("+Weekdays", Modifier.padding(5.dp))
                }
                Card(
                    border = BorderStroke(1.dp, Color.Black),
                    modifier = Modifier.clickable {
                        RepeatMode.Weekends.days.forEach {
                            selected[it.ordinal] = true
                        }
                    }
                ) {
                    Text("+Weekends", Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }

            DayOfWeek.values().forEachIndexed { index, dayOfWeek ->
                Row {
                    Checkbox(
                        checked = selected[index],
                        onCheckedChange = { selected[index] = it })
                    Text(
                        dayOfWeek.name,
                        Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
            Button(onClick = {
                val selectedDays =
                    DayOfWeek.values().filterIndexed { index, _ -> selected[index] }.toSet()
                val repeat = when {
                    selectedDays.isEmpty() -> RepeatMode.OneTime
                    selectedDays == RepeatMode.Weekdays.days -> RepeatMode.Weekdays
                    selectedDays == RepeatMode.Weekends.days -> RepeatMode.Weekends
                    selectedDays == RepeatMode.EveryDay.days -> RepeatMode.EveryDay
                    else -> RepeatMode.Custom(selectedDays)
                }
                onConfirm(repeat)
                onDismiss()
            }) {
                Text(
                    "Done",
                    Modifier
                        .fillMaxWidth()
                        .padding(5.dp), textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun MissionsCard(
    mission: Mission?,
    onNewMissions: (missions: List<Mission>) -> Unit,
    addMission: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                if (mission != null)
                    Surface(
                        color = MaterialTheme.colorScheme.onSecondary,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .width(150.dp)
                            .height(80.dp)
                            .padding(10.dp)
                    ) {
                        Column(
                            Modifier.padding(5.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Icon(Icons.Default.AccountBox, mission.name)
                            Text(text = mission.name)
                        }
                    }
            }
            if (mission == null) {
                Surface(
                    color = MaterialTheme.colorScheme.onSecondary,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp)
                        .padding(10.dp)
                        .clickable(onClick = addMission)
                ) {
                    Column(
                        Modifier.padding(5.dp),
                        verticalArrangement = Arrangement.SpaceAround,
                    ) {
                        Icon(Icons.Default.AccountBox, "add")
                        Text(text = "Add Mission")
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundCard(sound: String, showSoundPicker: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(text = "Sound", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = sound)
        }
    }
}


@Composable
fun TimePickerViewDialog(
    initialHour: Int,
    initialMinute: Int,
    onCancel: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val context = LocalContext.current
    val timePicker = TimePickerDialog(context, { _, selectedHour: Int, selectedMinute: Int ->
        onConfirm(selectedHour, selectedMinute)
    }, initialHour, initialMinute, false)
    DisposableEffect(true) {
        timePicker.show()
        timePicker.setOnDismissListener { onCancel() }
        onDispose {
            timePicker.cancel()
        }
    }
}

@Preview(device = "id:pixel_5", showSystemUi = true)
@Composable
fun AlarmEditScreenPreview() {
    WTFUTheme {
        AlarmEdit(alarm = null, addAlarm = {}, onSchedule = {})
    }
}

@Preview
@Composable
fun TimePickerPreview() {
    WTFUTheme {
        TimePickerViewDialog(
            initialHour = 10,
            initialMinute = 30,
            onCancel = {},
            onConfirm = { _, _ -> })
    }
}

@Preview
@Composable
fun RepeatDialogPreview() {
    WTFUTheme {
        RepeatPickerDialog(
            repeatMode = RepeatMode.Weekends,
            onDismiss = {},
            onConfirm = {})
    }
}

@Preview(device = "id:pixel_5")
@Composable
fun MissionsCardPreview() {
    MissionsCard(mission = MathMission(), {}, {})
}

@Preview
@Composable
fun SoundCardPreview() {
    WTFUTheme {
        SoundCard(sound = "random", showSoundPicker = {})
    }
}
