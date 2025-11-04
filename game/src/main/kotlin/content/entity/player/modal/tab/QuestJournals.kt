package content.entity.player.modal.tab

import com.github.michaelbull.logging.InlineLogger
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.entity.InterfaceInteraction
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.Timer

class QuestJournals : Script {

    val logger = InlineLogger()

    val questDefinitions: QuestDefinitions by inject()

    init {
        playerSpawn {
            clearCamera()
        }

        timerStart("refresh_quest_journal") { 1 }
        timerTick("refresh_quest_journal") { Timer.CANCEL }
        timerStop("refresh_quest_journal") { refreshQuestJournal() }

        variableSet { key, _, _ ->
            if (questDefinitions.ids.containsKey(key)) {
                softTimers.start("refresh_quest_journal")
            }
        }

        interfaceOpen("quest_journals") { id ->
            interfaceOptions.unlock(id, "journals", 0 until 201, "View")
            sendVariable("quest_points")
            sendVariable("quest_points_total") // set total quest points available in variables-player.yml
            sendVariable("unstable_foundations")
            for (quest in questDefinitions.ids.keys) {
                sendVariable(quest)
            }
        }

        interfaceOption(id = "quest_journals:journals") { (_, itemSlot) ->
            val quest = questDefinitions.getOrNull(itemSlot)
            if (quest == null) {
                logger.warn { "Unknown quest $itemSlot" }
                return@interfaceOption
            }
            InterfaceInteraction.openQuestJournal(this, quest.stringId)
        }
    }
}
