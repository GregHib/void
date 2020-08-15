package rs.dusk.world.activity.quest

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.StringMapVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.sendVar
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where

IntVariable(101, Variable.Type.VARP, true, 0).register("quest_points")

StringMapVariable(281, Variable.Type.VARP, true, defaultValue = "complete", values = mapOf(
    "unstarted" to 0,
    "incomplete" to 1,
    "complete" to 1000
)).register("unstable_foundations")

InterfaceOpened where { name == "quest_journals"} then {
    player.sendVar("quest_points")
    player.sendVar("unstable_foundations")
}