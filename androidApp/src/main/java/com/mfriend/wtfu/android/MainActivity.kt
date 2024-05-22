package com.mfriend.wtfu.android

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mfriend.wtfu.AlarmViewModel
import com.mfriend.wtfu.android.ui.alarm.AlarmTriggerScreen
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private val notificationManager: NotificationManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        notificationManager.cancel(3)
        super.onCreate(savedInstanceState)
        setContent {
            AlarmApp()
        }
    }
}

@Composable
fun AlarmApp(viewModel: AlarmViewModel = koinViewModel()) {
    WTFUTheme {
        val navController = rememberNavController()
        Surface {
            val alarms by viewModel.alarmsFlow.collectAsState(initial = emptyList())
            NavHost(
                navController = navController,
                startDestination = "AlarmList",
            ) {
                composable("AlarmTrigger?alarm={alarm}", arguments = listOf(navArgument("alarm") {
                    type= NavType.ReferenceType
                })) { backstackEntry ->
                    AlarmTriggerScreen(backstackEntry.arguments!!.getInt("alarm"),viewModel)
                }
                composable("AlarmList") {
                    AlarmListScreen(
                        alarms = alarms,
                        newAlarm = { navController.navigate("AlarmEdit") },
                        onAlarmClicked = {
                            navController.navigate(
                                "AlarmTrigger?alarm=${it.id}"
                            )
                        })
                }
                composable(
                    "AlarmEdit?alarm={alarm}",
                    arguments = listOf(navArgument("alarm") {
                        type = NavType.IntType
                        defaultValue = -1
                    })
                ) { backstackEntry ->
                    AlarmEditScreen(
                        backstackEntry.arguments!!.getInt("alarm"),
                        alarmSaved = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
