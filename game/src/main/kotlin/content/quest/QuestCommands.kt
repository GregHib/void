package content.quest

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.cache.config.data.QuestDefinition
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
 * them; supply-your-own items are read from the optional `req_item_ids` key.
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
     * and swab flags are all `rum_deal_*`. Variables a quest shares with a skill (the blindweed
     * farming patch, say) aren't named after the quest and are left alone.
     */
    private fun reset(player: Player, args: List<String>) {
        val questId = args.getOrNull(0) ?: return
        val quest = findQuest(player, questId) ?: return
        player.clear(questId)
        var cleared = 0
        for (variable in VariableDefinitions.definitions.keys) {
            if (variable.startsWith("${questId}_") && player.variables.contains(variable)) {
                player.clear(variable)
                cleared++
            }
        }
        player.refreshQuestJournal()
        player.message("Reset ${quest["name", questId]} to unstarted, clearing $cleared ${if (cleared == 1) "variable" else "variables"}.")
    }

    private fun findQuest(player: Player, questId: String): QuestDefinition? {
        val questDefinitions: QuestDefinitions = get()
        val quest = questDefinitions.getOrNull(questId)
        if (quest == null) {
            player.message("No quest found with id '$questId'.")
            return  null
        }
        return quest
    }

    private fun prepare(player: Player, args: List<String>) {
        val questId = args.getOrNull(0) ?: return
        val quest = findQuest(player, questId) ?: return
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
        for (item in items) {
            if (ItemDefinitions.getOrNull(item) == null) {
                player.message("Unknown item '$item' in ${quest.stringId} requirements.")
                continue
            }
            if (!player.inventory.contains(item)) {
                player.addOrDrop(item)
                given++
            }
        }

        player.message(
            "Prepared for ${quest["name", questId]}: ${skills.size} skills, " +
                "${quests.size} ${if (quests.size == 1) "quest" else "quests"}, $given items.",
        )
    }
}
