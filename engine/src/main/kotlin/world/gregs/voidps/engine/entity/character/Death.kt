package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.*

interface Death {

    data class OnDeath(
        var dropItems: Boolean = true,
        var teleport: Boolean = true,
    )

    fun playerDeath(handler: Player.(OnDeath) -> Unit) {
        playerHandlers.add(handler)
    }

    fun npcDeath(npc: String = "*", handler: NPC.() -> Unit) {
        Wildcards.find(npc, Wildcard.Npc) { id ->
            npcHandlers.getOrPut(id) { mutableListOf() }.add(handler)
        }
    }

    companion object : AutoCloseable {
        val playerHandlers = ObjectArrayList<Player.(OnDeath) -> Unit>(20)
        val npcHandlers = Object2ObjectOpenHashMap<String, MutableList<NPC.() -> Unit>>(20)

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

        override fun close() {
            playerHandlers.clear()
            npcHandlers.clear()
        }
    }
}
