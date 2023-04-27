package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 2 }) { player: Player ->
    val lines = when (player["demon_slayer", "unstarted"]) {
        else -> listOf(
            ""
        )
    }
    player.sendQuestJournal("Demon Slayer", lines)
}