package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.type.Tile

interface Death {

    data class OnDeath(
        var dropItems: Boolean = true,
        var teleport: Tile? = null,
    )

    fun playerDeath(handler: Player.(OnDeath) -> Unit) {
        Script.checkLoading()
        playerHandlers.add(handler)
    }

    fun npcDeath(npc: String = "*", handler: NPC.() -> Unit) {
        Script.checkLoading()
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcHandlers.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    fun npcCanDie(npc: String = "*", handler: NPC.() -> Boolean) {
        Script.checkLoading()
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcDeathHandlers.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    companion object : AutoCloseable {
        private val playerHandlers = ObjectArrayList<Player.(OnDeath) -> Unit>(20)
        private val npcHandlers = Object2ObjectOpenHashMap<String, MutableList<NPC.() -> Unit>>(20)
        private val npcDeathHandlers = Object2ObjectOpenHashMap<String, MutableList<NPC.() -> Boolean>>(20)

        fun killed(player: Player): OnDeath {
            val onDeath = OnDeath()
            for (handler in playerHandlers) {
                handler.invoke(player, onDeath)
            }
            return onDeath
        }

        fun killed(npc: NPC) {
            for (handler in npcHandlers[npc.id] ?: emptyList()) {
                handler.invoke(npc)
            }
            for (handler in npcHandlers["*"] ?: emptyList()) {
                handler.invoke(npc)
            }
        }

        fun canDie(npc: NPC): Boolean {
            for (handler in npcDeathHandlers[npc.id] ?: emptyList()) {
                if (!handler.invoke(npc)) {
                    return false
                }
            }
            for (handler in npcDeathHandlers["*"] ?: emptyList()) {
                if (!handler.invoke(npc)) {
                    return false
                }
            }
            return true
        }

        override fun close() {
            playerHandlers.clear()
            npcHandlers.clear()
        }
    }
}
