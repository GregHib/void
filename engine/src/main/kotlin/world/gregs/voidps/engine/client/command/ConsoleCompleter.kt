package world.gregs.voidps.engine.client.command

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.splitSafe

/**
 * Tab completion for the server console, using the same command names and [CommandArgument.autofill]
 * values the in-game command autofill uses.
 */
object ConsoleCompleter {

    private val logger = InlineLogger("Console")
    private const val MAX_MATCHES = 10

    /**
     * Values the word being typed could be, and where that word starts.
     */
    data class Completion(val start: Int, val candidates: List<String>)

    fun complete(line: String, cursor: Int): Completion {
        val typed = line.substring(0, cursor)
        val parts = typed.splitSafe(' ')
        val word = parts.last()
        val start = cursor - word.length
        if (parts.size == 1) {
            val names = ConsoleCommands.commands.keys + ConsoleCommands.aliases.keys
            return Completion(start, names.filter { it.startsWith(word, ignoreCase = true) })
        }
        val command = ConsoleCommands.find(parts.first().lowercase()) ?: return Completion(start, emptyList())
        val argument = command.args.getOrNull(parts.size - 2) ?: return Completion(start, emptyList())
        val autofill = autofill(argument) ?: return Completion(start, emptyList())
        return Completion(start, autofill.filter { it.startsWith(word, ignoreCase = true) })
    }

    /**
     * What the word being typed could be, as lines to show the operator.
     */
    fun matches(candidates: List<String>): List<String> {
        val shown = candidates.take(MAX_MATCHES)
        val header = if (candidates.size > shown.size) "Matches (showing ${shown.size} of ${candidates.size}):" else "Matches:"
        return listOf(header) + shown.map { candidate -> "  $candidate" }
    }

    /**
     * Autofill is provided by content and read from the console thread, so a value which can't be
     * collected costs the completion rather than the key press.
     */
    private fun autofill(argument: CommandArgument): Set<String>? {
        try {
            return argument.autofill?.invoke()
        } catch (e: Exception) {
            logger.debug(e) { "Unable to autofill '${argument.key}'." }
            return null
        }
    }
}
