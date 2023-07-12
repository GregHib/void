package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "quest_journals" }) { player: Player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables.yml
    player.sendVariable("unstable_foundations")
    //free
    player.sendVariable("cooks_assistant")
    player.sendVariable("demon_slayer")
    player.sendVariable("dorics_quest")
    player.sendVariable("the_knights_sword")
    player.sendVariable("rune_mysteries")
    //members
}

on<Registered> { player: Player ->
    player.clearCamera()
}