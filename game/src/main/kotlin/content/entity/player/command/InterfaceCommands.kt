package content.entity.player.command

import content.entity.npc.shop.OpenShop
import content.entity.player.dialogue.sendLines
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.adminCommands
import world.gregs.voidps.engine.client.command.boolArg
import world.gregs.voidps.engine.client.command.command
import world.gregs.voidps.engine.client.command.commandAlias
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.sendInventoryItems
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.ClientScriptDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.network.login.protocol.encode.*
import kotlin.collections.iterator

@Script
class InterfaceCommands {

    val definitions: InterfaceDefinitions by inject()
    val animationDefinitions: AnimationDefinitions by inject()
    val inventoryDefinitions: InventoryDefinitions by inject()
    val scriptDefinitions: ClientScriptDefinitions by inject()

    init {
        adminCommand("inter", stringArg("interface-id", autofill = definitions.ids.keys), desc = "Open an interface with int or string id", handler = ::open)
        commandAlias("inter", "iface")
        adminCommand("show", stringArg("interface-id", autofill = definitions.ids.keys), stringArg("component-id", autofill = definitions.componentIds.keys), boolArg("visible"), desc = "Toggle visibility of an interface component") { player, args ->
            player.client?.interfaceVisibility(InterfaceDefinition.pack(args[0].toInt(), args[1].toInt()), !args[2].toBoolean())
        }

        adminCommand("colour", stringArg("iface-id", autofill = definitions.ids.keys), stringArg("comp-id", autofill = definitions.componentIds.keys), intArg("red"), intArg("green"), intArg("blue"), desc = "Set colour of an interface component") { player, args ->
            player.client?.colourInterface(InterfaceDefinition.pack(args[0].toInt(), args[1].toInt()), args[2].toInt(), args[3].toInt(), args[4].toInt())
        }

        adminCommand("send_text", stringArg("interface-id", autofill = definitions.ids.keys), stringArg("component-id", autofill = definitions.componentIds.keys), stringArg("text", "text to send (use quotes for spaces)"), desc = "Set text of an interface component") { player, args ->
            player.interfaces.sendText(args[0], args[1], args[2])
        }

        adminCommand("setting", stringArg("id", autofill = definitions.ids.keys), stringArg("comp", autofill = definitions.componentIds.keys), intArg("from"), intArg("to"), intArg("setting", optional = true), intArg("s2", optional = true), intArg("s3", optional = true), desc = "Send settings to an interface component") { player, args ->
            val remainder = args.subList(4, args.size).map { it.toIntOrNull() }.requireNoNulls().toIntArray()
            player.message("Settings sent ${remainder.toList()}", ChatType.Console)
            player.sendInterfaceSettings(InterfaceDefinition.pack(args[0].toInt(), args[1].toInt()), args[2].toInt(), args[3].toInt(), getHash(*remainder))
        }

        adminCommand(
            "script",
            stringArg("id", autofill = scriptDefinitions.ids.keys),
            stringArg("p1", desc = "First parameter", optional = true),
            stringArg("p2", optional = true),
            stringArg("p3", optional = true),
            stringArg("p4", optional = true),
            stringArg("p5", optional = true),
            desc = "Run a client script with any number of parameters",
            handler = ::sendScript,
        )

        val component = command(stringArg("iface-id", autofill = definitions.ids.keys), stringArg("comp-id", autofill = { definitions.componentIds.keys.map { it.substringAfterLast(":") }.toSet() }), intArg("item"), intArg("amount", optional = true), desc = "Send an item to an interface component") { player, args ->
            player.interfaces.sendItem(args[0], args[1], args[2].toInt(), args.getOrNull(3)?.toInt() ?: 1)
        }
        val inventory = command(stringArg("interface-id", autofill = definitions.ids.keys), desc = "Send an item to an interface component", handler = ::sendInventory)

        adminCommands("send_items", component, inventory)

        adminCommand(
            "expr",
            stringArg("expression-id", autofill = { animationDefinitions.definitions.filter { it.stringId.startsWith("expression_") }.map { it.stringId.removePrefix("expression_") }.toSet() }),
            desc = "Display dialogue head with an animation expression",
            handler = ::expression,
        )

        adminCommand("shop", stringArg("shop-id", autofill = { inventoryDefinitions.definitions.filter { it["shop", false] }.map { it.stringId }.toSet() }), desc = "Open a shop by id") { player, args ->
            player.emit(OpenShop(args[0]))
        }
    }

    fun open(player: Player, args: List<String>) {
        val id = args[0].toIntOrNull()
        if (id == null) {
            player.interfaces.open(args[0])
            return
        }
        if (id == -1 && closeInterface(player)) {
            return
        }
        val inter = definitions.get(args[0])
        var parent = if (player.interfaces.resizable) 746 else 548
        var index = if (player.interfaces.resizable) 5 else 8
        val p = inter["parent_${if (player.interfaces.resizable) "resize" else "fixed"}", ""]
        if (p.isNotBlank()) {
            parent = definitions.get(p).id
            index = inter["index_${if (player.interfaces.resizable) "resize" else "fixed"}", -1]
        }
        if (id == -1) {
            player.client?.closeInterface(InterfaceDefinition.pack(parent, index))
        } else {
            println("Open $parent $index $id")
            player.client?.openInterface(false, InterfaceDefinition.pack(parent, index), id)
        }
    }

    fun sendScript(player: Player, args: List<String>) {
        val remainder: List<Any> = args.subList(1, args.size).map {
            when (it) {
                "true" -> 1
                "false" -> 0
                else -> it.toIntOrNull() ?: it
            }
        }
        val id = args[0].toIntOrNull()
        if (id == null) {
            player.sendScript(id = args[0], *remainder.toTypedArray())
        } else {
            player.sendScript(id, remainder)
        }
    }

    fun expression(player: Player, args: List<String>) {
        val id = args[0].toIntOrNull()
        val content = args.joinToString(" ")
        if (id != null) {
            val npc = id < 1000
            if (player.open("dialogue_${if (npc) "npc_" else ""}chat1")) {
                if (npc) {
                    player.client?.npcDialogueHead(15794178, 2176)
                } else {
                    player.client?.playerDialogueHead(4194306)
                }
                player.interfaces.sendAnimation("dialogue_${if (npc) "npc_" else ""}chat1", "head", id)
                player.interfaces.sendText("dialogue_${if (npc) "npc_" else ""}chat1", "title", "title")
                player.interfaces.sendLines("dialogue_${if (npc) "npc_" else ""}chat1", listOf(content))
            }
        } else {
            player.queue("dialogue_command") {
                npc("1902", content, content)
            }
        }
    }
    fun sendInventory(player: Player, args: List<String>) {
//        val array = IntArray(28 * 2)
//        array[0] = 995
//        array[28] = 1
//        player.sendInventoryItems(90, 28, array, false)
//        val ags = IntArray(28 * 2)
//        ags[0] = 11694
//        ags[28] = 1
//        player.sendInventoryItems(90, 28, ags, true)
        repeat(1200) {
            player.sendInventoryItems(it, 0, intArrayOf(), false)
        }
        for (inventory in 0 until 1200) {
            player.sendInventoryItems(inventory, 1, intArrayOf(995, 100), false)
        }
        var setting = 0
        for (i in 0 until 10) {
            setting += (2 shl i)
        }
        val options = Array(9) { "Option $it" }
        val definition = definitions.get(args[0])
        for ((id, component) in definition.components ?: return) {
            if (InterfaceDefinition.componentId(id) == 16) {
                player.sendScript("primary_options", component.id, 0, 1, 1, 0, -1, *options)
                player.sendScript("secondary_options", component.id, 0, 1, 1, 0, -1, *options)
                player.sendInterfaceSettings(id, 0, 100, setting)
            }
        }
    }

    fun closeInterface(player: Player): Boolean {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("wide_screen") ?: player.interfaces.get("underlay") ?: return false
        return player.interfaces.close(id)
    }
}
