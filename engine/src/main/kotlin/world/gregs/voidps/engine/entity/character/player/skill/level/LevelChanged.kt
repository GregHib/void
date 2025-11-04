package world.gregs.voidps.engine.entity.character.player.skill.level

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
interface LevelChanged {

    fun levelChanged(skill: Skill? = null, block: Player.(skill: Skill, from: Int, to: Int) -> Unit) {
        playerChanged.getOrPut(skill) { mutableListOf() }.add(block)
    }

    fun npcLevelChanged(skill: Skill, id: String = "*", block: NPC.(skill: Skill, from: Int, to: Int) -> Unit) {
        if (id == "*") {
            npcChanged.getOrPut(skill.name) { mutableListOf() }.add(block)
            return
        }
        Wildcards.find(id, Wildcard.Npc) { match ->
            npcChanged.getOrPut("$match:${skill.name}") { mutableListOf() }.add(block)
        }
    }

    companion object : LevelChanged {
        val playerChanged = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>(30)
        val npcChanged = Object2ObjectOpenHashMap<String, MutableList<(NPC, Skill, from: Int, to: Int) -> Unit>>(15)

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