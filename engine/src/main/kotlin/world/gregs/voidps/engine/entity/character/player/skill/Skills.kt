package world.gregs.voidps.engine.entity.character.player.skill

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

/**
 * Notification when current or max skill level has changed.
 */
interface Skills {

    fun levelChanged(skill: Skill? = null, block: Player.(skill: Skill, from: Int, to: Int) -> Unit) {
        playerChanged.getOrPut(skill) { mutableListOf() }.add(block)
    }

    fun npcLevelChanged(skill: Skill, id: String = "*", block: NPC.(skill: Skill, from: Int, to: Int) -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { match ->
            npcChanged.getOrPut("$match:${skill.name}") { mutableListOf() }.add(block)
        }
    }

    fun maxLevelChanged(skill: Skill? = null, block: Player.(skill: Skill, from: Int, to: Int) -> Unit) {
        playerMaxChanged.getOrPut(skill) { mutableListOf() }.add(block)
    }

    fun experience(handler: Player.(skill: Skill, from: Double, to: Double) -> Unit) {
        experience.add(handler)
    }

    fun blockedExperience(handler: Player.(skill: Skill, exp: Double) -> Unit) {
        blockedExperience = handler
    }

    companion object : AutoCloseable {
        val playerChanged = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>(30)
        val npcChanged = Object2ObjectOpenHashMap<String, MutableList<(NPC, Skill, from: Int, to: Int) -> Unit>>(15)

        val playerMaxChanged = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>(15)

        val experience = ObjectArrayList<(Player, Skill, Double, Double) -> Unit>(5)
        var blockedExperience: (Player.(Skill, Double) -> Unit)? = null

        fun exp(player: Player, skill: Skill, from: Double, to: Double) {
            for (handler in experience) {
                handler.invoke(player, skill, from, to)
            }
        }

        fun blocked(player: Player, skill: Skill, exp: Double) {
            blockedExperience?.invoke(player, skill, exp)
        }

        fun changed(player: Player, skill: Skill, from: Int, to: Int) {
            for (block in playerChanged[skill] ?: emptyList()) {
                block(player, skill, from, to)
            }
            for (block in playerChanged[null] ?: return) {
                block(player, skill, from, to)
            }
        }

        fun changed(npc: NPC, skill: Skill, from: Int, to: Int) {
            for (block in npcChanged["${npc.id}:${skill.name}"] ?: emptyList()) {
                block(npc, skill, from, to)
            }
            for (block in npcChanged["*:${skill.name}"] ?: emptyList()) {
                block(npc, skill, from, to)
            }
        }

        fun maxChanged(player: Player, skill: Skill, from: Int, to: Int) {
            for (block in playerMaxChanged[skill] ?: emptyList()) {
                block(player, skill, from, to)
            }
            for (block in playerMaxChanged[null] ?: return) {
                block(player, skill, from, to)
            }
        }

        override fun close() {
            playerChanged.clear()
            npcChanged.clear()
            playerMaxChanged.clear()
            experience.clear()
            blockedExperience = null
        }
    }
}