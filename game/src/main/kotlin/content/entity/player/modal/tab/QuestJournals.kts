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
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.inject

val logger = InlineLogger()

interfaceOpen("quest_journals") { player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables-player.yml
    player.sendVariable("unstable_foundations")
    for (quest in questDefinitions.ids.keys) {
        player.sendVariable(quest)
    }
}

val questDefinitions: QuestDefinitions by inject()

interfaceOption(component = "journals", id = "quest_journals") {
    val quest = questDefinitions.getOrNull(itemSlot)
    if (quest == null) {
        logger.warn { "Unknown quest $itemSlot" }
        return@interfaceOption
    }
    player.emit(OpenQuestJournal(player, quest.stringId))
}

variableSet(ids = questDefinitions.ids.keys.toTypedArray()) { player ->
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