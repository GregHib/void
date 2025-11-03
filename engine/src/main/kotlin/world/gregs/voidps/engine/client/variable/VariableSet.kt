package world.gregs.voidps.engine.client.variable

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
interface VariableSet {
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

    companion object : VariableSet {
        val playerBlocks = Object2ObjectOpenHashMap<String, MutableList<(Player, String, Any?, Any?) -> Unit>>(500)
        val npcBlocks = Object2ObjectOpenHashMap<String, MutableList<(NPC, String, Any?, Any?) -> Unit>>(2)

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

        fun clear() {
            playerBlocks.clear()
            npcBlocks.clear()
        }
    }
}
