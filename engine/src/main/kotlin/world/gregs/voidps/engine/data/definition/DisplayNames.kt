package world.gregs.voidps.engine.data.definition

import kotlin.math.pow
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
     * Turns the local part of an email address into a valid display name
     */
    fun sanitise(local: String): String {
        val name = local.replace(disallowed, " ").replace(spaces, " ").trim().take(MAX_LENGTH).trim()
        if (name.isEmpty()) {
            return FALLBACK
        }
        return name.replaceFirstChar { it.uppercaseChar() }
    }

    /**
     * Finds the first name based on [base] which isn't [taken] by appending a number
     */
    fun unique(base: String, taken: (String) -> Boolean): String {
        if (!taken(base)) {
            return base
        }
        for (suffix in 2..MAX_ATTEMPTS) {
            val number = suffix.toString()
            val name = base.take(MAX_LENGTH - number.length).trimEnd() + number
            if (!taken(name)) {
                return name
            }
        }
        while (true) {
            val number = Random.nextInt(100_000, 1_000_000).toString()
            val name = base.take(MAX_LENGTH - number.length).trimEnd() + number
            if (!taken(name)) {
                return name
            }
        }
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
                0 -> stem + digits(random)
                1 -> fit(stem, NOUNS.random(random))
                2 -> fit(ADJECTIVES.random(random), stem)
                else -> fit(ADJECTIVES.random(random), NOUNS.random(random)) + digits(random, max = 2)
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
     * Joins two words, shortening the first so the result fits within [MAX_LENGTH]
     */
    private fun fit(first: String, second: String): String {
        val room = MAX_LENGTH - second.length
        if (room < 2) {
            return second
        }
        return first.take(room) + second
    }

    private val ADJECTIVES = listOf(
        "Swift", "Iron", "Dark", "Bold", "Wild", "Lone", "Grey", "Red", "Sly", "Grim", "Storm", "Frost",
        "Fire", "Shadow", "Silent", "Brave", "Mystic", "Royal", "Elder", "Rogue", "Lucky", "Noble", "Steel", "Jade",
    )
    private val NOUNS = listOf(
        "Wolf", "Knight", "Ranger", "Mage", "Rogue", "Archer", "Druid", "Slayer", "Hunter", "Warden", "Raven", "Fox",
        "Bear", "Hawk", "Drake", "Sage", "Blade", "Rune", "Miner", "Smith", "Fisher", "Wizard", "Paladin", "Scout",
    )

    private const val MAX_ATTEMPTS = 999
    private const val MAX_SUGGESTION_ATTEMPTS = 50
}
