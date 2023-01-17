package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "quest_journals"}) { player: Player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVar("quest_points")
    player.sendVar("quest_points_total") //set total quest points available in variables.yml
    player.sendVar("dorics_quest")
    player.sendVar("unstable_foundations")
}