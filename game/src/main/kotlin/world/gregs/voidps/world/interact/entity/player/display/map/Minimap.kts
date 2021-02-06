package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.codec.game.encode.sendRunEnergy
import world.gregs.voidps.network.codec.game.encode.sendVarp

IntVariable(1240, Variable.Type.VARP, true, 1980).register("life_points")
BooleanVariable(102, Variable.Type.VARP).register("poisoned")

InterfaceOpened where { name == "health_orb" } then {
    player.sendVar("life_points")
    player.sendVar("poisoned")
}

InterfaceOpened where { name == "energy_orb" } then {
    player.sendRunEnergy(100)
}

InterfaceOpened where { name == "summoning_orb" } then {
    player.sendVarp(1160, -1)
}