package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop

val quests = arrayOf(
    // free
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
	"gunnars_ground",
    "the_knights_sword",
    "the_restless_ghost",
    "rune_mysteries",
    // members
    "druidic_ritual"
)

interfaceOpen("quest_journals") { player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables-player.yml
    player.sendVariable("unstable_foundations")
    for (quest in quests) {
        player.sendVariable(quest)
    }
}

variableSet(ids = quests) { player ->
    player.softTimers.start("refresh_quest_journal")
}

timerStart("refresh_quest_journal") {
    interval = 1
}

timerStop("refresh_quest_journal") { player ->
    player.refreshQuestJournal()
}

playerSpawn { player ->
    player.clearCamera()
}