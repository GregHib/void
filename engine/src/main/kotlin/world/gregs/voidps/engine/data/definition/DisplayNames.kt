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
     * Suggests [count] valid names similar to [base] which aren't [taken]
     */
    fun suggestions(base: String, count: Int, taken: (String) -> Boolean, random: Random = Random.Default): List<String> {
        val cleaned = sanitise(base).replace(" ", "")
        val stem = if (cleaned == FALLBACK) FALLBACK else cleaned
        val names = LinkedHashSet<String>()
        var attempts = 0
        while (names.size < count && attempts++ < count * MAX_SUGGESTION_ATTEMPTS) {
            val digits = random.nextInt(2, 5)
            val number = random.nextInt(0, 10.0.pow(digits).toInt()).toString().padStart(digits, '0')
            val name = stem.take(MAX_LENGTH - digits) + number
            if (valid(name) && !taken(name) && names.add(name)) {
                continue
            }
        }
        return names.toList()
    }

    private const val MAX_ATTEMPTS = 999
    private const val MAX_SUGGESTION_ATTEMPTS = 50
}
