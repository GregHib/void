package content.entity.player.modal.tab

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.bank
import content.entity.player.command.find
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.boolArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.ui.InterfaceApi
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.timer.Timer

class QuestJournals(
    val accounts: AccountDefinitions,
    val questDefinitions: QuestDefinitions
) : Script {

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
        }

        interfaceOption(id = "quest_journals:journals") { (_, itemSlot) ->
            val quest = questDefinitions.getOrNull(itemSlot)
            if (quest == null) {
                logger.warn { "Unknown quest $itemSlot" }
                return@interfaceOption
            }
            closeInterfaces()
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

        adminCommand(
            "reset_quest",
            stringArg("quest", desc = "Name of the quest to reset", autofill = questDefinitions.ids.keys),
            boolArg("remove-items", desc = "Removes all quest related items from the player", optional = true),
            stringArg("player-name", optional = true, autofill = accounts.displayNames.keys),
            desc = "Resets a quest for the player",
            handler = ::resetQuest,
        )
    }

    /**
     * Resets a player's progress on a quest
     * Note: Not foolproof
     *   - Doesn't remove them from a quest area
     *   - Dropped quest items can avoid the reset
     *   - Doesn't reset rewards
     *   - Allows player to reclaim rewards (such as XP)
     */
    fun resetQuest(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(2)) ?: return
        val id = args[0]
        val def = questDefinitions.getOrNull(id) ?: return
        val vars: List<String> = def.getOrNull("variables") ?: emptyList()
        for (variable in vars) {
            target.clear(variable)
        }
        val removeItems = args.getOrNull(1)?.toBooleanStrictOrNull() ?: true
        if (removeItems) {
            val items: List<String> = def.getOrNull("items") ?: emptyList()
            for (item in items) {
                removeItems(target, item)
            }
        }
    }

    private fun removeItems(player: Player, item: String) {
        player.inventory.removeToLimit(item, Int.MAX_VALUE)
        player.bank.removeToLimit(item, Int.MAX_VALUE)
        player.beastOfBurden.removeToLimit(item, Int.MAX_VALUE)
        player.equipment.removeToLimit(item, Int.MAX_VALUE)
    }
}
