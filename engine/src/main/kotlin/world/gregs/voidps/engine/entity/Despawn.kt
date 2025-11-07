package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface Despawn {

    fun playerDespawn(handler: Player.() -> Unit) {
        playerDespawns.add(handler)
    }

    fun npcDespawn(id: String = "*", handler: NPC.() -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { key ->
            npcDespawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun objectDespawn(id: String = "*", handler: GameObject.() -> Unit) {
        Wildcards.find(id, Wildcard.Object) { key ->
            objectDespawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun floorItemDespawn(id: String = "*", handler: FloorItem.() -> Unit) {
        Wildcards.find(id, Wildcard.Item) { key ->
            floorItemDespawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun worldDespawn(handler: () -> Unit) {
        worldDespawns.add(handler)
    }

    companion object : AutoCloseable {
        private val playerDespawns = ObjectArrayList<(Player) -> Unit>(20)
        private val npcDespawns = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Unit>>(30)
        private val objectDespawns = Object2ObjectOpenHashMap<String, MutableList<(GameObject) -> Unit>>(10)
        private val floorItemDespawns = Object2ObjectOpenHashMap<String, MutableList<(FloorItem) -> Unit>>(2)
        private val worldDespawns = ObjectArrayList<() -> Unit>(2)

        fun player(player: Player) {
            for (handler in playerDespawns) {
                handler(player)
            }
        }

        fun npc(npc: NPC) {
            for (handler in npcDespawns["*"] ?: emptyList()) {
                handler(npc)
            }
            for (handler in npcDespawns[npc.id] ?: return) {
                handler(npc)
            }
        }

        fun gameObject(gameObject: GameObject) {
            for (handler in objectDespawns["*"] ?: emptyList()) {
                handler(gameObject)
            }
            for (handler in objectDespawns[gameObject.id] ?: return) {
                handler(gameObject)
            }
        }

        fun floorItem(floorItem: FloorItem) {
            for (handler in floorItemDespawns["*"] ?: emptyList()) {
                handler(floorItem)
            }
            for (handler in floorItemDespawns[floorItem.id] ?: return) {
                handler(floorItem)
            }
        }

        fun world() {
            for (handler in worldDespawns) {
                handler()
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
