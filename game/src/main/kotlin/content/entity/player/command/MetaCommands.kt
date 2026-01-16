package content.entity.player.command

import content.quest.questJournal
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.playerCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.rights
import kotlin.math.max

class MetaCommands(
    val fontDefinitions: FontDefinitions,
    val itemDefinitions: ItemDefinitions,
    val objectDefinitions: ObjectDefinitions,
    val npcDefinitions: NPCDefinitions,
    val accountDefinitions: AccountDefinitions,
    val grandExchange: GrandExchange,
    val variableDefinitions: VariableDefinitions,
) : Script {

    init {
        playerCommand(
            "commands",
            stringArg("filter", optional = true, desc = "Term to search command list with"),
            desc = "Display a list of available commands",
            handler = ::listCommands,
        )

        playerCommand(
            "help",
            stringArg("command-name", desc = "Command name to lookup"),
            desc = "Find more information about a specific command",
            handler = ::help,
        )

        modCommand(
            "find",
            stringArg("content-name", desc = "The term to search content for", autofill = { itemDefinitions.ids.keys + objectDefinitions.ids.keys + npcDefinitions.ids.keys + accountDefinitions.displayNames.keys + accountDefinitions.clans.keys }),
            desc = "Search all content",
            handler = ::find,
        )
        commandAlias("find", "search")

        modCommand("items", stringArg("name", desc = "Item name or id to search for", autofill = itemDefinitions.ids.keys), desc = "Search all items") { args ->
            if (hasClock("search_delay")) {
                return@modCommand
            }
            start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            message("===== Items =====", ChatType.Console)
            val found = search(this, itemDefinitions, search) { it.name }
            message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("objects", stringArg("name", desc = "Object name or id to search for", autofill = objectDefinitions.ids.keys), desc = "Search all game objects") { args ->
            if (hasClock("search_delay")) {
                return@modCommand
            }
            start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            message("===== Objects =====", ChatType.Console)
            val found = search(this, objectDefinitions, search) { it.name }
            message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("npcs", stringArg("name", desc = "Npc name or id to search for", autofill = npcDefinitions.ids.keys), desc = "Search all npcs") { args ->
            if (hasClock("search_delay")) {
                return@modCommand
            }
            start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            message("===== NPCs =====", ChatType.Console)
            val found = search(this, npcDefinitions, search) { it.name }
            message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("players", stringArg("name", desc = "Player name or id to search for", autofill = accountDefinitions.displayNames.keys), desc = "Search all players") { args ->
            if (hasClock("search_delay")) {
                return@modCommand
            }
            start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            message("===== Players =====", ChatType.Console)
            val found = searchPlayers(this, search)
            message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("clans", stringArg("name", desc = "Clan name or id to search for", autofill = accountDefinitions.clans.keys), desc = "Search all clans") { args ->
            if (hasClock("search_delay")) {
                return@modCommand
            }
            start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            message("===== Clans =====", ChatType.Console)
            val found = searchClans(this, search)
            message("$found results found for '$search'", ChatType.Console)
        }
    }

    fun find(player: Player, args: List<String>) {
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        val search = args.joinToString(" ").lowercase()
        var found = 0
        player.message("===== Items =====", ChatType.Console)
        found += search(player, itemDefinitions, search) { it.name }
        player.message("===== Objects =====", ChatType.Console)
        found += search(player, objectDefinitions, search) { it.name }
        player.message("===== NPCs =====", ChatType.Console)
        found += search(player, npcDefinitions, search) { it.name }
        player.message("===== Commands =====", ChatType.Console)
        found += searchCommands(player, search)
        player.message("===== Players =====", ChatType.Console)
        found += searchPlayers(player, search)
        player.message("===== Clans =====", ChatType.Console)
        found += searchClans(player, search)
        player.message("===== Variables =====", ChatType.Console)
        found += searchVariables(player, search)
        player.message("$found results found for '$search'", ChatType.Console)
    }

    fun help(player: Player, args: List<String>) {
        if (player.hasClock("help_delay")) {
            return
        }
        player.start("help_delay", 1)
        val command = args[0]
        val metadata = Commands.find(player, command) ?: return
        fun appendLine(text: String) {
            player.message(text, ChatType.Console)
        }

        appendLine("===== ${metadata.name} =====")
        appendLine("Rights: ${metadata.rights.name}")
        val usages = mutableListOf<Pair<String, String>>()
        val args = mutableListOf<Triple<String, String, String>>()
        val font = fontDefinitions.get("p12_full")
        for (signature in metadata.signatures) {
            val usage = "${metadata.name} ${signature.args.joinToString(" ") { if (it.optional) "[${it.key}]" else "(${it.key})" }}"
            usages.add(usage to signature.description)
            for (arg in signature.args) {
                args.add(Triple(arg.key, arg.type.name, arg.description))
                if (arg.autofill != null) {
                    val values = arg.autofill!!.invoke()
                    if (values.isNotEmpty()) {
                        args.add(Triple("      e.g. ${values.take(5).joinToString(", ")}", "", ""))
                    }
                }
            }
        }

        var longestArg = if (args.isEmpty()) 0 else args.maxOf { if (it.second.isBlank()) 0 else font.width(it.first) }
        longestArg = longestArg - longestArg.rem(3) + 6
        val longestUsage = usages.maxOf { font.width(it.first) }
        val longestKey = if (args.isEmpty()) 0 else args.maxOf { if (it.second.isBlank()) 0 else font.width("${it.first}${" ".repeat((longestArg - font.width(it.first)) / 3)}${it.second}") }
        var longest = max(longestUsage, longestKey)
        longest = longest - longest.rem(3) + 15
        appendLine("Usage:")
        for ((usage, desc) in usages) {
            appendLine("  ${usage}${" ".repeat((longest - font.width(usage)) / 3)}$desc")
        }
        if (args.isNotEmpty()) {
            appendLine(" ")
            appendLine("Arguments:")
            for ((arg, type, desc) in args) {
                if (type.isBlank()) {
                    appendLine(arg)
                    continue
                }
                val key = "${arg}${" ".repeat((longestArg - font.width(arg)).coerceAtLeast(0) / 3)}$type"
                appendLine("  $key${" ".repeat((longest - font.width(key)) / 3)}$desc")
            }
        }
        appendLine("=============")
    }

    fun listCommands(player: Player, args: List<String>) {
        if (player.hasClock("commands_delay")) {
            return
        }
        player.start("commands_delay", 1)
        val filter = args.getOrNull(0)
        val list = mutableListOf(
            "${if (filter == null) "Complete" else "Filtered"} list with arguments and descriptions in the format:",
            "${Colours.BLUE.toTag()}command_name (required-variable) [optional-variable]</col>",
            "command description",
            "",
        )
        val commands = Commands.commands.values
            .filter {
                player.rights.ordinal >= it.rights.ordinal &&
                    (
                        filter == null ||
                            it.name.contains(filter, ignoreCase = true) ||
                            it.signatures.any { sig -> sig.description.contains(filter, ignoreCase = true) } ||
                            it.signatures.any { sig -> sig.args.any { arg -> arg.key.contains(filter, ignoreCase = true) } }
                        )
            }
            .sortedByDescending { it.name }
        for (command in commands) {
            for (signature in command.signatures) {
                if (filter != null && !command.name.contains(filter, ignoreCase = true) && !signature.description.contains(filter, ignoreCase = true) && signature.args.none { it.key.contains(filter, ignoreCase = true) }) {
                    continue
                }
                list.add("${Colours.BLUE.toTag()}${command.name} ${signature.usage()}</col>")
                if (signature.description.isNotBlank()) {
                    list.add(signature.description)
                }
                if (filter != null) {
                    list.add("")
                }
            }
        }
        for (line in list) {
            println(line)
        }
        player.questJournal("Commands List", list)
    }

    private val utf8Regex = "[^\\x20-\\x7e]".toRegex()

    private fun <T> search(player: Player, definitions: DefinitionsDecoder<T>, search: String, getName: (T) -> String): Int where T : Definition, T : Extra {
        var found = 0
        for (id in definitions.definitions.indices) {
            val def = definitions.getOrNull(id) ?: continue
            val name = getName(def)
            if (name.lowercase().contains(search) || def.stringId.lowercase().contains(search)) {
                player.message("[${name.lowercase().replace(utf8Regex, "")}] - id: $id${if (def.stringId.isNotBlank()) " (${def.stringId})" else ""}", ChatType.Console)
                found++
            }
        }
        return found
    }

    private fun searchCommands(player: Player, content: String): Int {
        var found = 0
        for ((command, meta) in Commands.commands) {
            if (command.contains(content, ignoreCase = true)) {
                for (signature in meta.signatures) {
                    player.message("[$command] - usage: ${signature.usage()}", ChatType.Console)
                    found++
                }
            }
        }
        return found
    }

    private fun searchPlayers(player: Player, content: String): Int {
        var found = 0
        for (name in accountDefinitions.displayNames.keys) {
            if (name.contains(content, ignoreCase = true)) {
                player.message("[$name]", ChatType.Console)
                found++
            }
        }
        return found
    }

    private fun searchClans(player: Player, content: String): Int {
        var found = 0
        for ((name, clan) in accountDefinitions.clans) {
            if (name.contains(content, ignoreCase = true)) {
                player.message("[$name] - members: ${clan.members.size}", ChatType.Console)
                found++
            }
        }
        return found
    }

    private fun searchVariables(player: Player, content: String): Int {
        var found = 0
        for ((name, definition) in variableDefinitions.definitions) {
            if (name.contains(content, ignoreCase = true)) {
                player.message("[$name] - id: ${definition.id}, default: ${definition.defaultValue}, persist: ${definition.persist}, values: ${definition.values}", ChatType.Console)
                found++
            }
        }
        return found
    }
}
