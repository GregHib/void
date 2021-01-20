package world.gregs.void.world.interact.entity.player.display.map

import world.gregs.void.engine.client.ui.event.InterfaceOpened
import world.gregs.void.engine.client.variable.BooleanVariable
import world.gregs.void.engine.client.variable.IntVariable
import world.gregs.void.engine.client.variable.Variable
import world.gregs.void.engine.client.variable.sendVar
import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.network.codec.game.encode.sendRunEnergy
import world.gregs.void.network.codec.game.encode.sendVarp

IntVariable(7198, Variable.Type.VARBIT, true, 990).register("life_points")
BooleanVariable(102, Variable.Type.VARP).register("poisoned")

InterfaceOpened where { name == "health_orb" } then {
    player.sendVar("life_points")
    player.sendVar("poisoned")
}

InterfaceOpened where { name == "energy_orb" } then {
    player.sendVar("energy_orb")
    player.sendRunEnergy(100)
}

InterfaceOpened where { name == "summoning_orb" } then {
    player.sendVarp(1160, -1)
}