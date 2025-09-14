package world.gregs.voidps.engine.client.ui.event

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.splitSafe
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.hasRights
import world.gregs.voidps.type.Distance

open class Commands {
    private val aliases: MutableMap<String, String> = mutableMapOf()
    private val suggestions: MutableMap<String, String> = mutableMapOf()
    val commands: MutableMap<String, CommandMetadata> = mutableMapOf()
    private val logger = InlineLogger("Commands")


    /**
     * Register a command
     */
    fun register(name: String, signatures: List<CommandSignature>, rights: PlayerRights = PlayerRights.None) {
        commands[name] = CommandMetadata(name = name, rights = rights, signatures = signatures)
    }

    /**
     * Call a command
     */
    suspend fun call(player: Player, command: String) {
        val parts = command.splitSafe(' ')
        val prefix = parts.getOrNull(0)?.lowercase() ?: return
        val metadata = find(player, prefix) ?: return
        val arguments = parts.drop(1)
        val signature = metadata.find(arguments)
        if (signature == null) {
            player.message("Unknown arguments for command '${metadata.name}'.", ChatType.Console)
            if (metadata.signatures.size == 1) {
                player.message("Valid arguments: ${metadata.signatures.first().usage()}", ChatType.Console)
                return
            }
            player.message("Usages:", ChatType.Console)
            for (sig in metadata.signatures) {
                player.message("  ${metadata.name} ${sig.usage()}", ChatType.Console)
            }
            return
        }
        try {
            signature.handler.invoke(player, arguments)
        } catch (e: Exception) {
            logger.error(e) { "Error in command '${metadata.name}'" }
        }
    }

    /**
     * Find a command given [commandName], [PlayerRights], an exact name match or [alias].
     * If no command found return null but give [suggestions] or nearest match.
     */
    fun find(player: Player, commandName: String): CommandMetadata? {
        val metadata = commands[commandName]
        if (metadata == null) {
            val alias = aliases[commandName]
            if (alias != null) {
                return commands[alias]
            }
            val suggestion = suggestions[commandName]
            if (suggestion != null) {
                player.message("Unknown command: $commandName. Did you mean '$suggestion'?", ChatType.Console)
                return null
            }
            val closest = commands.keys.minByOrNull { Distance.levenshtein(it, commandName) }
            if (closest != null && Distance.levenshtein(closest, commandName) <= 2) {
                player.message("Unknown command: $commandName. Did you mean '$closest'?", ChatType.Console)
                return null
            }
            player.message("Unknown command: $commandName.", ChatType.Console)
            return null
        }
        if (!player.hasRights(metadata.rights)) {
            player.message("Unauthorized command: ${metadata.name}; ${metadata.rights.name} rights required.", ChatType.Console)
            return null
        }
        return metadata
    }

    /**
     * Autofill partially complete console [command] with [commands] or [CommandArgument.autofill] values
     */
    fun autofill(player: Player, command: String) {
        val parts = command.splitSafe(' ')
        if (parts.isEmpty()) {
            return
        }

        // Match commands
        if (parts.size == 1) {
            val filtered = commands.filter { (key, cmd) -> player.hasRights(cmd.rights) && key.startsWith(parts[0], ignoreCase = true) }
            printMatches(filtered.keys, player)
            val match = (if (filtered.size == 1) filtered.keys.firstOrNull() else longestCommonPrefix(filtered.keys)) ?: return
            player.message(match, ChatType.ConsoleSet)
            return
        }

        // Match arguments
        val metadata = find(player, parts[0]) ?: return
        val arguments = parts.drop(1)
        val signatures = metadata.find(arguments) ?: return
        val arg = signatures.args.getOrNull(arguments.lastIndex) ?: return
        val last = parts.last()
        val filtered = arg.autofill?.invoke()?.filter { it.startsWith(last, ignoreCase = true) } ?: return
        printMatches(filtered, player)
        val match = (if (filtered.size == 1) filtered.firstOrNull() else longestCommonPrefix(filtered)) ?: return
        val complete = if (match.contains(' ')) "\"$match\"" else match
        player.message("${parts.dropLast(1).joinToString(" ")} $complete", ChatType.ConsoleSet)
    }

    private fun printMatches(matches: Collection<String>, player: Player) {
        if (matches.size <= 1) {
            return
        }
        val take = player["auto_complete_match", 5]
        player.message("Multiple matches${if (matches.size < 6) "" else " (showing $take of ${matches.size})"}:", ChatType.Console)
        for (match in matches.take(take)) {
            player.message("  $match", ChatType.Console)
        }
    }

    /**
     * Find the longest string which all [strings] start with
     */
    private fun longestCommonPrefix(strings: Collection<String>): String? {
        if (strings.isEmpty()) {
            return null
        }
        val shortest = strings.minByOrNull { it.length } ?: return null
        for (i in shortest.indices) {
            val c = shortest[i]
            if (strings.any { it[i] != c }) {
                return shortest.substring(0, i)
            }
        }
        return shortest
    }

    fun clear() {
        aliases.clear()
        suggestions.clear()
        commands.clear()
    }

    /**
     * Alternative spellings of a command [name]
     */
    fun alias(name: String, vararg alternatives: String) {
        for (alternative in alternatives) {
            aliases[alternative] = name
        }
    }

    /**
     * Common incorrect [name] which should be corrected
     * For replacements @see [alias]
     */
    fun suggest(name: String, vararg alternatives: String) {
        for (alternative in alternatives) {
            suggestions[alternative] = name
        }
    }

    companion object : Commands()
}

fun playerCommand(name: String, vararg arguments: CommandArgument, desc: String = "", handler: suspend (Player, List<String>) -> Unit) {
    Commands.register(name, listOf(CommandSignature(arguments.toList(), desc, handler)))
}

fun modCommand(name: String, vararg arguments: CommandArgument, desc: String = "", handler: suspend (Player, List<String>) -> Unit) {
    Commands.register(name, listOf(CommandSignature(arguments.toList(), desc, handler)), PlayerRights.Mod)
}

fun adminCommand(name: String, vararg arguments: CommandArgument, desc: String = "", handler: suspend (Player, List<String>) -> Unit) {
    Commands.register(name, listOf(CommandSignature(arguments.toList(), desc, handler)), PlayerRights.Admin)
}

fun commandAlias(name: String, vararg alternatives: String) {
    Commands.alias(name, *alternatives)
}

fun command(vararg args: CommandArgument, desc: String = "", handler: suspend (Player, List<String>) -> Unit) = CommandSignature(args.toList(), desc, handler)


fun playerCommands(name: String, vararg signatures: CommandSignature) {
    Commands.register(name, signatures.toList())
}

fun modCommands(name: String, vararg signatures: CommandSignature) {
    Commands.register(name, signatures.toList(), PlayerRights.Mod)
}

fun adminCommands(name: String, vararg signatures: CommandSignature) {
    Commands.register(name, signatures.toList(), PlayerRights.Admin)
}