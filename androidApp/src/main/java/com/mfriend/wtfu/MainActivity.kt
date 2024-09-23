package com.mfriend.wtfu

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.mfriend.wtfu.ui.alarm.AlarmTriggerScreen
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val notificationManager: NotificationManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        super.onCreate(savedInstanceState)
        setContent {
            AlarmApp()
        }
    }
}


@Serializable
object AlarmList
@Serializable
data class AlarmEdit(val id: Int? = null)
@Serializable
data class AlarmTrigger(val id: Int)

@Composable
fun AlarmApp(viewModel: AlarmViewModel = koinViewModel()) {
    WTFUTheme {
        val navController = rememberNavController()
        Surface {
            val alarms by viewModel.alarmsFlow.collectAsState(initial = emptyList())
            NavHost(
                navController = navController,
                startDestination = AlarmList,
            ) {
                composable<AlarmTrigger>(
                    deepLinks = listOf(
                        navDeepLink<AlarmTrigger>(basePath = "https://mrfiend.com/trigger")
                    )
                ) { backstackEntry ->
                    val route: AlarmTrigger = backstackEntry.toRoute()
                    AlarmTriggerScreen(
                        route.id,
                        viewModel,
                        onDismiss = { navController.popBackStack() })
                }
                composable<AlarmList> {
                    AlarmListScreen(
                        alarms = alarms,
                        newAlarm = { navController.navigate("AlarmEdit") },
                        onAlarmClicked = {
                            navController.navigate(
                                AlarmEdit(it.id)
                            )
                        })
                }
                composable<AlarmEdit>(
                ) { backstackEntry ->
                    val route: AlarmEdit = backstackEntry.toRoute()
                    AlarmEditScreen(
                        route.id,
                        alarmSaved = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
