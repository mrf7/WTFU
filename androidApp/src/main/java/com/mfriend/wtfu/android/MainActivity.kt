package com.mfriend.wtfu.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
            backgroundColor = MaterialTheme.colors.primarySurface,
            floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate("AlarmEdit") }) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Alarm")
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "AlarmList",
                Modifier.padding(padding)
            ) {
                composable("AlarmList") { AlarmListScreen(alarms = viewModel.alarms) }
                composable(
                    "AlarmEdit?alarm",
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
