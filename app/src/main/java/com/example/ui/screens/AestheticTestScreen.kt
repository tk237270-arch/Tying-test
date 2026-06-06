package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
fun AestheticTestScreen(
    viewModel: TypeArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val targetText by viewModel.aestheticTargetText.collectAsState()
    val inputText by viewModel.aestheticInputText.collectAsState()
    val timer by viewModel.aestheticTimer.collectAsState()
    val wpm by viewModel.aestheticWpm.collectAsState()
    val accuracy by viewModel.aestheticAccuracy.collectAsState()
    val isActive by viewModel.aestheticIsActive.collectAsState()
    val isFinished by viewModel.aestheticIsFinished.collectAsState()

    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Trigger loading text
    LaunchedEffect(Unit) {
        viewModel.resetAestheticTest()
        // Wait a small tick, then request focus to bring up keyboard automatically
        focusRequester.requestFocus()
    }

    // High fidelity blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(530, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_fade"
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                    modifier = Modifier.testTag("aesthetic_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberCyan)
                }

                Text(
                    text = "AESTHETIC SPEED TEST",
                    color = CyberCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                // Placeholder to balance
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Quick Selector Pills (Disable during play)
            if (!isActive && !isFinished) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Duration selections
                    viewModel.wordDurationOptions.forEach { opt ->
                        val isSel = selectedDuration == opt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyberCard.copy(alpha = 0.9f) else Color.Transparent)
                                .clickable { viewModel.changeAestheticDuration(opt) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${opt}S",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CyberCyan else DarkGreyText
                            )
                        }
                    }

                    // Divider line
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkGreyText.copy(alpha = 0.3f)).align(Alignment.CenterVertically))

                    // Source selection
                    viewModel.textSourceOptions.forEach { src ->
                        val isSel = selectedSource == src
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyberCard.copy(alpha = 0.9f) else Color.Transparent)
                                            .clickable { viewModel.changeAestheticSource(src) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = src.uppercase(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) CyberCyan else DarkGreyText
                            )
                        }
                    }
                }
            }

            // Live Metrics Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricIndicator(label = "TIME REMAINING", value = "${timer}s", color = if (timer <= 5) CyberPink else CyberCyan)
                MetricIndicator(label = "WORDS / MIN", value = wpm.toInt().toString(), color = CyberGreen)
                MetricIndicator(label = "ACCURACY", value = "${accuracy.toInt()}%", color = CyberYellow)
            }

            // The main typing block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberCard)
                    .border(BorderStroke(1.dp, CyberCyan.copy(alpha = if (isActive) 0.5f else 0.15f)), RoundedCornerShape(16.dp))
                    .clickable { focusRequester.requestFocus() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Invisible input interceptor
                BasicTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateAestheticInput(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .testTag("aesthetic_text_input"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent) // Completely invis
                )

                // Beautifully formatted styled characters display
                val annotatedText = buildAnnotatedString {
                    for (i in targetText.indices) {
                        val tarChar = targetText[i]
                        val isTyped = i < inputText.length
                        val isMatch = isTyped && inputText[i] == tarChar

                        when {
                            isTyped -> {
                                if (isMatch) {
                                    // Correct character
                                    withStyle(SpanStyle(color = CyberGreen, fontWeight = FontWeight.Bold)) {
                                        append(tarChar)
                                    }
                                } else {
                                    // Wrong character. If it was an untyped space, show understreak
                                    if (tarChar == ' ') {
                                        withStyle(SpanStyle(color = CyberPink, fontWeight = FontWeight.Black)) {
                                            append("⎵")
                                        }
                                    } else {
                                        withStyle(SpanStyle(color = CyberPink, fontWeight = FontWeight.Bold)) {
                                            append(tarChar)
                                        }
                                    }
                                }
                            }
                            i == inputText.length -> {
                                // Dynamic highlighted cursor character
                                withStyle(
                                    SpanStyle(
                                        background = CyberCyan.copy(alpha = cursorAlpha),
                                        color = CyberBackground,
                                        fontWeight = FontWeight.Black
                                    )
                                ) {
                                    append(tarChar)
                                }
                            }
                            else -> {
                                // Dimmed remaining letters
                                withStyle(SpanStyle(color = DarkGreyText.copy(alpha = 0.5f))) {
                                    append(tarChar)
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = annotatedText,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BrightText,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isActive && inputText.isEmpty()) {
                        Text(
                            text = ">>> TAP CARD TO UNFOLD KEYBOARD & START TYPING <<<",
                            fontSize = 10.sp,
                            color = CyberCyan.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Actions Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        viewModel.resetAestheticTest()
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("aesthetic_restart_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESET TEST",
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // RESULTS OVERLAY ticket popup (Monkeytype receipts!)
        AnimatedVisibility(
            visible = isFinished,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("aesthetic_results_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(2.dp, CyberGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SPEED TEST COMPLETE",
                            color = CyberGreen,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "=== LEADERBOARD EXCERPT ===",
                            color = DarkGreyText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large WPM
                        Text(
                            text = "${wpm.toInt()}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "WORDS PER MINUTE",
                            color = DarkGreyText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Detail statistics grid
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${accuracy.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyberYellow, fontFamily = FontFamily.Monospace)
                                Text(text = "ACCURACY", fontSize = 9.sp, color = DarkGreyText, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${selectedDuration}S", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyberCyan, fontFamily = FontFamily.Monospace)
                                Text(text = "DURATION", fontSize = 9.sp, color = DarkGreyText, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${(wpm * accuracy).toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CyberPink, fontFamily = FontFamily.Monospace)
                                Text(text = "SCORE", fontSize = 9.sp, color = DarkGreyText, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Button(
                            onClick = {
                                viewModel.resetAestheticTest()
                                focusRequester.requestFocus()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("results_retry_button")
                        ) {
                            Text(
                                text = "PLAY AGAIN",
                                color = CyberBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                            border = BorderStroke(1.dp, BrightText.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("results_exit_button")
                        ) {
                            Text(
                                text = "EXIT TO DASHBOARD",
                                color = BrightText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
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
fun MetricIndicator(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = DarkGreyText,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
