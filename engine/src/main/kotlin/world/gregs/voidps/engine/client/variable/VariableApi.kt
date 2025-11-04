package world.gregs.voidps.engine.client.variable

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface VariableApi {
    /**
     * Variable with name [key] was set to [to] [from] previous value
     */
    fun variableSet(key: String = "*", block: Player.(key: String, from: Any?, to: Any?) -> Unit) {
        Wildcards.find(key, Wildcard.Variables) { match ->
            playerBlocks.getOrPut(match) { mutableListOf() }.add(block)
        }
    }

    fun npcVariableSet(key: String = "*", id: String = "*", block: NPC.(key: String, from: Any?, to: Any?) -> Unit) {
        Wildcards.find(key, Wildcard.Variables) { keyMatch ->
            Wildcards.find(id, Wildcard.Npc) { idMatch ->
                npcBlocks.getOrPut("$keyMatch:$idMatch") { mutableListOf() }.add(block)
            }
        }
    }

    /**
     * Variable with name [key] had a sub-value [value] added to it
     */
    fun variableBitAdded(key: String, block: Player.(value: Any) -> Unit) {
        varbitsAdded[key] = block
    }

    /**
     * Variable with name [key] had a sub-value [value] removed from it
     */
    fun variableBitRemoved(key: String, block: Player.(value: Any) -> Unit) {
        varbitsRemoved[key] = block
    }


    companion object : AutoCloseable {
        val playerBlocks = Object2ObjectOpenHashMap<String, MutableList<(Player, String, Any?, Any?) -> Unit>>(500)
        val npcBlocks = Object2ObjectOpenHashMap<String, MutableList<(NPC, String, Any?, Any?) -> Unit>>(2)

        val varbitsAdded = Object2ObjectOpenHashMap<String, Player.(Any) -> Unit>(2)
        val varbitsRemoved = Object2ObjectOpenHashMap<String, Player.(Any) -> Unit>(2)

        fun add(player: Player, key: String, value: Any) {
            varbitsAdded[key]?.invoke(player, value)
        }

        fun remove(player: Player, key: String, value: Any) {
            varbitsRemoved[key]?.invoke(player, value)
        }

        fun set(player: Player, key: String, from: Any?, to: Any?) {
            for (block in playerBlocks[key] ?: emptyList()) {
                block(player, key, from, to)
            }
            for (block in playerBlocks["*"] ?: return) {
                block(player, key, from, to)
            }
        }

        fun set(npc: NPC, key: String, from: Any?, to: Any?) {
            for (block in npcBlocks["$key:${npc.id}"] ?: emptyList()) {
                block(npc, key, from, to)
            }
            for (block in npcBlocks["$key:*"] ?: emptyList()) {
                block(npc, key, from, to)
            }
            for (block in npcBlocks["*:${npc.id}"] ?: emptyList()) {
                block(npc, key, from, to)
            }
            for (block in npcBlocks["*:*"] ?: return) {
                block(npc, key, from, to)
            }
        }

        override fun close() {
            playerBlocks.clear()
            npcBlocks.clear()
        }
    }
}
