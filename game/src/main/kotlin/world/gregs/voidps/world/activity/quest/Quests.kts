package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.event.adminCommand

val quests = listOf(
    "unstable_foundations",
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
    "gunnars_ground",
    "the_restless_ghost",
    "rune_mysteries",
    "the_knights_sword",
    // mini-quests
    "enter_the_abyss",
)

adminCommand("quests") {
    for (quest in quests) {
        player[quest] = "completed"
    }
    player["quest_points"] = 7
    player.refreshQuestJournal()
}