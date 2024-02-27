package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.adminCommand

val quests = listOf(
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
	"gunnars_ground",
    "rune_mysteries",
    "the_knights_sword"
)

adminCommand("quests") {
    for (quest in quests) {
        player[quest] = "completed"
    }
    player["quest_points"] = 7
    player.refreshQuestJournal()
}