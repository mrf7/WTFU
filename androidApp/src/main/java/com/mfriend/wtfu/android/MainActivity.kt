package com.mfriend.wtfu.android

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("AlarmEdit") }) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Alarm")
                }
            }
        ) { padding ->
            val alarms by viewModel.alarmsFlow.collectAsState(initial = emptyList())
            NavHost(
                navController = navController,
                startDestination = "Test",
                Modifier.padding(padding)
            ) {
                composable("AlarmList") {
                    AlarmListScreen(alarms = alarms) {
                        navController.navigate(
                            "AlarmEdit?alarm=${it.id}"
                        )
                    }
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

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview() {
    AlarmApplication()
}
