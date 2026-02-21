package content.entity.player.modal.tab

import com.github.michaelbull.logging.InlineLogger
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.timer.Timer

class QuestJournals(val questDefinitions: QuestDefinitions) : Script {

    val logger = InlineLogger()

    init {
        playerSpawn {
            clearCamera()
            sendVariable("quest_journal_show_all")
            sendVariable("quest_journal_order")
        }

        timerStart("refresh_quest_journal") { 1 }
        timerTick("refresh_quest_journal") { Timer.CANCEL }
        timerStop("refresh_quest_journal") { refreshQuestJournal() }

        variableSet { key, _, _ ->
            if (questDefinitions.ids.containsKey(key)) {
                softTimers.start("refresh_quest_journal")
            }
        }

        interfaceOpened("quest_journals") { id ->
            interfaceOptions.unlock(id, "journals", 0 until 201, "View")
            interfaceOptions.unlockAll(id, "order", 0 until 3)
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
            InterfaceApi.openQuestJournal(this, quest.stringId)
        }

        interfaceOption("Filter quests", "quest_journals:filter") {
            toggle("quest_journal_show_all")
        }

        interfaceOption(id = "quest_journals:order") {
            val type = when (it.itemSlot) {
                0 -> "free_members"
                1 -> "progress"
                2 -> "difficulty"
                else -> return@interfaceOption
            }
            if (get("quest_journal_order", "free_members") == type) {
                toggle("quest_journal_reversed")
            } else {
                set("quest_journal_reversed", false)
                set("quest_journal_order", type)
            }
        }
    }
}
