package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.type.Tile

/**
 * Entity moved between [from] and their current tile
 */
interface Moved {
    fun moved(block: Player.(from: Tile) -> Unit) {
        playerMoved.add(block)
    }

    fun npcMoved(id: String = "*", block: NPC.(from: Tile) -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { match ->
            npcMoved.getOrPut(match) { mutableListOf() }.add(block)
        }
    }

    companion object {
        val playerMoved = mutableListOf<(Player, Tile) -> Unit>()
        val npcMoved = mutableMapOf<String, MutableList<(NPC, Tile) -> Unit>>()

        fun player(player: Player, from: Tile) {
            for (block in playerMoved) {
                block(player, from)
            }
        }

        fun npc(npc: NPC, from: Tile) {
            for (block in npcMoved[npc.id] ?: emptyList()) {
                block(npc, from)
            }
            for (block in npcMoved["*"] ?: return) {
                block(npc, from)
            }
        }

        fun clear() {
            playerMoved.clear()
            npcMoved.clear()
        }

    }
}