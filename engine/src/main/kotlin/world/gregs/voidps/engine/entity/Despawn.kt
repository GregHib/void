package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Wildcards

interface Despawn {

    fun playerDespawn(block: Player.() -> Unit) {
        playerDespawns.add(block)
    }

    fun npcDespawn(id: String = "*", block: NPC.() -> Unit) {
        for (key in Wildcards.find(id)) {
            npcDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun objectDespawn(id: String = "*", block: GameObject.() -> Unit) {
        for (key in Wildcards.find(id)) {
            objectDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun floorItemDespawn(id: String = "*", block: FloorItem.() -> Unit) {
        for (key in Wildcards.find(id)) {
            floorItemDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun worldDespawn(block: () -> Unit) {
        worldDespawns.add(block)
    }

    companion object {
        val playerDespawns = mutableListOf<(Player) -> Unit>()
        val npcDespawns = mutableMapOf<String, MutableList<(NPC) -> Unit>>()
        val objectDespawns = mutableMapOf<String, MutableList<(GameObject) -> Unit>>()
        val floorItemDespawns = mutableMapOf<String, MutableList<(FloorItem) -> Unit>>()
        val worldDespawns = mutableListOf<() -> Unit>()

        fun player(player: Player) {
            for (block in playerDespawns) {
                block(player)
            }
        }

        fun npc(npc: NPC) {
            for (block in npcDespawns["*"] ?: emptyList()) {
                block(npc)
            }
            for (block in npcDespawns[npc.id] ?: return) {
                block(npc)
            }
        }

        fun gameObject(gameObject: GameObject) {
            for (block in objectDespawns["*"] ?: emptyList()) {
                block(gameObject)
            }
            for (block in objectDespawns[gameObject.id] ?: return) {
                block(gameObject)
            }
        }

        fun floorItem(floorItem: FloorItem) {
            for (block in floorItemDespawns["*"] ?: emptyList()) {
                block(floorItem)
            }
            for (block in floorItemDespawns[floorItem.id] ?: return) {
                block(floorItem)
            }
        }

        fun world() {
            for (block in worldDespawns) {
                block()
            }
        }

        fun clear() {
            playerDespawns.clear()
            npcDespawns.clear()
            objectDespawns.clear()
            floorItemDespawns.clear()
            worldDespawns.clear()
        }
    }
}
