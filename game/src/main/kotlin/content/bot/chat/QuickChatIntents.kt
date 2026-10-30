package content.bot.chat

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions

/**
 * Quick chat phrases have a fixed meaning so they map straight to an intent instead of being guessed by the model.
 * Patterns are the phrase text with typed placeholders, and `*` wildcards e.g. "What is your level in *?"
 */
class QuickChatIntents(definitions: QuickChatPhraseDefinitions, patterns: Map<String, List<String>>) {
    private val logger = InlineLogger("BotChat")
    private val intents = HashMap<Int, String>()

    /**
     * Patterns which don't match any phrase
     */
    val unmatched: List<String>

    init {
        val regexes = patterns.flatMap { (intent, list) -> list.map { Triple(it, regex(it), intent) } }
        val used = HashSet<String>()
        for (definition in definitions.definitions) {
            val text = QuickChatPhrases.text(definition) ?: continue
            val (pattern, _, intent) = regexes.firstOrNull { it.second.matches(text) } ?: continue
            intents.putIfAbsent(definition.id, intent)
            used.add(pattern)
        }
        unmatched = regexes.map { it.first }.filter { it !in used }
        for (pattern in unmatched) {
            logger.warn { "Quick chat pattern '$pattern' doesn't match any phrase." }
        }
    }

    val size: Int
        get() = intents.size

    fun intent(phrase: Int): String? = intents[phrase]

    companion object {
        fun regex(pattern: String) = Regex(pattern.split('*').joinToString(".*") { Regex.escape(it) })
    }
}
