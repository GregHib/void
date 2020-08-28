import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.client.ui.menu.InterfaceOptionSettings.getHash
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.*
import rs.dusk.utility.inject
import rs.dusk.world.command.Command

val details: InterfaceDetails by inject()

Command where { prefix == "inter" } then {
    val id = content.toIntOrNull()
    if (id == null) {
        val name = content
        player.interfaces.open(name)
    } else if (id != -1 || !closeInterface(player)) {
        val inter = details.get(id)
        var parent = if (player.gameFrame.resizable) 746 else 548
        var index = if (player.gameFrame.resizable) 12 else 9
        if (inter.data != null) {
            parent = inter.getParent(player.gameFrame.resizable)
            index = inter.getIndex(player.gameFrame.resizable)
        }
        if (id == -1) {
            player.send(InterfaceCloseMessage(parent, index))
        } else {
            player.send(InterfaceOpenMessage(false, parent, index, id))
        }
    }
}

fun closeInterface(player: Player): Boolean {
    val id = player.interfaces.get("main_screen") ?: return false
    return player.interfaces.close(id)
}

Command where { prefix == "show" } then {
    val parts = content.split(" ")
    player.send(InterfaceVisibilityMessage(parts[0].toInt(), parts[1].toInt(), !parts[2].toBoolean()))
}

Command where { prefix == "sendItem" } then {
    val parts = content.split(" ")
    player.interfaces.sendItem(parts[0], parts[1], parts[2].toInt(), parts.getOrNull(3)?.toInt() ?: 1)
}

Command where { prefix == "setting" } then {
    val parts = content.split(" ")
    val remainder = parts.subList(4, parts.size).map { it.toIntOrNull() }.requireNoNulls().toIntArray()
    player.send(InterfaceSettingsMessage(parts[0].toInt(), parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), getHash(*remainder)))
}

Command where { prefix == "script" } then {
    val parts = content.split(" ")
    val remainder = parts.subList(1, parts.size).map { it.toIntOrNull() ?: it }.toTypedArray()
    player.send(ScriptMessage(parts[0].toInt(), *remainder))
}