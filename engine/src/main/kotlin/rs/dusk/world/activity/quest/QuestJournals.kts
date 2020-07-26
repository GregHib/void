package rs.dusk.world.activity.quest

import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.variable.IntVariable
import rs.dusk.engine.model.engine.variable.StringMapVariable
import rs.dusk.engine.model.engine.variable.Variable
import rs.dusk.engine.model.engine.variable.sendVar

IntVariable(101, Variable.Type.VARP, true, 0).register("quest_points")

StringMapVariable(281, Variable.Type.VARP, true, defaultValue = "complete", values = mapOf(
    0 to "unstarted",
    1 to "incomplete",
    1000 to "complete"
)).register("unstable_foundations")

InterfaceOpened where { name == "quest_journals"} then {
    player.sendVar("quest_points")
    player.sendVar("unstable_foundations")
}