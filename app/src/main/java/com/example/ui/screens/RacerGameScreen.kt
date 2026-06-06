package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ArcadeScreen
import com.example.TypeArcadeViewModel
import com.example.ui.theme.*

@Composable
fun RacerGameScreen(
    viewModel: TypeArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val countdown by viewModel.racerCountdown.collectAsState()
    val targetText by viewModel.racerTargetText.collectAsState()
    val inputText by viewModel.racerInputText.collectAsState()
    val playerProgress by viewModel.playerProgress.collectAsState()
    val botProgress by viewModel.botProgress.collectAsState()
    val difficulty by viewModel.racerDifficulty.collectAsState()
    val wpm by viewModel.racerWpm.collectAsState()
    val accuracy by viewModel.racerAccuracy.collectAsState()
    val winner by viewModel.racerWinner.collectAsState()
    val isPlaying by viewModel.racerIsPlaying.collectAsState()
    val isFinished by viewModel.racerIsFinished.collectAsState()

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Prepare racetrack initially
        viewModel.startRacerGame()
    }

    // Force keyboard focus when race countdown ends or starts
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            focusRequester.requestFocus()
        }
    }

    // Countdown scale pulse
    val countdownScale = remember { Animatable(1f) }
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            countdownScale.snapTo(0.7f)
            countdownScale.animateTo(
                targetValue = 1.6f,
                animationSpec = tween(400, easing = EaseOutBack)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                    modifier = Modifier.testTag("racer_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberPink)
                }

                Text(
                    text = "COMPETITIVE RACER",
                    color = CyberPink,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // Placeholder
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Difficulty selections selector (Visible only if not racing)
            if (!isPlaying && !isFinished && countdown == -1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = " BOT SKILL:",
                        color = DarkGreyText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val difficulties = listOf("Easy", "Medium", "Hard")
                        difficulties.forEach { diff ->
                            val isSel = difficulty == diff
                            val actColor = when (diff) {
                                "Easy" -> CyberGreen
                                "Medium" -> CyberYellow
                                "Hard" -> CyberPink
                                else -> CyberCyan
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) actColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        BorderStroke(
                                            if (isSel) 1.dp else 0.dp,
                                            if (isSel) actColor.copy(alpha = 0.5f) else Color.Transparent
                                        ),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { viewModel.changeRacerDifficulty(diff) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = diff.uppercase(),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) actColor else DarkGreyText
                                )
                            }
                        }
                    }
                }
            }

            // Racetrack Visual Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.5.dp, CyberPink.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Track 1: Player
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(22.dp)
                                .background(CyberCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Visual track
                        RacetrackLane(
                            progress = playerProgress,
                            carColor = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Divider strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkGreyText.copy(alpha = 0.2f))
                    )

                    // Track 2: Turbo Bot
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(22.dp)
                                .background(CyberPink.copy(alpha = 0.12f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BOT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPink,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        RacetrackLane(
                            progress = botProgress,
                            carColor = CyberPink,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Real-time Standings and metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val standingText = if (playerProgress >= botProgress) "1st PLACE (LEAD)" else "2nd PLACE (TRAILING)"
                val standingColor = if (playerProgress >= botProgress) CyberGreen else CyberPink

                Text(
                    text = standingText,
                    color = standingColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.background(standingColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "SPEED: ${wpm.toInt()} WPM",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ACC: ${accuracy.toInt()}%",
                        color = CyberYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Paragraph to type text rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberCard)
                    .border(BorderStroke(1.dp, CyberPink.copy(alpha = if (isPlaying) 0.5f else 0.12f)), RoundedCornerShape(14.dp))
                    .clickable { if (isPlaying) focusRequester.requestFocus() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (countdown > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = countdown.toString(),
                            fontSize = (48f * countdownScale.value).sp,
                            fontWeight = FontWeight.Black,
                            color = CyberPink,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PREPARING ENGINES...",
                            fontSize = 11.sp,
                            color = DarkGreyText,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (countdown == 0) {
                    Text(
                        text = "GO!",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberGreen,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    // Render paragraphs text with typo constraints
                    var hasTypo = false
                    val annotatedText = buildAnnotatedString {
                        for (i in targetText.indices) {
                            val tarChar = targetText[i]
                            val isTyped = i < inputText.length
                            val isMatch = isTyped && inputText[i] == tarChar

                            if (isTyped && !hasTypo) {
                                if (isMatch) {
                                    withStyle(SpanStyle(color = CyberGreen, fontWeight = FontWeight.Bold)) {
                                        append(tarChar)
                                    }
                                } else {
                                    // Found typo! Any further characters are frozen
                                    hasTypo = true
                                    withStyle(SpanStyle(background = CyberPink.copy(alpha = 0.5f), color = BrightText, fontWeight = FontWeight.Black)) {
                                        if (tarChar == ' ') append("⎵") else append(tarChar)
                                    }
                                }
                            } else {
                                // Remaining chars
                                withStyle(SpanStyle(color = DarkGreyText.copy(alpha = 0.45f))) {
                                    append(tarChar)
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = annotatedText,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BrightText,
                            lineHeight = 26.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // If has typo, show correction alert banner
                        if (hasTypo) {
                            Text(
                                text = ">>> TYPO DISCOVERED! PRESS BACKSPACE TO CORRECT <<<",
                                color = CyberPink,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberPink.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        } else if (isPlaying && inputText.isEmpty()) {
                            Text(
                                text = ">>> RACING ACTIVE: BEGIN TYPING NOW <<<",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Basic text input captures focused keys
            if (isPlaying) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateRacerInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("racer_text_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    placeholder = { Text("Type here to race...", color = DarkGreyText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPink,
                        unfocusedBorderColor = DarkGreyText,
                        focusedTextColor = BrightText
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false
                    )
                )
            } else if (!isFinished && countdown == -1) {
                // Initial launch prompt
                Button(
                    onClick = { viewModel.startRacerGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("racer_start_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START NEW RACE",
                        fontFamily = FontFamily.Monospace,
                        color = CyberBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Victory Popup Screen
        AnimatedVisibility(
            visible = isFinished,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("racer_victory_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(2.dp, if (winner == "Player") CyberGreen else CyberPink)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (winner == "Player") "🏁 VICTORY! 🏁" else "🤖 BOT WIN 🤖",
                            color = if (winner == "Player") CyberGreen else CyberPink,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = if (winner == "Player") {
                                "You crossed the finish line first! Excellent speed."
                            } else {
                                "Turbo Bot finished ahead. Practice correcting typos faster!"
                            },
                            color = BrightText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBackground.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${wpm.toInt()} WPM",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (winner == "Player") CyberGreen else CyberPink,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "LIVE RACING VELOCITY",
                                    fontSize = 10.sp,
                                    color = DarkGreyText,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "${accuracy.toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyberYellow, fontFamily = FontFamily.Monospace)
                                Text(text = "ACCURACY", fontSize = 9.sp, color = DarkGreyText, fontFamily = FontFamily.Monospace)
                            }
                            Column {
                                val scoreReward = if (winner == "Player") (wpm * 10).toInt() + 500 else (wpm * 10).toInt() + 200
                                Text(text = "+$scoreReward", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CyberGreen, fontFamily = FontFamily.Monospace)
                                Text(text = "RATING PTS", fontSize = 9.sp, color = DarkGreyText, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Button(
                            onClick = { viewModel.startRacerGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (winner == "Player") CyberGreen else CyberPink),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("racer_victory_retry_button")
                        ) {
                            Text(
                                text = "RACE AGAIN",
                                color = CyberBackground,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                            border = BorderStroke(1.dp, BrightText.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("racer_victory_exit_button")
                        ) {
                            Text(
                                text = "EXIT TO TERMINAL",
                                color = BrightText,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RacetrackLane(
    progress: Float,
    carColor: Color,
    modifier: Modifier = Modifier
) {
    // Row of dots represents progress path. Car is visual wedge. Checkered flags at end.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track visual guide lines
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2f
            drawLine(
                color = DarkGreyText.copy(alpha = 0.15f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 2f
            )

            // Draw Checkered Line
            val flagWidth = 12f
            val squareSize = size.height / 6f
            for (col in 0..1) {
                val startX = size.width - 24f + (col * flagWidth)
                for (row in 0..5) {
                    val color = if ((row + col) % 2 == 0) Color.White else Color.Black
                    drawRect(
                        color = color,
                        topLeft = Offset(startX, row * squareSize),
                        size = androidx.compose.ui.geometry.Size(flagWidth, squareSize)
                    )
                }
            }
        }

        // Draw the car based on animation coordinates
        BoxWithConstraints {
            val maxWidth = maxWidth
            val offsetDp = (progress * (maxWidth.value - 36f)).coerceIn(0f, maxWidth.value - 36f).dp

            Box(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .size(24.dp, 20.dp)
                    .background(carColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                    .border(BorderStroke(1.dp, carColor), RoundedCornerShape(4.dp))
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                // Vector car arrow
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerY = size.height / 2f
                    val path = Path().apply {
                        // draw clean sports car wedge pointing right outline
                        moveTo(4f, centerY - 6f)
                        lineTo(20f, centerY)
                        lineTo(4f, centerY + 6f)
                        close()
                    }
                    drawPath(path = path, color = carColor)
                }
            }
        }
    }
}
