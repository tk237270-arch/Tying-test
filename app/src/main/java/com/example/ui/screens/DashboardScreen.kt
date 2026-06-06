package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ArcadeScreen
import com.example.TypeArcadeViewModel
import com.example.data.ScoreHistory
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: TypeArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val topAesthetic by viewModel.topAestheticScores.collectAsState()
    val topRacer by viewModel.topRacerScores.collectAsState()
    val topLaser by viewModel.topLaserScores.collectAsState()

    var selectedLeaderboardTab by remember { mutableStateOf("aesthetic") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Neon Header Title
        item {
            HeaderSection()
        }

        // Mode Cards
        item {
            Text(
                text = "CHOOSE YOUR MODE",
                color = DarkGreyText,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            ModeCard(
                title = "1. Minimalist Speed",
                description = "Custom duration word test using character metrics. Raw typing stats.",
                tagline = "Monkeytype Vibe",
                accentColor = CyberCyan,
                icon = { Icon(Icons.Default.Speed, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(32.dp)) },
                onClick = { viewModel.navigateTo(ArcadeScreen.AESTHETIC_TEST) },
                testTag = "mode_aesthetic_card"
            )
        }

        item {
            ModeCard(
                title = "2. Competitive Racer",
                description = "Race your Formula car against \"Turbo Bot\". Errors freeze your speed!",
                tagline = "TypeRacer Vibe",
                accentColor = CyberPink,
                icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = CyberPink, modifier = Modifier.size(32.dp)) },
                onClick = { viewModel.navigateTo(ArcadeScreen.RACER_GAME) },
                testTag = "mode_racer_card"
            )
        }

        item {
            ModeCard(
                title = "3. Space Defense",
                description = "Falling word missiles are targeting home base! Type letters to blast them.",
                tagline = "ZType Vibe",
                accentColor = CyberYellow,
                icon = { Icon(Icons.Default.Adjust, contentDescription = null, tint = CyberYellow, modifier = Modifier.size(32.dp)) },
                onClick = { viewModel.navigateTo(ArcadeScreen.LASER_GAME) },
                testTag = "mode_laser_card"
            )
        }

        // Leaderboard Panel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARCADE CABINET RECORD HIGHS",
                    color = DarkGreyText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                IconButton(
                    onClick = { viewModel.navigateTo(ArcadeScreen.HISTORY) },
                    modifier = Modifier.testTag("history_button")
                ) {
                    Icon(Icons.Default.History, contentDescription = "View complete log", tint = CyberCyan)
                }
            }
        }

        item {
            // Mode Select buttons inside HighScore
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("aesthetic" to "MINIMAL", "racer" to "RACER", "laser" to "SPACE")
                tabs.forEach { (modeKey, displayName) ->
                    val isSelected = selectedLeaderboardTab == modeKey
                    val isSelectedColor = when (modeKey) {
                        "racer" -> CyberPink
                        "laser" -> CyberYellow
                        else -> CyberCyan
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyberCard.copy(alpha = 0.9f) else Color.Transparent)
                            .clickable { selectedLeaderboardTab = modeKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) isSelectedColor else DarkGreyText
                        )
                    }
                }
            }
        }

        // Leaderboard values block
        val currentTabScores = when (selectedLeaderboardTab) {
            "aesthetic" -> topAesthetic
            "racer" -> topRacer
            "laser" -> topLaser
            else -> emptyList()
        }

        if (currentTabScores.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = DarkGreyText, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO HIGH SCORES RECORDED YET",
                            color = DarkGreyText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Play a game first to set a record!",
                            color = DarkGreyText.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            items(currentTabScores.take(5)) { indexScore ->
                ScoreRow(rank = currentTabScores.indexOf(indexScore) + 1, item = indexScore)
            }
        }
    }
}

@Composable
fun HeaderSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "title_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TYPE ARCADE",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    color = CyberCyan,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = ">>> CONQUER THE KEYBOARD <<<",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberPink,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Test and boost your Words Per Minute (WPM) accuracy across three stylized retro game formats. Save your scores locally on the arcade leaderboard.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = BrightText.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    tagline: String,
    accentColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BrightText
                    )
                    Text(
                        text = tagline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = DarkGreyText,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ScoreRow(
    rank: Int,
    item: ScoreHistory
) {
    val rankColor = when (rank) {
        1 -> CyberYellow
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> DarkGreyText
    }

    val modeLabel = when (item.mode) {
        "aesthetic" -> "Minimal"
        "racer" -> "Racer"
        "laser" -> "Space"
        else -> item.mode
    }

    val modeColor = when (item.mode) {
        "racer" -> CyberPink
        "laser" -> CyberYellow
        else -> CyberCyan
    }

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(item.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.6f)),
        border = BorderStroke(0.5.dp, DarkGreyText.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(rankColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = modeLabel.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = modeColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 9.sp,
                        color = DarkGreyText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Stats details
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = "${item.wpm.toInt()} WPM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightText,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ACC: ${item.accuracy.toInt()}%",
                    fontSize = 10.sp,
                    color = DarkGreyText,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "+${item.scorePoints}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${item.timeDurationSeconds}s elapsed",
                    fontSize = 9.sp,
                    color = DarkGreyText,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
