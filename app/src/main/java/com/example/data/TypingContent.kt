package com.example.data

object TypingContent {
    // Monkeytype classic 100 random words
    val classicWords = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
    )

    // Code snippets / syntax words to satisfy nerdy typing practice
    val codingSnippets = listOf(
        "val count = list.filter { it.isActive }.size",
        "fun execute(param: String): Deferred<Result>",
        "Modifier.padding(16.dp).fillMaxWidth()",
        "import kotlinx.coroutines.flow.StateFlow",
        "val database = Room.databaseBuilder(context)",
        "Scaffold(contentWindowInsets = WindowInsets)",
        "items.forEach { item -> Log.d(\"TAG\", item) }",
        "val deferred = async(Dispatchers.Default)",
        "override fun onCreate(savedInstanceState: Bundle?)",
        "val state by viewModel.uiState.collectAs()"
    )

    // Fun facts & retro arcade sentences
    val techQuotes = listOf(
        "In nineteen seventy-two, Atari released Pong, widely considered the first blockbuster video game in history.",
        "Space Invaders was so popular in Japan that it caused a temporary shortage of the hundred-yen coin.",
        "Pac-Man was designed to appeal to everyone, featuring colorful ghosts named Blinky, Pinky, Inky, and Clyde.",
        "The Konami Code is up, up, down, down, left, right, left, right, B, A.",
        "A fast typist can achieve over one hundred words per minute, using tactile feedback from mechanical switches.",
        "Jetpack Compose relies on standard Kotlin compiler plugins to optimize recomposition nodes dynamically.",
        "Vector graphics in early games like Asteroids drew crisp vector lines directly onto cathode ray tubes.",
        "Tetris was designed by Alexey Pajitnov in nineteen eighty-four while working at the Academy of Sciences.",
        "Super Mario Bros revolutionized side-scrolling platformers with its secret warp zones and smooth physics.",
        "The first computer mouse was invented by Douglas Engelbart and was carved out of first-class mahogany wood."
    )

    fun generateWords(count: Int): String {
        return (1..count).map { classicWords.random() }.joinToString(" ")
    }

    fun generateQuotes(count: Int): String {
        return (1..count).map { techQuotes.random() }.joinToString(" ")
    }
}
