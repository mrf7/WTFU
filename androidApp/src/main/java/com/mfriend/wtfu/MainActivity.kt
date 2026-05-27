package com.mfriend.wtfu

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mfriend.wtfu.navigation.AlarmEdit
import com.mfriend.wtfu.navigation.AlarmList
import com.mfriend.wtfu.navigation.AlarmTrigger
import com.mfriend.wtfu.navigation.resolveDeepLinkForExistingTask
import com.mfriend.wtfu.navigation.resolveStartKeys
import com.mfriend.wtfu.ui.alarm.AlarmTriggerScreen
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val notificationManager: NotificationManager by inject()
    private val pendingDeepLink = mutableStateOf<NavKey?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        super.onCreate(savedInstanceState)
        setContent {
            AlarmApp(
                startKeys = resolveStartKeys(intent),
                deepLinkKey = pendingDeepLink.value,
                onDeepLinkHandled = { pendingDeepLink.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink.value = resolveDeepLinkForExistingTask(intent)
    }
}

@Composable
fun AlarmApp(
    startKeys: List<NavKey>,
    deepLinkKey: NavKey? = null,
    onDeepLinkHandled: () -> Unit = {},
    viewModel: AlarmViewModel = koinViewModel(),
) {
    WTFUTheme {
        val backStack = rememberNavBackStack(*startKeys.toTypedArray())
        val alarms by viewModel.alarmsFlow.collectAsState(initial = emptyList())

        LaunchedEffect(deepLinkKey) {
            val key = deepLinkKey ?: return@LaunchedEffect
            backStack.add(key)
            onDeepLinkHandled()
        }

        Surface {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AlarmList> {
                        AlarmListScreen(
                            alarms = alarms,
                            newAlarm = { backStack.add(AlarmEdit()) },
                            onAlarmClicked = { backStack.add(AlarmEdit(id = it.id)) },
                        )
                    }
                    entry<AlarmEdit> { key ->
                        AlarmEditScreen(
                            alarmId = key.id,
                            alarmSaved = { backStack.removeLastOrNull() },
                            viewModel = viewModel,
                        )
                    }
                    entry<AlarmTrigger> { key ->
                        AlarmTriggerScreen(
                            alarmId = key.id,
                            viewModel = viewModel,
                            onDismiss = { backStack.removeLastOrNull() },
                        )
                    }
                },
            )
        }
    }
}
