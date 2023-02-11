package world.gregs.voidps.world.command.debug

import world.gregs.voidps.engine.client.*
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.client.ui.sendItem
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.closeInterface
import world.gregs.voidps.network.encode.interfaceVisibility
import world.gregs.voidps.network.encode.openInterface

val definitions: InterfaceDefinitions by inject()

on<Command>({ prefix == "inter" }) { player: Player ->
    val id = content.toIntOrNull()
    if (id == null) {
        val name = content
        player.interfaces.open(name)
    } else if (id != -1 || !closeInterface(player)) {
        val inter = definitions.get(content)
        var parent = if (player.gameFrame.resizable) 746 else 548
        var index = if (player.gameFrame.resizable) 12 else 8
        val p = inter["parent_${if (player.gameFrame.resizable) "resize" else "fixed"}", ""]
        if (p.isNotBlank()) {
            parent = definitions.get(p).id
            index = inter["index_${if (player.gameFrame.resizable) "resize" else "fixed"}", -1]
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
    val id = player.interfaces.get("main_screen") ?: player.interfaces.get("underlay") ?: return false
    return player.interfaces.close(id)
}

on<Command>({ prefix == "show" }) { player: Player ->
    val parts = content.split(" ")
    player.client?.interfaceVisibility(parts[0].toInt(), parts[1].toInt(), !parts[2].toBoolean())
}

on<Command>({ prefix == "sendItem" }) { player: Player ->
    val parts = content.split(" ")
    player.interfaces.sendItem(parts[0], parts[1], parts[2].toInt(), parts.getOrNull(3)?.toInt() ?: 1)
}

on<Command>({ prefix == "sendText" }) { player: Player ->
    val parts = content.split(" ")
    player.interfaces.sendText(parts[0], parts[1], content.removePrefix("${parts[0]} ${parts[1]} "))
}

on<Command>({ prefix == "setting" }) { player: Player ->
    val parts = content.split(" ")
    val remainder = parts.subList(4, parts.size).map { it.toIntOrNull() }.requireNoNulls().toIntArray()
    player.message("Settings sent ${remainder.toList()}", ChatType.Console)
    player.sendInterfaceSettings(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), getHash(*remainder))
}

on<Command>({ prefix == "script" }) { player: Player ->
    val parts = content.split(" ")
    val remainder = parts.subList(1, parts.size).map { it.toIntOrNull() ?: it }.toTypedArray()
    player.sendScript(parts[0].toInt(), *remainder)
}

on<Command>({ prefix == "sendItems" }) { player: Player ->
    repeat(1200) {
        player.sendContainerItems(it, 0, intArrayOf(), false)
    }
    for (container in 0 until 1200) {
        player.sendContainerItems(container, 1, intArrayOf(995, 100), false)
    }
}

on<Command>({ prefix == "var" }) { player: Player ->
    val parts = content.split(" ")
    player.setVar(parts.first(), parts.last().toIntOrNull() ?: parts.last())
}

on<Command>({ prefix == "varp" }) { player: Player ->
    val parts = content.split(" ")
    player.sendVarp(parts.first().toInt(), parts.last().toInt())
}

on<Command>({ prefix == "varbit" }) { player: Player ->
    val parts = content.split(" ")
    player.sendVarbit(parts.first().toInt(), parts.last().toInt())
}

on<Command>({ prefix == "varc" }) { player: Player ->
    val parts = content.split(" ")
    player.sendVarc(parts.first().toInt(), parts.last().toInt())
}

on<Command>({ prefix == "varcstr" }) { player: Player ->
    val parts = content.split(" ")
    player.sendVarcStr(parts.first().toInt(), content.removePrefix("${parts.first()} "))
}