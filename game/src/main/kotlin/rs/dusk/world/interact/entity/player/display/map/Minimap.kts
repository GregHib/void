package rs.dusk.world.interact.entity.player.display.map

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.BooleanVariable
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.sendVar
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.RunEnergyMessage
import rs.dusk.network.rs.codec.game.encode.message.VarpMessage

IntVariable(7198, Variable.Type.VARBIT, true, 990).register("life_points")
BooleanVariable(102, Variable.Type.VARP).register("poisoned")

InterfaceOpened where { name == "health_orb" } then {
    player.sendVar("life_points")
    player.sendVar("poisoned")
}

InterfaceOpened where { name == "energy_orb" } then {
    player.sendVar("energy_orb")
    player.send(RunEnergyMessage(100))
}

InterfaceOpened where { name == "summoning_orb" } then {
    player.send(VarpMessage(1160, -1))
}