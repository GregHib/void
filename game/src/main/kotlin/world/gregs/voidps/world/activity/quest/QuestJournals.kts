package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop

val quests = setOf(
    // free
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
    "the_knights_sword",
    "rune_mysteries"
    // members
)

interfaceOpen("quest_journals") { player: Player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables.yml
    player.sendVariable("unstable_foundations")
    for (quest in quests) {
        player.sendVariable(quest)
    }
}

variableSet(quests) { player: Player ->
    player.softTimers.start("refresh_quest_journal")
}

timerStart("refresh_quest_journal") { _: Player ->
    interval = 1
}

timerStop("refresh_quest_journal") { player: Player ->
    player.refreshQuestJournal()
}

playerSpawn { player: Player ->
    player.clearCamera()
}