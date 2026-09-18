package world.gregs.voidps.engine.client.command

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.splitSafe

/**
 * Tab completion for the server console, using the same command names and [CommandArgument.autofill]
 * values the in-game command autofill uses.
 */
object ConsoleCompleter {

    private val logger = InlineLogger("Console")

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
            return Completion(start, ConsoleCommands.commands.keys.filter { it.startsWith(word, ignoreCase = true) })
        }
        val command = ConsoleCommands.commands[parts.first().lowercase()] ?: return Completion(start, emptyList())
        val argument = command.args.getOrNull(parts.size - 2) ?: return Completion(start, emptyList())
        val autofill = autofill(argument) ?: return Completion(start, emptyList())
        return Completion(start, autofill.filter { it.startsWith(word, ignoreCase = true) })
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
