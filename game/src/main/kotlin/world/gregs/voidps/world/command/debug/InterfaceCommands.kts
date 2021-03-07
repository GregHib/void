import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.client.ui.menu.InterfaceOptionSettings.getHash
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.codec.game.encode.*
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.command.Command

val details: InterfaceDetails by inject()
val closeEncoder: InterfaceCloseEncoder by inject()
val openEncoder: InterfaceOpenEncoder by inject()
val visibleEncoder: InterfaceVisibilityEncoder by inject()
val settingsEncoder: InterfaceSettingsEncoder by inject()

Command where { prefix == "inter" } then {
    val id = content.toIntOrNull()
    if (id == null) {
        val name = content
        player.interfaces.open(name)
    } else if (id != -1 || !closeInterface(player)) {
        val inter = details.getSafe(details.getNameOrNull(id) ?: "")
        var parent = if (player.gameFrame.resizable) 746 else 548
        var index = if (player.gameFrame.resizable) 12 else 9
        if (inter.data != null) {
            parent = inter.getParent(player.gameFrame.resizable)
            index = inter.getIndex(player.gameFrame.resizable)
        }
        if (id == -1) {
            closeEncoder.encode(player, parent, index)
        } else {
            openEncoder.encode(player, false, parent, index, id)
        }
    }
}

fun closeInterface(player: Player): Boolean {
    val id = player.interfaces.get("main_screen") ?: player.interfaces.get("underlay") ?: return false
    return player.interfaces.close(id)
}

Command where { prefix == "show" } then {
    val parts = content.split(" ")
    visibleEncoder.encode(player, parts[0].toInt(), parts[1].toInt(), !parts[2].toBoolean())
}

Command where { prefix == "sendItem" } then {
    val parts = content.split(" ")
    player.interfaces.sendItem(parts[0], parts[1], parts[2].toInt(), parts.getOrNull(3)?.toInt() ?: 1)
}

Command where { prefix == "setting" } then {
    val parts = content.split(" ")
    val remainder = parts.subList(4, parts.size).map { it.toIntOrNull() }.requireNoNulls().toIntArray()
    player.message("Settings sent ${remainder.toList()}", ChatType.Console)
    settingsEncoder.encode(player, parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), getHash(*remainder))
}

Command where { prefix == "script" } then {
    val parts = content.split(" ")
    val remainder = parts.subList(1, parts.size).map { it.toIntOrNull() ?: it }.toTypedArray()
    player.sendScript(parts[0].toInt(), *remainder)
}

Command where { prefix == "sendItems" } then {
    repeat(1200) {
        player.sendContainerItems(it, intArrayOf(), intArrayOf(), false)
    }
    for (container in 0 until 1200) {
        player.sendContainerItems(container, IntArray(1) { 995 }, IntArray(1) { 100 }, false)
    }
}

Command where { prefix == "varp" } then {
    val parts = content.split(" ")
    player.sendVarp(parts.first().toInt(), parts.last().toInt())
}

Command where { prefix == "varbit" } then {
    val parts = content.split(" ")
    player.sendVarbit(parts.first().toInt(), parts.last().toInt())
}

Command where { prefix == "varc" } then {
    val parts = content.split(" ")
    player.sendVarc(parts.first().toInt(), parts.last().toInt())
}

Command where { prefix == "varcstr" } then {
    val parts = content.split(" ")
    player.sendVarcStr(parts.first().toInt(), content.removePrefix("${parts.first()} "))
}