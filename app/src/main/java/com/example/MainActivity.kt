package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TypeArcadeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports border notch coverage elegantly
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            ArcadeScreen.DASHBOARD -> {
                                DashboardScreen(viewModel = viewModel)
                            }
                            ArcadeScreen.AESTHETIC_TEST -> {
                                AestheticTestScreen(viewModel = viewModel)
                            }
                            ArcadeScreen.RACER_GAME -> {
                                RacerGameScreen(viewModel = viewModel)
                            }
                            ArcadeScreen.LASER_GAME -> {
                                LaserGameScreen(viewModel = viewModel)
                            }
                            ArcadeScreen.HISTORY -> {
                                HistoryScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Wrapper to hold screen content bounds
@Composable
fun Box(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}
