package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.StringMapVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

IntVariable(101, Variable.Type.VARP, true, 0).register("quest_points")

StringMapVariable(281, Variable.Type.VARP, true, defaultValue = "complete", values = mapOf(
    "unstarted" to 0,
    "incomplete" to 1,
    "complete" to 1000
)).register("unstable_foundations")

on<InterfaceOpened>({ name == "quest_journals"}) { player: Player ->
    player.sendVar("quest_points")
    player.sendVar("unstable_foundations")
}