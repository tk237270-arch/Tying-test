package com.example

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Screen navigation enum
enum class ArcadeScreen {
    DASHBOARD,
    AESTHETIC_TEST,
    RACER_GAME,
    LASER_GAME,
    HISTORY
}

// Particle explosion for Space Defense Mode
data class ExplosionParticle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val color: Int, // 0=cyan, 1=pink, 2=yellow
    var alpha: Float = 1.0f,
    val scale: Float = Random.nextFloat() * 8f + 4f
)

// Laser laser-bullet for Space Defense Mode
data class LaserBullet(
    val id: Int,
    var currentX: Float,
    var currentY: Float,
    val targetX: Float,
    val targetY: Float,
    val speed: Float = 0.08f,
    var shouldRemove: Boolean = false
)

// Falling enemy word ship
data class WordShip(
    val id: Int,
    val word: String,
    var typedLetterCount: Int = 0,
    var x: Float, // 0.1 to 0.9 percentage of screen width
    var y: Float, // 0.0 (top) to 1.0 (bottom) percentage of screen height
    val speed: Float
)

class TypeArcadeViewModel(application: Application) : AndroidViewModel(application) {

    private val scoreDao = AppDatabase.getDatabase(application).scoreDao()
    private val repository = ScoreRepository(scoreDao)

    // Screens
    private val _currentScreen = MutableStateFlow(ArcadeScreen.DASHBOARD)
    val currentScreen: StateFlow<ArcadeScreen> = _currentScreen.asStateFlow()

    // High Score lists
    val allHistory: StateFlow<List<ScoreHistory>> = repository.allItemsStateFlow(viewModelScope)

    // Specific mode top scores
    private val _topAestheticScores = MutableStateFlow<List<ScoreHistory>>(emptyList())
    val topAestheticScores: StateFlow<List<ScoreHistory>> = _topAestheticScores.asStateFlow()

    private val _topRacerScores = MutableStateFlow<List<ScoreHistory>>(emptyList())
    val topRacerScores: StateFlow<List<ScoreHistory>> = _topRacerScores.asStateFlow()

    private val _topLaserScores = MutableStateFlow<List<ScoreHistory>>(emptyList())
    val topLaserScores: StateFlow<List<ScoreHistory>> = _topLaserScores.asStateFlow()

    init {
        loadHighScores()
    }

    private fun loadHighScores() {
        viewModelScope.launch {
            repository.getHighScoresForMode("aesthetic").collect { _topAestheticScores.value = it }
        }
        viewModelScope.launch {
            repository.getHighScoresForMode("racer").collect { _topRacerScores.value = it }
        }
        viewModelScope.launch {
            repository.getHighScoresForMode("laser").collect { _topLaserScores.value = it }
        }
    }

    fun navigateTo(screen: ArcadeScreen) {
        _currentScreen.value = screen
        if (screen == ArcadeScreen.DASHBOARD) {
            stopAllGames()
            loadHighScores()
        }
    }

    private fun stopAllGames() {
        stopAestheticTest()
        stopRacerGame()
        stopLaserGame()
    }


    // ==========================================
    // 1. AESTHETIC SPEED TEST GAME ENGINE (Monkeytype Style)
    // ==========================================
    private var aestheticTimerJob: Job? = null

    private val _aestheticTimer = MutableStateFlow(30)
    val aestheticTimer = _aestheticTimer.asStateFlow()

    val wordDurationOptions = listOf(15, 30, 60)
    val textSourceOptions = listOf("classic", "snippets", "quotes")

    private val _selectedDuration = MutableStateFlow(30)
    val selectedDuration = _selectedDuration.asStateFlow()

    private val _selectedSource = MutableStateFlow("classic")
    val selectedSource = _selectedSource.asStateFlow()

    private val _aestheticTargetText = MutableStateFlow("")
    val aestheticTargetText = _aestheticTargetText.asStateFlow()

    private val _aestheticInputText = MutableStateFlow("")
    val aestheticInputText = _aestheticInputText.asStateFlow()

    private val _aestheticWpm = MutableStateFlow(0.0)
    val aestheticWpm = _aestheticWpm.asStateFlow()

    private val _aestheticAccuracy = MutableStateFlow(100.0)
    val aestheticAccuracy = _aestheticAccuracy.asStateFlow()

    private val _aestheticIsActive = MutableStateFlow(false)
    val aestheticIsActive = _aestheticIsActive.asStateFlow()

    private val _aestheticIsFinished = MutableStateFlow(false)
    val aestheticIsFinished = _aestheticIsFinished.asStateFlow()

    private var aestheticTotalKeystrokes = 0
    private var aestheticCorrectKeystrokes = 0
    private var aestheticTimeElapsed = 0

    fun changeAestheticDuration(seconds: Int) {
        if (!_aestheticIsActive.value) {
            _selectedDuration.value = seconds
            _aestheticTimer.value = seconds
        }
    }

    fun changeAestheticSource(source: String) {
        if (!_aestheticIsActive.value) {
            _selectedSource.value = source
            resetAestheticTest()
        }
    }

    fun resetAestheticTest() {
        stopAestheticTest()
        _aestheticInputText.value = ""
        _aestheticWpm.value = 0.0
        _aestheticAccuracy.value = 100.0
        _aestheticIsActive.value = false
        _aestheticIsFinished.value = false
        _aestheticTimer.value = _selectedDuration.value
        aestheticTotalKeystrokes = 0
        aestheticCorrectKeystrokes = 0
        aestheticTimeElapsed = 0

        // Generate target text
        _aestheticTargetText.value = when (_selectedSource.value) {
            "classic" -> TypingContent.generateWords(35)
            "snippets" -> TypingContent.codingSnippets.shuffled().take(4).joinToString(" ")
            "quotes" -> TypingContent.generateQuotes(1)
            else -> TypingContent.generateWords(25)
        }
    }

    fun updateAestheticInput(input: String) {
        if (_aestheticIsFinished.value) return

        val target = _aestheticTargetText.value
        if (input.length > target.length) return

        // Start timer on first keystroke
        if (!_aestheticIsActive.value && input.isNotEmpty()) {
            startAestheticTimer()
        }

        val previousLength = _aestheticInputText.value.length
        _aestheticInputText.value = input

        // Keystroke statistics update
        if (input.length > previousLength) {
            aestheticTotalKeystrokes++
            val charIndex = input.length - 1
            if (input[charIndex] == target[charIndex]) {
                aestheticCorrectKeystrokes++
            }
        }

        calculateAestheticLiveStats()

        // Check if finished because text typed fully
        if (input.length == target.length) {
            finishAestheticTest()
        }
    }

    private fun startAestheticTimer() {
        _aestheticIsActive.value = true
        aestheticTimerJob?.cancel()
        aestheticTimerJob = viewModelScope.launch {
            while (_aestheticTimer.value > 0) {
                delay(1000)
                _aestheticTimer.value--
                aestheticTimeElapsed++
                calculateAestheticLiveStats()
            }
            finishAestheticTest()
        }
    }

    private fun calculateAestheticLiveStats() {
        val secondsElapsed = if (aestheticTimeElapsed > 0) aestheticTimeElapsed else 1
        val minutesElapsed = secondsElapsed / 60.0

        val textTyped = _aestheticInputText.value
        val target = _aestheticTargetText.value

        var correctChars = 0
        for (i in textTyped.indices) {
            if (i < target.length && textTyped[i] == target[i]) {
                correctChars++
            }
        }

        // Standard WPM calculation (correct characters / 5) / minutes
        val wpmVal = (correctChars / 5.0) / minutesElapsed
        _aestheticWpm.value = if (wpmVal.isInfinite() || wpmVal.isNaN()) 0.0 else Math.round(wpmVal * 10.0) / 10.0

        // Accuracy calculation
        _aestheticAccuracy.value = if (aestheticTotalKeystrokes > 0) {
            val acc = (correctChars.toDouble() / aestheticTotalKeystrokes.toDouble()) * 100.0
            Math.round(acc * 10.0) / 10.0
        } else {
            100.0
        }
    }

    private fun finishAestheticTest() {
        aestheticTimerJob?.cancel()
        _aestheticIsActive.value = false
        _aestheticIsFinished.value = true

        val finalWpm = _aestheticWpm.value
        val finalAcc = _aestheticAccuracy.value

        // Save stat to DB
        viewModelScope.launch {
            repository.insertScore(
                ScoreHistory(
                    mode = "aesthetic",
                    wpm = finalWpm,
                    accuracy = finalAcc,
                    timeDurationSeconds = if (aestheticTimeElapsed > 0) aestheticTimeElapsed else 1,
                    scorePoints = (finalWpm * finalAcc).toInt()
                )
            )
            loadHighScores()
        }
    }

    private fun stopAestheticTest() {
        aestheticTimerJob?.cancel()
        _aestheticIsActive.value = false
    }


    // ==========================================
    // 2. COMPETITIVE RACER GAME ENGINE (TypeRacer Style)
    // ==========================================
    private var racerTimerJob: Job? = null

    private val _racerCountdown = MutableStateFlow(-1) // -1 means inactive
    val racerCountdown = _racerCountdown.asStateFlow()

    private val _racerDifficulty = MutableStateFlow("Medium") // "Easy", "Medium", "Hard"
    val racerDifficulty = _racerDifficulty.asStateFlow()

    private val _racerTargetText = MutableStateFlow("")
    val racerTargetText = _racerTargetText.asStateFlow()

    private val _racerInputText = MutableStateFlow("")
    val racerInputText = _racerInputText.asStateFlow()

    private val _playerProgress = MutableStateFlow(0f)
    val playerProgress = _playerProgress.asStateFlow()

    private val _botProgress = MutableStateFlow(0f)
    val botProgress = _botProgress.asStateFlow()

    private val _racerWpm = MutableStateFlow(0.0)
    val racerWpm = _racerWpm.asStateFlow()

    private val _racerAccuracy = MutableStateFlow(100.0)
    val racerAccuracy = _racerAccuracy.asStateFlow()

    private val _racerWinner = MutableStateFlow("") // "player" or "bot" or ""
    val racerWinner = _racerWinner.asStateFlow()

    private val _racerIsPlaying = MutableStateFlow(false)
    val racerIsPlaying = _racerIsPlaying.asStateFlow()

    private val _racerIsFinished = MutableStateFlow(false)
    val racerIsFinished = _racerIsFinished.asStateFlow()

    private var racerStartTimestamp = 0L
    private var racerTotalKeystrokes = 0
    private var racerCorrectKeystrokes = 0

    fun changeRacerDifficulty(difficulty: String) {
        if (!_racerIsPlaying.value && _racerCountdown.value == -1) {
            _racerDifficulty.value = difficulty
        }
    }

    fun startRacerGame() {
        stopRacerGame()
        _racerInputText.value = ""
        _playerProgress.value = 0f
        _botProgress.value = 0f
        _racerWpm.value = 0.0
        _racerAccuracy.value = 100.0
        _racerWinner.value = ""
        _racerIsFinished.value = false
        _racerIsPlaying.value = false
        racerTotalKeystrokes = 0
        racerCorrectKeystrokes = 0

        // Select racer paragraph
        _racerTargetText.value = TypingContent.techQuotes.shuffled().take(2).joinToString(" ")

        // Start pre-race countdown
        _racerCountdown.value = 3
        viewModelScope.launch {
            while (_racerCountdown.value > 0) {
                delay(1000)
                _racerCountdown.value--
            }
            _racerCountdown.value = 0 // GO!
            delay(500)
            _racerCountdown.value = -1 // Hide count
            triggerRaceStart()
        }
    }

    private fun triggerRaceStart() {
        _racerIsPlaying.value = true
        racerStartTimestamp = System.currentTimeMillis()

        // Calibrate bot speed in WPM based on level
        val botWPM = when (_racerDifficulty.value) {
            "Easy" -> Random.nextDouble(25.0, 35.0)
            "Medium" -> Random.nextDouble(55.0, 65.0)
            "Hard" -> Random.nextDouble(80.0, 95.0)
            else -> 50.0
        }

        // Standard character calculation: assuming 5 chars per word
        val totalChars = _racerTargetText.value.length
        val botCharsPerMs = (botWPM * 5.0) / (60.0 * 1000.0)

        // Starting interactive racer game loop
        racerTimerJob?.cancel()
        racerTimerJob = viewModelScope.launch {
            while (_racerIsPlaying.value) {
                delay(50)
                val elapsedMs = System.currentTimeMillis() - racerStartTimestamp

                // Advance Bot
                val expectedBotChars = botCharsPerMs * elapsedMs
                val expectedProgress = (expectedBotChars / totalChars).toFloat().coerceIn(0f, 1f)
                _botProgress.value = expectedProgress

                // Compute player live speed
                calculateRacerLiveStats()

                // Check Bot Victory
                if (_botProgress.value >= 1f && _playerProgress.value < 1f && _racerWinner.value.isEmpty()) {
                    _racerWinner.value = "Turbo Bot"
                }

                // If player is finished, but finished before/after bot
                if (_playerProgress.value >= 1f) {
                    if (_racerWinner.value.isEmpty()) {
                        _racerWinner.value = "Player"
                    }
                    finishRacerGame()
                }
            }
        }
    }

    fun updateRacerInput(input: String) {
        if (!_racerIsPlaying.value || _racerIsFinished.value) return

        val target = _racerTargetText.value
        if (input.length > target.length) return

        // Evaluate typo freeze-rule: you cannot proceed paste any mistake
        // Find if current text has typing error
        var hasTypo = false
        for (i in input.indices) {
            if (i < target.length && input[i] != target[i]) {
                hasTypo = true
                break
            }
        }

        val previousLength = _racerInputText.value.length
        _racerInputText.value = input

        if (input.length > previousLength) {
            racerTotalKeystrokes++
            val lastCharIndex = input.length - 1
            if (!hasTypo && lastCharIndex < target.length && input[lastCharIndex] == target[lastCharIndex]) {
                racerCorrectKeystrokes++
            }
        }

        // Progress corresponds to the number of correct matching letters from start up to typo
        var matchedCorrectLetters = 0
        for (i in input.indices) {
            if (i < target.length && input[i] == target[i]) {
                matchedCorrectLetters = i + 1
            } else {
                break
            }
        }

        _playerProgress.value = (matchedCorrectLetters.toFloat() / target.length.toFloat()).coerceIn(0f, 1f)
        calculateRacerLiveStats()

        if (matchedCorrectLetters == target.length) {
            _playerProgress.value = 1f
            if (_racerWinner.value.isEmpty()) {
                _racerWinner.value = "Player"
            }
            finishRacerGame()
        }
    }

    private fun calculateRacerLiveStats() {
        val totalMsElapsed = System.currentTimeMillis() - racerStartTimestamp
        val secondsElapsed = if (totalMsElapsed > 0) totalMsElapsed / 1000.0 else 1.0
        val minutesElapsed = secondsElapsed / 60.0

        val target = _racerTargetText.value
        val typedInput = _racerInputText.value

        var correctChars = 0
        for (i in typedInput.indices) {
            if (i < target.length && typedInput[i] == target[i]) {
                correctChars++
            } else {
                break
            }
        }

        val wpmVal = (correctChars / 5.0) / minutesElapsed
        _racerWpm.value = if (wpmVal.isInfinite() || wpmVal.isNaN()) 0.0 else Math.round(wpmVal * 10.0) / 10.0

        _racerAccuracy.value = if (racerTotalKeystrokes > 0) {
            val acc = (correctChars.toDouble() / racerTotalKeystrokes.toDouble()) * 100.0
            Math.round(acc * 10.0) / 10.0
        } else {
            100.0
        }
    }

    private fun finishRacerGame() {
        racerTimerJob?.cancel()
        _racerIsPlaying.value = false
        _racerIsFinished.value = true

        val elapsedMs = System.currentTimeMillis() - racerStartTimestamp
        val elapsedSeconds = if (elapsedMs > 0) (elapsedMs / 1000).toInt() else 1

        val finalWpm = _racerWpm.value
        val finalAcc = _racerAccuracy.value
        val rankPoints = if (_racerWinner.value == "Player") 500 else 200

        // Save to DB
        viewModelScope.launch {
            repository.insertScore(
                ScoreHistory(
                    mode = "racer",
                    wpm = finalWpm,
                    accuracy = finalAcc,
                    timeDurationSeconds = elapsedSeconds,
                    scorePoints = (finalWpm * 10).toInt() + rankPoints
                )
            )
            loadHighScores()
        }
    }

    private fun stopRacerGame() {
        racerTimerJob?.cancel()
        _racerCountdown.value = -1
        _racerIsPlaying.value = false
        _racerIsFinished.value = false
    }


    // ==========================================
    // 3. SPACE DEFENSE ARCADE GAME ENGINE (ZType Style)
    // ==========================================
    private var laserGameLoopJob: Job? = null

    private val _laserScore = MutableStateFlow(0)
    val laserScore = _laserScore.asStateFlow()

    private val _laserLives = MutableStateFlow(3)
    val laserLives = _laserLives.asStateFlow()

    private val _laserIsPlaying = MutableStateFlow(false)
    val laserIsPlaying = _laserIsPlaying.asStateFlow()

    private val _laserIsFinished = MutableStateFlow(false)
    val laserIsFinished = _laserIsFinished.asStateFlow()

    // Interactive word lists and active states
    val activeShips = mutableStateListOf<WordShip>()
    val activeLaserBullets = mutableStateListOf<LaserBullet>()
    val activeExplosionParticles = mutableStateListOf<ExplosionParticle>()

    // Typing Target lock
    private val _targetedShipId = MutableStateFlow<Int?>(null)
    val targetedShipId = _targetedShipId.asStateFlow()

    private val _laserTypedSofar = MutableStateFlow("")
    val laserTypedSofar = _laserTypedSofar.asStateFlow()

    private var laserShipIdCounter = 0
    private var laserBulletIdCounter = 0
    private var laserParticleIdCounter = 0

    private var laserLastSpawnTime = 0L
    private var laserSpawnInterval = 4000L // Spawns new word every 4s, reduces as score grows!
    private var laserBaseSpeed = 0.015f    // Speeds up incrementally too!

    private var laserGameTotalKeystrokes = 0
    private var laserGameCorrectKeystrokes = 0
    private var laserGameStartTime = 0L

    fun startLaserGame() {
        stopLaserGame()
        _laserScore.value = 0
        _laserLives.value = 3
        _laserIsPlaying.value = true
        _laserIsFinished.value = false

        _targetedShipId.value = null
        _laserTypedSofar.value = ""

        activeShips.clear()
        activeLaserBullets.clear()
        activeExplosionParticles.clear()

        laserShipIdCounter = 0
        laserBulletIdCounter = 0
        laserParticleIdCounter = 0
        laserSpawnInterval = 4000L
        laserBaseSpeed = 0.015f

        laserGameTotalKeystrokes = 0
        laserGameCorrectKeystrokes = 0
        laserGameStartTime = System.currentTimeMillis()

        // Spawn first two words quickly
        spawnEnemyShip()
        spawnEnemyShip()
        laserLastSpawnTime = System.currentTimeMillis()

        // Physics + Spawning Loop
        laserGameLoopJob?.cancel()
        laserGameLoopJob = viewModelScope.launch {
            while (_laserIsPlaying.value) {
                delay(30) // ~33 FPS fluid physics
                updateLaserPhysics()
            }
        }
    }

    private fun spawnEnemyShip() {
        val word = TypingContent.classicWords.random()
        val randomX = Random.nextFloat() * 0.75f + 0.125f // center screen span
        val speedModifier = Random.nextFloat() * 0.5f + 0.8f // local variance
        val speed = laserBaseSpeed * speedModifier

        activeShips.add(
            WordShip(
                id = ++laserShipIdCounter,
                word = word,
                typedLetterCount = 0,
                x = randomX,
                y = 0.0f,
                speed = speed
            )
        )
    }

    private fun updateLaserPhysics() {
        val now = System.currentTimeMillis()

        // 1. Spawning Logic
        if (now - laserLastSpawnTime > laserSpawnInterval) {
            spawnEnemyShip()
            laserLastSpawnTime = now

            // Adapt difficulty
            val currentScore = _laserScore.value
            laserSpawnInterval = (4000L - (currentScore * 80L)).coerceAtLeast(1400L)
            laserBaseSpeed = (0.015f + (currentScore * 0.0003f)).coerceAtMost(0.045f)
        }

        // 2. Fall Update & Barrier Check
        val shipsIterator = activeShips.iterator()
        while (shipsIterator.hasNext()) {
            val ship = shipsIterator.next()
            ship.y += ship.speed

            // Did it hit the bottom barrier?
            if (ship.y >= 0.9f) {
                _laserLives.value = (_laserLives.value - 1).coerceAtLeast(0)
                // Trigger explosion visual where it crashed
                createExplosion(ship.x, 0.9f, 1) // CyberPink damage splash
                shipsIterator.remove()

                // Reset target if it was the targeted crash
                if (_targetedShipId.value == ship.id) {
                    _targetedShipId.value = null
                    _laserTypedSofar.value = ""
                }

                if (_laserLives.value <= 0) {
                    finishLaserGame()
                    break
                }
            }
        }

        // 3. Move Laser Bullets
        val bulletIterator = activeLaserBullets.iterator()
        while (bulletIterator.hasNext()) {
            val b = bulletIterator.next()
            // interpolate or step closer to target coordinates
            val dx = b.targetX - b.currentX
            val dy = b.targetY - b.currentY
            val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()

            if (distance < b.speed) {
                // Bullet arrived! Create mini visual sparks
                createExplosion(b.targetX, b.targetY, 0) // CyberCyan impact sparks
                bulletIterator.remove()
            } else {
                b.currentX += (dx / distance) * b.speed
                b.currentY += (dy / distance) * b.speed
            }
        }

        // 4. Explosion Particle Simulation
        val particleIterator = activeExplosionParticles.iterator()
        while (particleIterator.hasNext()) {
            val p = particleIterator.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= 0.04f // fade out in ~25 frames
            if (p.alpha <= 0) {
                particleIterator.remove()
            }
        }
    }

    fun handleLaserInputChar(char: Char) {
        if (!_laserIsPlaying.value || _laserIsFinished.value) return

        laserGameTotalKeystrokes++

        val targetId = _targetedShipId.value
        if (targetId == null) {
            // Find any ship whose first letter matches this key character
            val matchingShip = activeShips.firstOrNull { it.word.firstOrNull()?.equals(char, true) == true }
            if (matchingShip != null) {
                laserGameCorrectKeystrokes++
                _targetedShipId.value = matchingShip.id
                _laserTypedSofar.value = char.toString()
                matchingShip.typedLetterCount = 1

                // Fire laser bullet!
                fireLaserBullet(startX = 0.5f, startY = 1.0f, targetShip = matchingShip)

                // Check instant completion (1-letter word)
                if (matchingShip.typedLetterCount == matchingShip.word.length) {
                    destroyShip(matchingShip)
                }
            }
        } else {
            // Evaluated locked-in targeted word ship
            val ship = activeShips.firstOrNull { it.id == targetId }
            if (ship != null) {
                val nextCharRequired = ship.word.getOrNull(ship.typedLetterCount)
                if (nextCharRequired != null && nextCharRequired.equals(char, true)) {
                    laserGameCorrectKeystrokes++
                    ship.typedLetterCount++
                    _laserTypedSofar.value += char

                    // Fire laser bullet from cannon (0.5, 0.95) to current character coordinate
                    fireLaserBullet(startX = 0.5f, startY = 0.95f, targetShip = ship)

                    if (ship.typedLetterCount == ship.word.length) {
                        destroyShip(ship)
                    }
                }
            } else {
                // Target disappeared somehow (e.g. crashed), clean up lock
                _targetedShipId.value = null
                _laserTypedSofar.value = ""
            }
        }
    }

    private fun fireLaserBullet(startX: Float, startY: Float, targetShip: WordShip) {
        // Approximate visual coordinates based on ship's animation variables
        activeLaserBullets.add(
            LaserBullet(
                id = ++laserBulletIdCounter,
                currentX = startX,
                currentY = startY,
                targetX = targetShip.x,
                targetY = targetShip.y
            )
        )
    }

    private fun destroyShip(ship: WordShip) {
        // Points awarded scale with length of word
        _laserScore.value += ship.word.length * 10
        createExplosion(ship.x, ship.y, 2) // Festive Golden/Yellow core explosion!

        activeShips.remove(ship)
        _targetedShipId.value = null
        _laserTypedSofar.value = ""
    }

    private fun createExplosion(x: Float, y: Float, colorType: Int) {
        // Add 16 particles exploding outwards
        val particleCount = 15
        for (i in 0 until particleCount) {
            val angle = (2 * Math.PI * i / particleCount) + (Random.nextFloat() * 0.4)
            val velocityScalar = Random.nextFloat() * 0.015f + 0.005f
            val vx = (cos(angle) * velocityScalar).toFloat()
            val vy = (sin(angle) * velocityScalar).toFloat()

            activeExplosionParticles.add(
                ExplosionParticle(
                    id = ++laserParticleIdCounter,
                    startX = x,
                    startY = y,
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = colorType
                )
            )
        }
    }

    private fun finishLaserGame() {
        _laserIsPlaying.value = false
        _laserIsFinished.value = true
        laserGameLoopJob?.cancel()

        // Calculate typing stats
        val elapsedMs = System.currentTimeMillis() - laserGameStartTime
        val elapsedMinutes = (elapsedMs / 1000.0) / 60.0

        val wpmSpeed = (laserGameCorrectKeystrokes / 5.0) / (if (elapsedMinutes > 0) elapsedMinutes else 0.1)
        val finalWpm = if (wpmSpeed.isInfinite() || wpmSpeed.isNaN()) 0.0 else Math.round(wpmSpeed * 10.0) / 10.0
        val finalAcc = if (laserGameTotalKeystrokes > 0) {
            val acc = (laserGameCorrectKeystrokes.toDouble() / laserGameTotalKeystrokes.toDouble()) * 100.0
            Math.round(acc * 10.0) / 10.0
        } else {
            100.0
        }

        val finalScore = _laserScore.value

        // Insert into database
        viewModelScope.launch {
            repository.insertScore(
                ScoreHistory(
                    mode = "laser",
                    wpm = finalWpm,
                    accuracy = finalAcc,
                    timeDurationSeconds = (elapsedMs / 1000).toInt(),
                    scorePoints = finalScore
                )
            )
            loadHighScores()
        }
    }

    private fun stopLaserGame() {
        _laserIsPlaying.value = false
        _laserIsFinished.value = false
        _targetedShipId.value = null
        laserGameLoopJob?.cancel()
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            loadHighScores()
        }
    }

    override fun onCleared() {
        stopAllGames()
        super.onCleared()
    }
}

// Extension to safely load flow from DAO in repository without requiring direct hilt injection
private fun ScoreRepository.allItemsStateFlow(scope: kotlinx.coroutines.CoroutineScope): StateFlow<List<ScoreHistory>> {
    return this.allScores.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
