package content.entity.player.command

import content.quest.questJournal
import content.social.trade.exchange.GrandExchange
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
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
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import kotlin.math.max

@Script
class MetaCommands {

    val fontDefinitions: FontDefinitions by inject()
    val itemDefinitions: ItemDefinitions by inject()
    val objectDefinitions: ObjectDefinitions by inject()
    val npcDefinitions: NPCDefinitions by inject()
    val accountDefinitions: AccountDefinitions by inject()
    val grandExchange: GrandExchange by inject()
    val variableDefinitions: VariableDefinitions by inject()

    init {
        playerCommand(
            "commands",
            stringArg("filter", optional = true, desc = "term to search command list with"),
            desc = "display a list of available commands",
            handler = ::listCommands,
        )

        playerCommand(
            "help",
            stringArg("command-name", desc = "command name to lookup"),
            desc = "find more information about a specific command",
            handler = ::help,
        )

        modCommand(
            "find",
            stringArg("content-name", desc = "the term to search content for", autofill = { itemDefinitions.ids.keys + objectDefinitions.ids.keys + npcDefinitions.ids.keys + accountDefinitions.displayNames.keys + accountDefinitions.clans.keys }),
            desc = "search all content",
            handler = ::find,
        )
        commandAlias("find", "search")

        modCommand("items", stringArg("name", desc = "the item name or id to search for", autofill = itemDefinitions.ids.keys), desc = "search all items") { player, args ->
            if (player.hasClock("search_delay")) {
                return@modCommand
            }
            player.start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            player.message("===== Items =====", ChatType.Console)
            val found = search(player, itemDefinitions, search) { it.name }
            player.message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("objects", stringArg("name", desc = "the object name or id to search for", autofill = objectDefinitions.ids.keys), desc = "search all game objects") { player, args ->
            if (player.hasClock("search_delay")) {
                return@modCommand
            }
            player.start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            player.message("===== Objects =====", ChatType.Console)
            val found = search(player, objectDefinitions, search) { it.name }
            player.message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("npcs", stringArg("name", desc = "the npc name or id to search for", autofill = npcDefinitions.ids.keys), desc = "search all npcs") { player, args ->
            if (player.hasClock("search_delay")) {
                return@modCommand
            }
            player.start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            player.message("===== NPCs =====", ChatType.Console)
            val found = search(player, npcDefinitions, search) { it.name }
            player.message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("players", stringArg("name", desc = "the player name or id to search for", autofill = accountDefinitions.displayNames.keys), desc = "search all players") { player, args ->
            if (player.hasClock("search_delay")) {
                return@modCommand
            }
            player.start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            player.message("===== Players =====", ChatType.Console)
            val found = searchPlayers(player, search)
            player.message("$found results found for '$search'", ChatType.Console)
        }

        modCommand("clans", stringArg("name", desc = "the clan name or id to search for", autofill = accountDefinitions.clans.keys), desc = "search all clans") { player, args ->
            if (player.hasClock("search_delay")) {
                return@modCommand
            }
            player.start("search_delay", 1)
            val search = args.joinToString(" ").lowercase()
            player.message("===== Clans =====", ChatType.Console)
            val found = searchClans(player, search)
            player.message("$found results found for '$search'", ChatType.Console)
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
        var longestArg = args.maxOf { if (it.second.isBlank()) 0 else font.width(it.first) }
        longestArg = longestArg - longestArg.rem(3) + 6
        val longestUsage = usages.maxOf { font.width(it.first) }
        val longestKey = args.maxOf { if (it.second.isBlank()) 0 else font.width("${it.first}${" ".repeat((longestArg - font.width(it.first)) / 3)}${it.second}") }
        var longest = max(longestUsage, longestKey)
        longest = longest - longest.rem(3) + 15
        appendLine("Usage:")
        for ((usage, desc) in usages) {
            appendLine("  ${usage}${" ".repeat((longest - font.width(usage)) / 3)}$desc")
        }
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
                if (filter != null && !signature.description.contains(filter, ignoreCase = true) && signature.args.none { it.key.contains(filter, ignoreCase = true) }) {
                    continue
                }
                list.add("${Colours.BLUE.toTag()}${command.name} ${signature.usage()}</col>")
                list.add(signature.description)
                list.add("")
            }
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
