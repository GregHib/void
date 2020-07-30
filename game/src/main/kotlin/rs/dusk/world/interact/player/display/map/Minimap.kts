package rs.dusk.world.interact.player.display.map

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.RunEnergyMessage
import rs.dusk.network.rs.codec.game.encode.message.VarpMessage
import rs.dusk.world.interact.player.display.InterfaceInteraction

IntVariable(7198, Variable.Type.VARBIT, true, 990).register("life_points")
IntVariable(2382, Variable.Type.VARP, true, 990).register("prayer_points")
BooleanVariable(181, Variable.Type.VARC).register("select_quick_prayers")
BooleanVariable(182, Variable.Type.VARC).register("using_quick_prayers")

StringMapVariable(1584, Variable.Type.VARP, true, mapOf(
    0 to "normal",
    1 to "curses"
)).register("prayer_list")
BooleanVariable(102, Variable.Type.VARP).register("poisoned")

InterfaceOpened where { name == "health_orb" } then {
    player.sendVar("life_points")
    player.sendVar("poisoned")
}

InterfaceOpened where { name == "prayer_orb" } then {
    player.sendVar("using_quick_prayers")
    player.sendVar("select_quick_prayers")
}

InterfaceInteraction where { name == "prayer_orb" && component == "orb" } then {
    when(optionId) {
        1 -> player.toggleVar("using_quick_prayers")
        2 -> player.toggleVar("select_quick_prayers")
    }
}

InterfaceOpened where { name == "energy_orb" } then {
    player.sendVar("energy_orb")
    player.send(RunEnergyMessage(100))
}

InterfaceOpened where { name == "summoning_orb" } then {
    player.send(VarpMessage(1160, -1))
}