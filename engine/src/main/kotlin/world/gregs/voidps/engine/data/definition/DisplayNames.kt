package world.gregs.voidps.engine.data.definition

import kotlin.random.Random

/**
 * Rules for player display names
 */
object DisplayNames {
    const val MAX_LENGTH = 12
    private const val FALLBACK = "Player"
    private val allowed = Regex("^[A-Za-z0-9 ]+$")
    private val disallowed = Regex("[^A-Za-z0-9 ]")
    private val spaces = Regex(" +")

    fun valid(name: String): Boolean {
        if (name.length !in 1..MAX_LENGTH) {
            return false
        }
        if (!allowed.matches(name)) {
            return false
        }
        if (name.startsWith(' ') || name.endsWith(' ') || name.contains("  ")) {
            return false
        }
        return true
    }

    /**
     * Turns free text into a valid display name
     */
    fun sanitise(local: String): String {
        val name = local.replace(disallowed, " ").replace(spaces, " ").trim().take(MAX_LENGTH).trim()
        if (name.isEmpty()) {
            return FALLBACK
        }
        return name.replaceFirstChar { it.uppercaseChar() }
    }

    /**
     * Suggests [count] valid, memorable names based on [base] which aren't [taken].
     * Mixes the base with numbers and words so players get a choice of styles.
     */
    fun suggestions(base: String, count: Int, taken: (String) -> Boolean, random: Random = Random.Default): List<String> {
        val stem = stem(base)
        val names = LinkedHashSet<String>()
        var attempts = 0
        var style = 0
        while (names.size < count && attempts++ < count * MAX_SUGGESTION_ATTEMPTS) {
            val name = when (style++ % 4) {
                0 -> stem.take(MAX_LENGTH - 3) + digits(random)
                1 -> join(stem, NOUNS, random)
                2 -> join(stem, ADJECTIVES, random, prefix = true)
                else -> join(ADJECTIVES.random(random), NOUNS, random) + digits(random, max = 2)
            }
            if (name.length <= MAX_LENGTH && valid(name) && !taken(name)) {
                names.add(name)
            }
        }
        return names.toList()
    }

    /**
     * The player's name without spaces or trailing numbers, e.g. "Seth2" -> "Seth"
     */
    private fun stem(base: String): String {
        val letters = sanitise(base).replace(" ", "").trimEnd { it.isDigit() }
        if (letters.isEmpty() || letters == FALLBACK) {
            return FALLBACK
        }
        return letters.replaceFirstChar { it.uppercaseChar() }
    }

    private fun digits(random: Random, max: Int = 3): String {
        val length = random.nextInt(1, max + 1)
        return (1..length).joinToString("") { random.nextInt(10).toString() }
    }

    /**
     * Joins [word] with a random entry of [words] which fits whole within [MAX_LENGTH], or numbers when none fit
     */
    private fun join(word: String, words: List<String>, random: Random, prefix: Boolean = false): String {
        val fitting = words.filter { it.length + word.length <= MAX_LENGTH }
        if (fitting.isEmpty()) {
            return word.take(MAX_LENGTH - 3) + digits(random)
        }
        val other = fitting.random(random)
        return if (prefix) other + word else word + other
    }

    private val ADJECTIVES = listOf(
        "Swift", "Iron", "Dark", "Bold", "Wild", "Lone", "Grey", "Red", "Sly", "Grim", "Storm", "Frost",
        "Fire", "Shadow", "Silent", "Brave", "Mystic", "Royal", "Elder", "Rogue", "Lucky", "Noble", "Steel", "Jade",
    )
    private val NOUNS = listOf(
        "Wolf", "Knight", "Ranger", "Mage", "Rogue", "Archer", "Druid", "Slayer", "Hunter", "Warden", "Raven", "Fox",
        "Bear", "Hawk", "Drake", "Sage", "Blade", "Rune", "Miner", "Smith", "Fisher", "Wizard", "Paladin", "Scout",
    )

    private const val MAX_SUGGESTION_ATTEMPTS = 50
}
