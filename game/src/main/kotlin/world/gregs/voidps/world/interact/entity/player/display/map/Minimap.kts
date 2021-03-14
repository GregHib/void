package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.BooleanVariable
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.encode.sendRunEnergy
import world.gregs.voidps.network.encode.sendVarp

IntVariable(7198, Variable.Type.VARBIT, true, 100).register("life_points")
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