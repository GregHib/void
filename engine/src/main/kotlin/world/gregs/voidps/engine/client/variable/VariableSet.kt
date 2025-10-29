package world.gregs.voidps.engine.client.variable

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcards

/**
 * Variable with name [key] was set to [to]
 * @param from previous value
 */
interface VariableSet {
    fun variableSet(player: Player, key: String, from: Any?, to: Any?) {}

    fun variableSet(key: String = "*", block: (player: Player, key: String, from: Any?, to: Any?) -> Unit) {
        for (match in Wildcards.find(key)) {
            playerBlocks.getOrPut(match) { mutableListOf() }.add(block)
        }
    }

    fun npcVariableSet(key: String = "*", id: String = "*", block: (npc: NPC, key: String, from: Any?, to: Any?) -> Unit) {
        for (keyMatch in Wildcards.find(key)) {
            for (idMatch in Wildcards.find(id)) {
                npcBlocks.getOrPut("$keyMatch:$idMatch") { mutableListOf() }.add(block)
            }
        }
    }

    companion object : VariableSet {
        val playerBlocks = mutableMapOf<String, MutableList<(Player, String, Any?, Any?) -> Unit>>()
        val npcBlocks = mutableMapOf<String, MutableList<(NPC, String, Any?, Any?) -> Unit>>()

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
