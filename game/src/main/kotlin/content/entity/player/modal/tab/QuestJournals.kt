package content.entity.player.modal.tab

import com.github.michaelbull.logging.InlineLogger
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.Timer

@Script
class QuestJournals : Api {

    val logger = InlineLogger()

    val questDefinitions: QuestDefinitions by inject()

    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (questDefinitions.ids.containsKey(key)) {
            player.softTimers.start("refresh_quest_journal")
        }
    }

    @Timer("refresh_quest_journal")
    override fun start(player: Player, timer: String, restart: Boolean): Int = 1

    @Timer("refresh_quest_journal")
    override fun tick(player: Player, timer: String): Int = Timer.CANCEL

    @Timer("refresh_quest_journal")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.refreshQuestJournal()
    }

    init {
        interfaceOpen("quest_journals") { player ->
            player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
            player.sendVariable("quest_points")
            player.sendVariable("quest_points_total") // set total quest points available in variables-player.yml
            player.sendVariable("unstable_foundations")
            for (quest in questDefinitions.ids.keys) {
                player.sendVariable(quest)
            }
        }

        interfaceOption(component = "journals", id = "quest_journals") {
            val quest = questDefinitions.getOrNull(itemSlot)
            if (quest == null) {
                logger.warn { "Unknown quest $itemSlot" }
                return@interfaceOption
            }
            player.emit(OpenQuestJournal(player, quest.stringId))
        }
    }

    override fun spawn(player: Player) {
        player.clearCamera()
    }
}
