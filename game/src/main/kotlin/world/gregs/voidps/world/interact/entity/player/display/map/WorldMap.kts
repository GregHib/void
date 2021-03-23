package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

IntVariable(622, Variable.Type.VARC, false, 0).register("world_map_position")
IntVariable(674, Variable.Type.VARC, false, 0).register("world_map_position_2")

on<InterfaceOpened>({ name == "world_map" }) { player: Player ->
    val position = player.tile.id
    player.setVar("world_map_position", position)
    player.setVar("world_map_position_2", position)
}

on<InterfaceOption>({ name == it.gameFrame.name && component == "world_map" && option == "*" }) { player: Player ->
    player.open("world_map")
}

on<InterfaceOption>({ name == "world_map" && component == "close" }) { player: Player ->
    player.close("world_map")
}