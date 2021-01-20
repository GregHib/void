package world.gregs.void.world.interact.entity.player.display.map

import world.gregs.void.engine.client.ui.close
import world.gregs.void.engine.client.ui.event.InterfaceOpened
import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.client.variable.IntVariable
import world.gregs.void.engine.client.variable.Variable
import world.gregs.void.engine.client.variable.setVar
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

IntVariable(622, Variable.Type.VARC, false, 0).register("world_map_position")
IntVariable(674, Variable.Type.VARC, false, 0).register("world_map_position_2")

InterfaceOpened where { name == "world_map" } then {
    val position = player.tile.id
    player.setVar("world_map_position", position)
    player.setVar("world_map_position_2", position)
}

InterfaceOption where { name == player.gameFrame.name && component == "world_map" && option == "*" } then {
    player.open("world_map")
}

InterfaceOption where { name == "world_map" && component == "close" } then {
    player.close("world_map")
}