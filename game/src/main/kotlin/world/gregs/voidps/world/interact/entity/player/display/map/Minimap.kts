package world.gregs.voidps.world.interact.entity.player.display.map

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.BooleanVariable
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.sendRunEnergy
import world.gregs.voidps.network.encode.sendVarp

IntVariable(7198, Variable.Type.VARBIT, true, 100).register("life_points")
BooleanVariable(102, Variable.Type.VARP).register("poisoned")

on<InterfaceOpened>({ name == "health_orb" }) { player: Player ->
    player.sendVar("life_points")
    player.sendVar("poisoned")
}

on<InterfaceOpened>({ name == "energy_orb" }) { player: Player ->
    player.sendRunEnergy(100)
}

on<InterfaceOpened>({ name == "summoning_orb" }) { player: Player ->
    player.sendVarp(1160, -1)
}