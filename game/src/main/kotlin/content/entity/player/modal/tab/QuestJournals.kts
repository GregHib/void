package content.entity.player.modal.tab

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.client.ui.interfaceOption

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

val logger = InlineLogger()

interfaceOpen("quest_journals") { player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables-player.yml
    player.sendVariable("unstable_foundations")
    for (quest in quests) {
        player.sendVariable(quest)
    }
}

interfaceOption(component = "journals", id = "quest_journals") {
    val quest = when (itemSlot) {
        1 -> "cooks_assistant"
        2 -> "demon_slayer"
        3 -> "dorics_quest"
        17 -> "gunnars_ground"
        13 -> "rune_mysteries"
        8 -> "the_knights_sword"
        11 -> "the_restless_ghost"
        33 -> "druidic_ritual"
        else -> return@interfaceOption logger.warn { "Unknown quest $itemSlot" }
    }
    player.emit(OpenQuestJournal(player, quest))
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