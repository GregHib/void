package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 2 }) { player: Player ->
    val lines = when (player["demon_slayer", "unstarted"]) {
        "unstarted" -> listOf(
            "<navy>I can start this quest by speaking to the <maroon>Gypsy<navy> in the <maroon>tent",
            "<navy>in <maroon>Varrock's main square.",
            "",
            "<navy>I must be able to defeat a level 27 <maroon>apocalyptic demon<navy>!"
        )
        else -> listOf()
    }
    player.sendQuestJournal("Demon Slayer", lines)
}