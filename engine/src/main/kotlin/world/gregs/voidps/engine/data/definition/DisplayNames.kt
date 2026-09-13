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

    private const val MAX_ATTEMPTS = 999
}
