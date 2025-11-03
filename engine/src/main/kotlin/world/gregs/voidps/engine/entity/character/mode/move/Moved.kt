package world.gregs.voidps.engine.entity.character.mode.move

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
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
        val playerMoved = ObjectArrayList<(Player, Tile) -> Unit>(15)
        val npcMoved = Object2ObjectOpenHashMap<String, MutableList<(NPC, Tile) -> Unit>>(10)

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