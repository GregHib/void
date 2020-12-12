package rs.dusk.world.interact.entity.player.display.map

import rs.dusk.engine.client.ui.close
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

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