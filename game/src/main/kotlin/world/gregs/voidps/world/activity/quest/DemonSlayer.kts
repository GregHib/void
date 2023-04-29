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
        "stage1" -> listOf(
            "<str>I spoke to Aris in Varrock Square who saw my future.",
            "<str>Unfortunately it involved killing a demon who nearly",
            "<str>destroyed Varrock over 150 years ago.",
            "",
            "<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.",
            "<navy>I should ask <maroon>Sir Prysin<navy> in <maroon>Varrock Palace<navy> where it is."
        )
        else -> listOf()
    }
    player.sendQuestJournal("Demon Slayer", lines)
}