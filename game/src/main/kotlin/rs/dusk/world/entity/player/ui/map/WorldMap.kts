package rs.dusk.world.entity.player.ui.map

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.variable.IntVariable
import rs.dusk.engine.model.engine.variable.Variable
import rs.dusk.engine.model.engine.variable.setVar

IntVariable(622, Variable.Type.VARC, false, 0).register("world_map_position")
IntVariable(674, Variable.Type.VARC, false, 0).register("world_map_position_2")

InterfaceOpened where { name == "world_map" } then {
    val position = player.tile.id
    player.setVar("world_map_position", position)
    player.setVar("world_map_position_2", position)
}