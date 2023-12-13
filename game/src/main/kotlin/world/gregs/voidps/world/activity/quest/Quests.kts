package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

val quests = listOf(
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
    "rune_mysteries",
    "the_knights_sword"
)

on<Command>({ prefix == "quests" }) { player: Player ->
    for (quest in quests) {
        player[quest] = "completed"
    }
    player["quest_points"] = 7
    player.refreshQuestJournal()
}