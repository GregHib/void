package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.updateInterface
import world.gregs.voidps.engine.utility.inject

val definitions: InterfaceDefinitions by inject()

on<InterfaceOpened>({ name == "world_map" }) { player: Player ->
    updateMap(player)
}

on<InterfaceOption>({ name == it.gameFrame.name && component == "world_map" && option == "*" }) { player: Player ->
    player.open("world_map")
}

on<InterfaceOption>({ name == "world_map" && component == "close" }) { player: Player ->
    // Mechanics are unknown, would need tracking last interface to handle inside Interfaces.kt
    player.client?.updateInterface(definitions.getId(player.gameFrame.name), 2)
    player.open(player.gameFrame.name)
}

on<Moved>({ it.interfaces.contains("world_map") }) { player: Player ->
    updateMap(player)
}

fun updateMap(player: Player) {
    val tile = player.tile.id
    player.setVar("world_map_centre", tile)
    player.setVar("world_map_player", tile)
}