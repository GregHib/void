package world.gregs.voidps.engine.entity.character.mode.move

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile

interface Moved {
    /**
     * Entity moved starting at from and ending at their current tile
     */
    fun moved(block: Player.(from: Tile) -> Unit) {
        playerMoved.add(block)
    }

    fun entered(area: String, block: Player.(area: Area) -> Unit) {
        entered.getOrPut(area) { mutableListOf() }.add(block)
    }

    fun exited(area: String, block: Player.(area: Area) -> Unit) {
        exited.getOrPut(area) { mutableListOf() }.add(block)
    }

    fun npcMoved(id: String = "*", block: NPC.(from: Tile) -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { match ->
            npcMoved.getOrPut(match) { mutableListOf() }.add(block)
        }
    }

    companion object {
        val entered = Object2ObjectOpenHashMap<String, MutableList<Player.(Area) -> Unit>>(25)
        val exited = Object2ObjectOpenHashMap<String, MutableList<Player.(Area) -> Unit>>(25)
        val playerMoved = ObjectArrayList<(Player, Tile) -> Unit>(15)
        val npcMoved = Object2ObjectOpenHashMap<String, MutableList<(NPC, Tile) -> Unit>>(10)

        fun enter(player: Player, id: String, area: Area) {
            for (block in entered[id] ?: emptyList()) {
                block(player, area)
            }
            for (block in entered["*"] ?: return) {
                block(player, area)
            }
        }

        fun exit(player: Player, id: String, area: Area) {
            for (block in exited[id] ?: emptyList()) {
                block(player, area)
            }
            for (block in exited["*"] ?: return) {
                block(player, area)
            }
        }

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