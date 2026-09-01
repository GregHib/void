package content.quest

import content.entity.player.inv.item.addOrDrop
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory

/**
 * Sets a player up to attempt a quest: skills raised to the requirements listed in `quests.toml`,
 * prerequisite quests marked complete, and any items the quest expects the player to bring handed
 * over. Requirements come from the quest definition, so this works for every quest that declares
 * them; supply-your-own items are read from the optional `req_item_ids` key, where an item
 * needed more than once is written as "id:amount".
 */
class QuestCommands : Script {

    init {
        adminCommand(
            "questreset",
            stringArg("quest-id", "the quest to reset", autofill = { get<QuestDefinitions>().ids.keys }),
            desc = "Reset a quest back to unstarted, clearing its variables",
            handler = ::reset,
        )

        adminCommand(
            "questprep",
            stringArg("quest-id", "the quest to prepare for", autofill = { get<QuestDefinitions>().ids.keys }),
            desc = "Meet a quest's skill, quest and item requirements",
            handler = ::prepare,
        )
    }

    /**
     * Puts a quest back to unstarted. Along with the quest variable itself this clears every
     * variable named after it, which is how quest progress flags are named - Rum Deal's counters
     * and swab flags are all `rum_deal_*` - plus anything the quest lists under `reset_vars`, for
     * the flags it keeps under the client's own varbit names. Variables a quest shares with a skill
     * (the blindweed farming patch, say) aren't named after the quest and are left alone.
     */
    private fun reset(player: Player, args: List<String>) {
        val questId = args.getOrNull(0) ?: return
        val questDefinitions: QuestDefinitions = get()
        val quest = questDefinitions.getOrNull(questId)
        if (quest == null) {
            player.message("No quest found with id '$questId'.")
            return
        }

        player.clear(questId)
        var cleared = 0
        val named = VariableDefinitions.definitions.keys.filter { it.startsWith("${questId}_") }
        for (variable in named + quest["reset_vars", emptyList<String>()]) {
            if (VariableDefinitions.get(variable) == null) {
                player.message("Unknown variable '$variable' in ${quest.stringId} reset list.")
                continue
            }
            if (player.variables.contains(variable)) {
                player.clear(variable)
                cleared++
            }
        }
        player.refreshQuestJournal()
        player.message("Reset ${quest["name", questId]} to unstarted, clearing $cleared ${if (cleared == 1) "variable" else "variables"}.")
    }

    private fun prepare(player: Player, args: List<String>) {
        val questId = args.getOrNull(0) ?: return
        val questDefinitions: QuestDefinitions = get()
        val quest = questDefinitions.getOrNull(questId)
        if (quest == null) {
            player.message("No quest found with id '$questId'.")
            return
        }

        val skills = quest["req_skills", emptyMap<String, Int>()]
        for ((name, level) in skills) {
            val skill = Skill.entries.firstOrNull { it.name == name }
            if (skill == null) {
                player.message("Unknown skill '$name' in ${quest.stringId} requirements.")
                continue
            }
            if (player.levels.getMax(skill) < level) {
                player.experience.set(skill, Level.experience(level))
            }
        }

        val quests = quest["req_quests", emptyList<String>()]
        for (required in quests) {
            player[required] = "completed"
        }

        val items = quest["req_item_ids", emptyList<String>()]
        var given = 0
        for (entry in items) {
            // Items a quest needs more than one of are written as "id:amount"
            val id = entry.substringBefore(':')
            val amount = entry.substringAfter(':', "1").toIntOrNull() ?: 1
            if (ItemDefinitions.getOrNull(id) == null) {
                player.message("Unknown item '$id' in ${quest.stringId} requirements.")
                continue
            }
            if (!player.inventory.contains(id, amount)) {
                player.addOrDrop(id, amount)
                given++
            }
        }

        player.message(
            "Prepared for ${quest["name", questId]}: ${skills.size} skills, " +
                "${quests.size} ${if (quests.size == 1) "quest" else "quests"}, $given items.",
        )
    }
}
