package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ArcadeScreen
import com.example.TypeArcadeViewModel
import com.example.ui.theme.*

@Composable
fun HistoryScreen(
    viewModel: TypeArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val allHistory by viewModel.allHistory.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                modifier = Modifier.testTag("history_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberCyan)
            }

            Text(
                text = "LOG RECORDS",
                color = CyberCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            IconButton(
                onClick = { if (allHistory.isNotEmpty()) showDeleteConfirm = true },
                enabled = allHistory.isNotEmpty(),
                modifier = Modifier.testTag("wipe_history_button")
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear logs",
                    tint = if (allHistory.isNotEmpty()) CyberPink else DarkGreyText.copy(alpha = 0.4f)
                )
            }
        }

        // Wipe confirm alert dialog overlay
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = {
                    Text(
                        text = "WIPE HIGH SCORES?",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyberPink,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "This will permanently wipe all local leaderboards and past stats from the database. Are you sure you want to proceed?",
                        fontFamily = FontFamily.Monospace,
                        color = BrightText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                containerColor = CyberCard,
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllHistory()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "YES, WIPE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberBackground
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false }
                    ) {
                        Text(
                            text = "CANCEL",
                            fontFamily = FontFamily.Monospace,
                            color = BrightText
                        )
                    }
                }
            )
        }

        // Main scores list log
        if (allHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberCard)
                    .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.15f)), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = DarkGreyText,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NO LOCAL LOGS CURRENTLY SAVED",
                        color = BrightText,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Play Speed Tests, Competitions, or Base Defenses dynamically to populate high scores.",
                        color = DarkGreyText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allHistory) { historyEntry ->
                    ScoreRow(rank = allHistory.indexOf(historyEntry) + 1, item = historyEntry)
                }
            }
        }
    }
}
