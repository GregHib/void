import rs.dusk.engine.client.send
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage
import rs.dusk.world.command.Command

Command where { prefix == "inter" } then {
    val id = content.toIntOrNull()
    if(id == null) {
        val name = content
        player.interfaces.open(name)
    } else if (id != -1 || !closeInterface(player)) {
        val parent = if(player.gameFrame.resizable) 746 else 548
        val index = if(player.gameFrame.resizable) 12 else 9
        if(id == -1) {
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
    player.interfaces.sendVisibility(parts[0], parts[1], parts[2].toBoolean())
}

Command where { prefix == "sendItem" } then {
    val parts = content.split(" ")
    player.interfaces.sendItem(parts[0], parts[1], parts[2].toInt(), parts.getOrNull(3)?.toInt() ?: 1)
}

Command where { prefix == "setting" } then {
    val parts = content.split(" ")
    player.interfaces.sendSetting(parts[0], parts[1], parts[2].toInt(), parts[3].toInt(), parts.getOrNull(4)?.toInt() ?: 0)
}

Command where { prefix == "sendItems" } then {
    player.send(ContainerItemsMessage(90, IntArray(28) { 995 }, IntArray(28) { 1 }, true))
    player.send(ContainerItemsMessage(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false))
}