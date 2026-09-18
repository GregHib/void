package world.gregs.voidps.engine.client.command

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.splitSafe
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.type.Distance
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Commands typed into the terminal which started the server.
 *
 * Player commands take a [world.gregs.voidps.engine.entity.character.player.Player] to reply to and
 * to check rights against, the console has neither. Rather than fake a player, the dispatch table is
 * kept separate and its handlers delegate to the same domain functions the player commands call.
 * Handlers return their output as lines which are written to [output], gutter marked so a command's
 * reply is distinguishable from the server's own log lines.
 *
 * Lines are read on [ConsoleReader]'s thread but executed by [run] as a game loop stage, so handlers
 * always touch game state from the game thread.
 */
object ConsoleCommands : Runnable {

    private val logger = InlineLogger("Console")
    private const val SUGGESTION_DISTANCE = 2
    private const val GUTTER = "\u001b[36m\u2502\u001b[0m "
    val commands: MutableMap<String, ConsoleCommand> = LinkedHashMap()
    val aliases: MutableMap<String, String> = LinkedHashMap()
    private val queue = ConcurrentLinkedQueue<String>()

    /**
     * Clearing the screen draws to the terminal rather than touching game state, so [ConsoleReader]
     * runs it instead of the game thread.
     */
    const val CLEAR = "clear"

    /**
     * Where a command's reply is written, one line at a time.
     */
    var output: (String) -> Unit = { line -> println("$GUTTER$line") }

    fun register(name: String, vararg args: CommandArgument, desc: String = "", rest: Boolean = false, handler: (List<String>) -> List<String>) {
        commands[name] = ConsoleCommand(name, args.toList(), desc, rest, handler)
    }

    /**
     * Alternative spellings of a command [name], as [Commands.alias] does for player commands.
     */
    fun alias(name: String, vararg alternatives: String) {
        for (alternative in alternatives) {
            aliases[alternative] = name
        }
    }

    /**
     * Find a command by an exact name or an [alias].
     */
    fun find(name: String): ConsoleCommand? = commands[name] ?: commands[aliases[name]]

    /**
     * A single command's usage and what each of its arguments is for.
     */
    fun help(name: String): List<String> {
        val command = find(name.lowercase()) ?: return unknown(name)
        val list = mutableListOf(command.usage())
        if (command.description.isNotBlank()) {
            list.add("  ${command.description}")
        }
        for (argument in command.args) {
            if (argument.description.isNotBlank()) {
                list.add("  $argument ${argument.description}")
            }
        }
        return list
    }

    /**
     * Queue a [line] read on the console thread for execution on the game thread.
     */
    fun submit(line: String) {
        queue.add(line)
    }

    /**
     * Run everything queued since the last tick.
     */
    override fun run() {
        while (true) {
            val line = queue.poll() ?: return
            AuditLog.info("console_command \"$line\"")
            for (reply in execute(line)) {
                output.invoke(reply)
            }
        }
    }

    /**
     * Parse and run a single [line], returning the output to display.
     */
    fun execute(line: String): List<String> {
        val parts = parse(line)
        val name = parts.firstOrNull()?.lowercase() ?: return emptyList()
        val command = find(name) ?: return unknown(name)
        val args = arguments(command, parts.drop(1))
        val required = command.args.count { !it.optional }
        if (args.size < required || args.size > command.args.size) {
            return listOf("Usage: ${command.usage()}")
        }
        for ((index, arg) in args.withIndex()) {
            val expected = command.args[index]
            if (!expected.canParse(arg)) {
                return listOf("Invalid value '$arg' for argument ${expected}.", "Usage: ${command.usage()}")
            }
        }
        try {
            return command.handler.invoke(args)
        } catch (e: Exception) {
            logger.error(e) { "Error in console command '$name'" }
            return listOf("Error in command '$name': ${e.message ?: e::class.simpleName}")
        }
    }

    /**
     * Join everything past the last argument into it, so a command taking a name or a message can be
     * typed without quoting it.
     */
    private fun arguments(command: ConsoleCommand, args: List<String>): List<String> {
        if (!command.rest || command.args.isEmpty() || args.size <= command.args.size) {
            return args
        }
        val last = command.args.size - 1
        return args.take(last) + args.drop(last).joinToString(" ")
    }

    /**
     * Nearest match if there is an obvious one, the whole list if there isn't.
     */
    private fun unknown(name: String): List<String> {
        val closest = commands.keys.minByOrNull { Distance.levenshtein(it, name) }
        if (closest != null && Distance.levenshtein(closest, name) <= SUGGESTION_DISTANCE) {
            return listOf("Unknown command '$name'. Did you mean '$closest'?")
        }
        return listOf("Unknown command '$name'.") + usages()
    }

    /**
     * Split [line] into a command name and arguments, keeping double quoted sections together.
     */
    fun parse(line: String): List<String> = line.trim().splitSafe(' ').filter { it.isNotBlank() }

    /**
     * Every registered command with its arguments and description.
     */
    fun usages(): List<String> {
        val list = mutableListOf("Available commands:")
        for (command in commands.values) {
            val description = if (command.description.isBlank()) "" else " - ${command.description}"
            list.add("  ${command.usage()}$description")
        }
        return list
    }

    fun clear() {
        commands.clear()
        aliases.clear()
        queue.clear()
        output = { line -> println("$GUTTER$line") }
    }

}

fun consoleCommand(name: String, vararg args: CommandArgument, desc: String = "", rest: Boolean = false, handler: (List<String>) -> List<String>) {
    ConsoleCommands.register(name, *args, desc = desc, rest = rest, handler = handler)
}

fun consoleAlias(name: String, vararg alternatives: String) {
    ConsoleCommands.alias(name, *alternatives)
}
