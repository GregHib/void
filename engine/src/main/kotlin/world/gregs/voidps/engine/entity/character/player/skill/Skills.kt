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

    fun levelChanged(skill: Skill? = null, handler: Player.(skill: Skill, from: Int, to: Int) -> Unit) {
        playerChanged.getOrPut(skill) { mutableListOf() }.add(handler)
    }

    fun npcLevelChanged(skill: Skill, id: String = "*", handler: NPC.(skill: Skill, from: Int, to: Int) -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { match ->
            npcChanged.getOrPut("$match:${skill.name}") { mutableListOf() }.add(handler)
        }
    }

    fun maxLevelChanged(skill: Skill? = null, handler: Player.(skill: Skill, from: Int, to: Int) -> Unit) {
        playerMaxChanged.getOrPut(skill) { mutableListOf() }.add(handler)
    }

    fun experience(handler: Player.(skill: Skill, from: Double, to: Double) -> Unit) {
        experience.add(handler)
    }

    fun blockedExperience(handler: Player.(skill: Skill, exp: Double) -> Unit) {
        blockedExperience = handler
    }

    companion object : AutoCloseable {
        private val playerChanged = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>(30)
        private val npcChanged = Object2ObjectOpenHashMap<String, MutableList<(NPC, Skill, from: Int, to: Int) -> Unit>>(15)

        private val playerMaxChanged = Object2ObjectOpenHashMap<Skill?, MutableList<(Player, Skill, from: Int, to: Int) -> Unit>>(15)

        private val experience = ObjectArrayList<(Player, Skill, Double, Double) -> Unit>(5)
        private var blockedExperience: (Player.(Skill, Double) -> Unit)? = null

        fun exp(player: Player, skill: Skill, from: Double, to: Double) {
            for (handler in experience) {
                handler.invoke(player, skill, from, to)
            }
        }

        fun blocked(player: Player, skill: Skill, exp: Double) {
            blockedExperience?.invoke(player, skill, exp)
        }

        fun changed(player: Player, skill: Skill, from: Int, to: Int) {
            for (handler in playerChanged[skill] ?: emptyList()) {
                handler(player, skill, from, to)
            }
            for (handler in playerChanged[null] ?: return) {
                handler(player, skill, from, to)
            }
        }

        fun changed(npc: NPC, skill: Skill, from: Int, to: Int) {
            for (handler in npcChanged["${npc.id}:${skill.name}"] ?: emptyList()) {
                handler(npc, skill, from, to)
            }
            for (handler in npcChanged["*:${skill.name}"] ?: emptyList()) {
                handler(npc, skill, from, to)
            }
        }

        fun maxChanged(player: Player, skill: Skill, from: Int, to: Int) {
            for (handler in playerMaxChanged[skill] ?: emptyList()) {
                handler(player, skill, from, to)
            }
            for (handler in playerMaxChanged[null] ?: return) {
                handler(player, skill, from, to)
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