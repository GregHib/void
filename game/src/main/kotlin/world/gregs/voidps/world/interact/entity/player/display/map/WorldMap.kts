package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.encode.updateInterface

val definitions: InterfaceDefinitions by inject()

interfaceOpen("world_map") { player: Player ->
    updateMap(player)
}

interfaceOption({ id == it.gameFrame.name && component == "world_map" && option == "*" }) { player: Player ->
    player.open("world_map")
}

interfaceOption({ id == "world_map" && component == "close" }) { player: Player ->
    // Mechanics are unknown, would need tracking last interface to handle inside Interfaces.kt
    player.client?.updateInterface(definitions.get(player.gameFrame.name).id, 2)
    player.open(player.gameFrame.name, close = false)
}

move({ it.interfaces.contains("world_map") }) { player: Player ->
    updateMap(player)
}

fun updateMap(player: Player) {
    val tile = player.tile.id
    player["world_map_centre"] = tile
    player["world_map_player"] = tile
}