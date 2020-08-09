package rs.dusk.world.activity.quest

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.StringMapVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.sendVar
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where

StringMapVariable(1584, Variable.Type.VARP, true, mapOf(
    0 to "normal",
    1 to "curses"
)).register("prayer_list")

InterfaceOpened where { name == "prayer_list"} then {
    var quickPrayers = false
    player.sendVar("prayer_list")
    player.sendVar("prayer_points")
    player.interfaces.sendSetting(name, if (quickPrayers) "quick_prayers" else "regular_prayers", 0, 29, 2)
}