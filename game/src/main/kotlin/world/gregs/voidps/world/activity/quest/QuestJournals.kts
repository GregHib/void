package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.sendVariable
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "quest_journals" }) { player: Player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables.yml
    player.sendVariable("unstable_foundations")
    //free
    player.sendVariable("cooks_assistant")
    player.sendVariable("dorics_quest")
    player.sendVariable("rune_mysteries")
    player.sendVariable("demon_slayer")
    //members
}