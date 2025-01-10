package world.gregs.voidps.world.command.debug

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.sendInventoryItems
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.*

val definitions: InterfaceDefinitions by inject()

adminCommand("inter (interface-id)", "open an interface with int or string id") {
    val id = content.toIntOrNull()
    if (id == null) {
        val name = content
        player.interfaces.open(name)
    } else if (id != -1 || !closeInterface(player)) {
        val inter = definitions.get(content)
        var parent = if (player.interfaces.resizable) 746 else 548
        var index = if (player.interfaces.resizable) 12 else 8
        val p = inter["parent_${if (player.interfaces.resizable) "resize" else "fixed"}", ""]
        if (p.isNotBlank()) {
            parent = definitions.get(p).id
            index = inter["index_${if (player.interfaces.resizable) "resize" else "fixed"}", -1]
        }
        if (id == -1) {
            player.client?.closeInterface(parent, index)
        } else {
            println("Open $parent $index $id")
            player.client?.openInterface(false, parent, index, id)
        }
    }
}

fun closeInterface(player: Player): Boolean {
    val id = player.interfaces.get("main_screen") ?: player.interfaces.get("wide_screen") ?: player.interfaces.get("underlay") ?: return false
    return player.interfaces.close(id)
}

adminCommand("show (interface-id) (interface-component-id) (visibility)", "set the visibility of an interface component") {
    val parts = content.split(" ")
    player.client?.interfaceVisibility(parts[0].toInt(), parts[1].toInt(), !parts[2].toBoolean())
}

adminCommand("colour (interface-id) (interface-component-id) (red) (green) (blue)", "set colour of interface component") {
    val parts = content.split(" ")
    player.client?.colourInterface(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), parts[4].toInt())
}

adminCommand("sendItem (interface) (interface-component) (item-id) [item-amount]", "send an item to an interface component") {
    val parts = content.split(" ")
    player.interfaces.sendItem(parts[0], parts[1], parts[2].toInt(), parts.getOrNull(3)?.toInt() ?: 1)
}

adminCommand("sendText (interface) (interface-component) (text...)", "send any text to an interface component") {
    val parts = content.split(" ")
    player.interfaces.sendText(parts[0], parts[1], content.removePrefix("${parts[0]} ${parts[1]} "))
}

adminCommand("setting (interface) (component-id) (from-slot) (to-slot) (settings...)", "send settings to an interface component") {
    val parts = content.split(" ")
    val remainder = parts.subList(4, parts.size).map { it.toIntOrNull() }.requireNoNulls().toIntArray()
    player.message("Settings sent ${remainder.toList()}", ChatType.Console)
    player.sendInterfaceSettings(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), getHash(*remainder))
}

adminCommand("script (script-id) [params...]", "run a client script with any number of parameters") {
    val parts = content.split(" ")
    val remainder = parts.subList(1, parts.size).map { if (it == "true") 1 else if (it == "false") 0 else it.toIntOrNull() ?: it }
    val id = parts[0].toIntOrNull()
    if (id == null) {
        player.sendScript(id = parts[0], *remainder.toTypedArray())
    } else {
        player.sendScript(id, remainder)
    }
}

adminCommand("sendItems") {
    repeat(1200) {
        player.sendInventoryItems(it, 0, intArrayOf(), false)
    }
    for (inventory in 0 until 1200) {
        player.sendInventoryItems(inventory, 1, intArrayOf(995, 100), false)
    }
}

adminCommand("var (variable-name) (variable-value)", "set a variable") {
    val parts = content.split(" ")
    player[parts.first()] = parts.last().toBooleanStrictOrNull() ?: parts.last().toIntOrNull() ?: parts.last()
}

adminCommand("varp (varp-id) (int-value)", "send a player-variable value to the client by string or int id") {
    val parts = content.split(" ")
    val intId = parts.first().toIntOrNull()
    if (intId == null) {
        player.variables.set(parts.first(), parts.last().toInt())
    } else {
        val def: VariableDefinitions = get()
        val name = def.getVarp(intId)
        if (name == null) {
            player.client?.sendVarp(intId, parts.last().toInt())
        } else {
            player.variables.set(name, parts.last().toInt())
        }
    }
}

adminCommand("varbit (varbit-id) (int-value)", "send a variable-bit value to the client by string or int id") {
    val parts = content.split(" ")
    val intId = parts.first().toIntOrNull()
    if (intId == null) {
        player.variables.set(parts.first(), parts.last().toInt())
    } else {
        val def: VariableDefinitions = get()
        val name = def.getVarbit(intId)
        if (name == null) {
            player.client?.sendVarbit(intId, parts.last().toInt())
        } else {
            player.variables.set(name, parts.last().toInt())
        }
    }
}

adminCommand("varc (varc-id) (int-value)", "send a client-variable value to the client by string or int id") {
    val parts = content.split(" ")
    val intId = parts.first().toIntOrNull()
    if (intId == null) {
        player.variables.set(parts.first(), parts.last().toInt())
    } else {
        player.client?.sendVarc(intId, parts.last().toInt())
    }
}

adminCommand("varcstr (varcstr-id) (string-value)", "send a variable-client-string value to the client") {
    val parts = content.split(" ")
    val intId = parts.first().toIntOrNull()
    val string = content.removePrefix("${parts.first()} ")
    if (intId == null) {
        player.variables.set(parts.first(), string)
    } else {
        player.client?.sendVarcStr(intId, string)
    }
}