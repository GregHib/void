package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface Despawn {

    fun playerDespawn(block: Player.() -> Unit) {
        playerDespawns.add(block)
    }

    fun npcDespawn(id: String = "*", block: NPC.() -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { key ->
            npcDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun objectDespawn(id: String = "*", block: GameObject.() -> Unit) {
        Wildcards.find(id, Wildcard.Object) { key ->
            objectDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun floorItemDespawn(id: String = "*", block: FloorItem.() -> Unit) {
        Wildcards.find(id, Wildcard.Item) { key ->
            floorItemDespawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun worldDespawn(block: () -> Unit) {
        worldDespawns.add(block)
    }

    companion object : AutoCloseable {
        val playerDespawns = ObjectArrayList<(Player) -> Unit>(20)
        val npcDespawns = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Unit>>(30)
        val objectDespawns = Object2ObjectOpenHashMap<String, MutableList<(GameObject) -> Unit>>(10)
        val floorItemDespawns = Object2ObjectOpenHashMap<String, MutableList<(FloorItem) -> Unit>>(2)
        val worldDespawns = ObjectArrayList<() -> Unit>(2)

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

        override fun close() {
            playerDespawns.clear()
            npcDespawns.clear()
            objectDespawns.clear()
            floorItemDespawns.clear()
            worldDespawns.clear()
        }
    }
}
