package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
import kotlin.random.Random

@Composable
fun LaserGameScreen(
    viewModel: TypeArcadeViewModel,
    modifier: Modifier = Modifier
) {
    val score by viewModel.laserScore.collectAsState()
    val lives by viewModel.laserLives.collectAsState()
    val isPlaying by viewModel.laserIsPlaying.collectAsState()
    val isFinished by viewModel.laserIsFinished.collectAsState()

    val ships = viewModel.activeShips
    val bullets = viewModel.activeLaserBullets
    val particles = viewModel.activeExplosionParticles

    val targetedId by viewModel.targetedShipId.collectAsState()
    val typedSofar by viewModel.laserTypedSofar.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Keyboard capture text buffer
    var keystrokeText by remember { mutableStateOf("") }

    // Spawn stars for background
    val bgStars = remember {
        List(25) {
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startLaserGame()
        focusRequester.requestFocus()
    }

    // Auto-focus keyboard on state modifications
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .clickable { focusRequester.requestFocus() },
        contentAlignment = Alignment.TopCenter
    ) {
        // 1. ARCADE BACKGROUND CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Starry sky rendering
            bgStars.forEach { star ->
                val px = star.x * width
                // slowly drift stars dynamically
                val py = ((star.y + (System.currentTimeMillis() % 100000 / 100000f)) % 1.0f) * height
                drawCircle(
                    color = BrightText.copy(alpha = 0.35f),
                    radius = 2f,
                    center = Offset(px, py)
                )
            }

            // Draw player defence base turrent at bottom (0.5 coord, 0.95 coord)
            val baseCentX = width / 2f
            val baseCentY = height * 0.95f
            val turretRadius = 32f

            // Base bracket
            drawCircle(
                color = CyberCyan.copy(alpha = 0.25f),
                radius = turretRadius,
                center = Offset(baseCentX, baseCentY)
            )

            // Turret barrel outline aimed at target or straight up
            val targetShip = ships.firstOrNull { it.id == targetedId }
            val aimX: Float
            val aimY: Float
            if (targetShip != null) {
                aimX = targetShip.x * width
                aimY = targetShip.y * height
            } else {
                aimX = baseCentX
                aimY = baseCentY - 150f
            }

            val dx = aimX - baseCentX
            val dy = aimY - baseCentY
            val dist = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

            if (dist > 0) {
                val barrelLength = 48f
                val endX = baseCentX + (dx / dist) * barrelLength
                val endY = baseCentY + (dy / dist) * barrelLength

                drawLine(
                    color = CyberCyan,
                    start = Offset(baseCentX, baseCentY),
                    end = Offset(endX, endY),
                    strokeWidth = 10f
                )
            }

            // Draw active moving laser bullets
            bullets.forEach { bullet ->
                val bx = bullet.currentX * width
                val by = bullet.currentY * height
                drawCircle(
                    color = CyberCyan,
                    radius = 5f,
                    center = Offset(bx, by)
                )
            }

            // Draw explosion particles
            particles.forEach { p ->
                val px = p.x * width
                val py = p.y * height
                val col = when (p.color) {
                    0 -> CyberCyan
                    1 -> CyberPink
                    else -> CyberYellow
                }
                drawCircle(
                    color = col.copy(alpha = p.alpha),
                    radius = p.scale,
                    center = Offset(px, py)
                )
            }
        }

        // Invisible Keyboard Listener Textfield
        BasicTextField(
            value = keystrokeText,
            onValueChange = {
                if (it.isNotEmpty()) {
                    val lastChar = it.last()
                    viewModel.handleLaserInputChar(lastChar)
                    keystrokeText = "" // always reset buffer immediately
                }
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .testTag("laser_text_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.None,
                autoCorrectEnabled = false
            ),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent)
        )

        // 2. LAYERED CONTEXT GAMEPLAY PANEL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Stats bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(CyberCard.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ArcadeScreen.DASHBOARD) },
                    modifier = Modifier.size(32.dp).testTag("laser_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberYellow)
                }

                // Score Display
                Text(
                    text = "SCORE: $score",
                    color = CyberYellow,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )

                // Lives Hearts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        val active = index < lives
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Life",
                            tint = if (active) CyberPink else DarkGreyText.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Word Asteroids Box layer (Fills remaining center part)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                val boxWidth = maxWidth
                val boxHeight = maxHeight

                // Render falling items as cards
                ships.forEach { ship ->
                    val isTargetedShip = ship.id == targetedId

                    val annoWord = buildAnnotatedString {
                        for (i in ship.word.indices) {
                            val c = ship.word[i]
                            if (isTargetedShip && i < ship.typedLetterCount) {
                                // highlight matching typed letters
                                withStyle(SpanStyle(color = CyberCyan, fontWeight = FontWeight.Bold)) {
                                    append(c)
                                }
                            } else {
                                withStyle(SpanStyle(color = if (isTargetedShip) CyberPink else BrightText)) {
                                    append(c)
                                }
                            }
                        }
                    }

                    // Dynamically position coordinates inside bounds
                    val posX = ship.x * boxWidth.value
                    val posY = ship.y * boxHeight.value

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (posX - 40f).coerceIn(4f, boxWidth.value - 90f).dp,
                                y = posY.coerceIn(0f, boxHeight.value - 40f).dp
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isTargetedShip) CyberPink.copy(alpha = 0.15f) else CyberCard.copy(
                                    alpha = 0.85f
                                )
                            )
                            .border(
                                BorderStroke(
                                    if (isTargetedShip) 1.5.dp else 1.dp,
                                    if (isTargetedShip) CyberPink else DarkGreyText.copy(alpha = 0.5f)
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = annoWord,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If typing focus is lost or starts, show a small hint
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(CyberCard.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                                .border(BorderStroke(0.5.dp, CyberCyan.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                            Text(
                                text = "TAP TO BRING KEYBOARD & TYPE DEFENSE KEYS",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Bottom cannon backing padding
            Spacer(modifier = Modifier.height(44.dp))
        }

        // 3. GAME OVER OVERLAY SCREEN
        AnimatedVisibility(
            visible = isFinished,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("laser_gameover_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(2.dp, CyberYellow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🔴 SYSTEM DESTROYED 🔴",
                            color = CyberPink,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Your shields crashed! Retro Space Defended scores have been indexed on the leaderboard.",
                            color = BrightText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBackground, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score PTS",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberYellow,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "FINAL HIGH SCORE",
                                    fontSize = 10.sp,
                                    color = DarkGreyText,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.startLaserGame()
                                focusRequester.requestFocus()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("laser_game_retry_button")
                        ) {
                            Text(
                                text = "START DEFENSE RETRY",
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
                                .testTag("laser_game_exit_button")
                        ) {
                            Text(
                                text = "RETURN TO COCKPIT",
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
