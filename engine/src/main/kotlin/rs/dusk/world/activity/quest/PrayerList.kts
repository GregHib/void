package rs.dusk.world.activity.quest

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.variable.StringMapVariable
import rs.dusk.engine.model.engine.variable.Variable
import rs.dusk.engine.model.engine.variable.sendVar
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage

StringMapVariable(1584, Variable.Type.VARP, true, mapOf(
    0 to "normal",
    1 to "curses"
)).register("prayer_list")

InterfaceOpened where { name == "prayer_list"} then {
    var quickPrayers = false
    player.sendVar("prayer_list")
    player.sendVar("prayer_points")
    player.send(InterfaceSettingsMessage(id, if (quickPrayers) 42 else 8, 0, 29, 2))
}