package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.splitSafe
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.hasRights
import world.gregs.voidps.type.Distance

object Commands {
    private val aliases: MutableMap<String, String> = mutableMapOf()
    private val suggestions: MutableMap<String, String> = mutableMapOf()
    val commands: MutableMap<String, CommandMetadata> = mutableMapOf()

    private fun register(metadata: CommandMetadata) {
        commands[metadata.name] = metadata
    }

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
            for (sig in metadata.signatures) {
                player.message("  ${sig.usage()}", ChatType.Console)
            }
            return
        }
        val content = command.removePrefix(parts[0]).trim()
        signature.handler.invoke(CommandContext(player, arguments, content))
    }

    fun find(player: Player, prefix: String): CommandMetadata? {
        val metadata = commands[prefix]
        if (metadata == null) {
            val alias = aliases[prefix]
            if (alias != null) {
                return commands[alias]
            }
            val suggestion = suggestions[prefix]
            if (suggestion != null) {
                player.message("Unknown command: $prefix. Did you mean '$suggestion'?", ChatType.Console)
                return null
            }
            val closest = commands.keys.minByOrNull { Distance.levenshtein(it, prefix) }
            if (closest != null && Distance.levenshtein(closest, prefix) <= 2) {
                player.message("Unknown command: $prefix. Did you mean '$closest'?", ChatType.Console)
                return null
            }
            player.message("Unknown command: $prefix.", ChatType.Console)
            return null
        }
        if (!player.hasRights(metadata.rights)) {
            player.message("Unauthorized command: ${metadata.name}. ${metadata.rights.name} rights required", ChatType.Console)
            return null
        }
        return metadata
    }

    fun autoComplete(player: Player, content: String) {
        val parts = content.splitSafe(' ')

        if (parts.isEmpty()) {
            return
        }

        // Match commands
        if (parts.size == 1) {
            val keys = commands.keys.filter { it.startsWith(parts[0], ignoreCase = true) }
            val match = (if (keys.size == 1) keys.firstOrNull() else longestCommonPrefix(keys)) ?: return
            player.message(match, ChatType.ConsoleSet)
            return
        }

        // Match arguments
        val command = find(player, parts[0]) ?: return
        val arguments = parts.drop(1)
        val signatures = command.find(arguments) ?: return
        val arg = signatures.args.getOrNull(arguments.lastIndex) ?: return
        val last = parts.last()
        val filtered = arg.autoComplete?.invoke()?.filter { it.startsWith(last, ignoreCase = true) } ?: return
        if (filtered.size > 1) {
            val take = player["auto_complete_match", 5]
            player.message("Multiple matches${if (filtered.size < 6) "" else " (showing $take of ${filtered.size})"}:", ChatType.Console)
            for (match in filtered.take(take)) {
                player.message("  $match", ChatType.Console)
            }
        }
        val match = (if (filtered.size == 1) filtered.firstOrNull() else longestCommonPrefix(filtered)) ?: return
        val complete = if (match.contains(' ')) "\"$match\"" else match
        player.message("${parts.dropLast(1).joinToString(" ")} $complete", ChatType.ConsoleSet)
    }

    private fun longestCommonPrefix(strings: List<String>): String? {
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
     */
    fun suggest(name: String, vararg alternatives: String) {
        for (alternative in alternatives) {
            suggestions[alternative] = name
        }
    }

    fun player(name: String, vararg arguments: Arg, description: String = "", handler: suspend CommandContext.() -> Unit) {
        player(name, CommandSignature(arguments.toList(), description, handler))
    }

    fun mod(name: String, vararg arguments: Arg, description: String = "", handler: suspend CommandContext.() -> Unit) {
        mod(name, CommandSignature(arguments.toList(), description, handler))
    }

    fun admin(name: String, vararg arguments: Arg, description: String = "", handler: suspend CommandContext.() -> Unit) {
        admin(name, CommandSignature(arguments.toList(), description, handler))
    }

    fun player(name: String, vararg signatures: CommandSignature) {
        register(CommandMetadata(name = name, signatures = signatures.toList()))
    }

    fun mod(name: String, vararg signatures: CommandSignature) {
        register(
            CommandMetadata(
                name = name,
                rights = PlayerRights.Mod,
                signatures = signatures.toList(),
            )
        )
    }

    fun admin(name: String, vararg signatures: CommandSignature) {
        register(
            CommandMetadata(
                name = name,
                rights = PlayerRights.Admin,
                signatures = signatures.toList(),
            )
        )
    }

    fun signature(vararg args: Arg, description: String = "", handler: suspend CommandContext.() -> Unit) = CommandSignature(args.toList(), description, handler)

}