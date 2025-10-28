package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Wildcards

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
interface LevelChanged {
    fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {}

    fun levelChanged(skill: Skill? = null, block: (player: Player, skill: Skill, from: Int, to: Int) -> Unit) {
        playerChanged.getOrPut(skill) { mutableListOf() }.add(block)
    }

    fun npcLevelChanged(skill: Skill, id: String = "*", block: (npc: NPC, skill: Skill, from: Int, to: Int) -> Unit) {
        if (id == "*") {
            npcChanged.getOrPut(skill.name) { mutableListOf() }.add(block)
            return
        }
        for (match in Wildcards.find(id)) {
            npcChanged.getOrPut("$match:${skill.name}") { mutableListOf() }.add(block)
        }
    }

    companion object : LevelChanged {
        val playerChanged = mutableMapOf<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>()
        val npcChanged = mutableMapOf<String, MutableList<(NPC, Skill, from: Int, to: Int) -> Unit>>()

        fun changed(player: Player, skill: Skill, from: Int, to: Int) {
            for (block in playerChanged[skill] ?: emptyList()) {
                block(player, skill, from, to)
            }
            for (block in playerChanged[null] ?: emptyList()) {
                block(player, skill, from, to)
            }
        }

        fun changed(npc: NPC, skill: Skill, from: Int, to: Int) {
            for (block in npcChanged["${npc.id}:${skill.name}"] ?: emptyList()) {
                block(npc, skill, from, to)
            }
            for (block in npcChanged[skill.name] ?: emptyList()) {
                block(npc, skill, from, to)
            }
            for (block in npcChanged["*"] ?: emptyList()) {
                block(npc, skill, from, to)
            }
        }

        fun clear() {
            playerChanged.clear()
            npcChanged.clear()
        }
    }
}