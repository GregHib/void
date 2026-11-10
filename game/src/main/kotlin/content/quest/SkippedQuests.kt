package content.quest

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.ListValues
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Players

/**
 * Quests which aren't implemented yet default to their completed stage so npcs and objects
 * which change with quest progress show their post-quest state, matching [questCompleted].
 * Only the default changes; a player's saved progress is left untouched.
 */
class SkippedQuests(val questDefinitions: QuestDefinitions) : Script {

    private val originals = mutableMapOf<String, Any?>()

    init {
        worldSpawn {
            apply()
        }

        settingsReload {
            apply()
            for (player in Players) {
                for (variable in originals.keys) {
                    player.sendVariable(variable)
                }
            }
        }
    }

    private fun apply() {
        for (quest in questDefinitions.ids.keys) {
            val skipped = questSkipped(quest)
            for (name in setOf(quest) + questDefinitions.get(quest)["variables", emptyList<String>()]) {
                val definition = VariableDefinitions.get(name) ?: continue
                val values = definition.values
                if (values !is MapValues && values !is ListValues) {
                    continue
                }
                val original = originals.getOrPut(name) { definition.defaultValue }
                definition.defaultValue = if (skipped && completed(values)) "completed" else original
            }
        }
    }

    private fun completed(values: Any): Boolean = when (values) {
        is MapValues -> values.values.containsKey("completed")
        is ListValues -> values.values.contains("completed")
        else -> false
    }
}
