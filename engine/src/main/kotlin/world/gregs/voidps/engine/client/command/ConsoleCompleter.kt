package world.gregs.voidps.engine.client.command

import world.gregs.voidps.engine.client.ui.chat.splitSafe

/**
 * Tab completion for the server console, using the same command names and [CommandArgument.autofill]
 * values the in-game command autofill uses.
 */
object ConsoleCompleter {

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
        val autofill = argument.autofill?.invoke() ?: return Completion(start, emptyList())
        return Completion(start, autofill.filter { it.startsWith(word, ignoreCase = true) })
    }
}
