package world.gregs.voidps.engine.entity

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface Spawn {
    fun playerSpawn(handler: Player.() -> Unit) {
        playerSpawns.add(handler)
    }

    fun npcSpawn(id: String = "*", handler: NPC.() -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { key ->
            npcSpawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun objectSpawn(id: String = "*", handler: GameObject.() -> Unit) {
        Wildcards.find(id, Wildcard.Object) { key ->
            objectSpawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun floorItemSpawn(id: String = "*", handler: FloorItem.() -> Unit) {
        Wildcards.find(id, Wildcard.Item) { key ->
            floorItemSpawns.getOrPut(key) { mutableListOf() }.add(handler)
        }
    }

    fun worldSpawn(handler: (ConfigFiles) -> Unit) {
        worldSpawns.add(handler)
    }

    companion object : AutoCloseable {
        private val playerSpawns = ObjectArrayList<(Player) -> Unit>(100)
        private val npcSpawns = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Unit>>(250)
        private val objectSpawns = Object2ObjectOpenHashMap<String, MutableList<(GameObject) -> Unit>>(2)
        private val floorItemSpawns = Object2ObjectOpenHashMap<String, MutableList<(FloorItem) -> Unit>>(2)
        private val worldSpawns = ObjectArrayList<(ConfigFiles) -> Unit>(25)

        fun player(player: Player) {
            for (handler in playerSpawns) {
                handler(player)
            }
        }

        fun npc(npc: NPC) {
            for (handler in npcSpawns["*"] ?: emptyList()) {
                handler(npc)
            }
            for (handler in npcSpawns[npc.id] ?: return) {
                handler(npc)
            }
        }

        fun gameObject(gameObject: GameObject) {
            for (handler in objectSpawns["*"] ?: emptyList()) {
                handler(gameObject)
            }
            for (handler in objectSpawns[gameObject.id] ?: return) {
                handler(gameObject)
            }
        }

        fun floorItem(floorItem: FloorItem) {
            for (handler in floorItemSpawns["*"] ?: emptyList()) {
                handler(floorItem)
            }
            for (handler in floorItemSpawns[floorItem.id] ?: return) {
                handler(floorItem)
            }
        }

        fun world(configFiles: ConfigFiles) {
            for (handler in worldSpawns) {
                handler(configFiles)
            }
        }

        override fun close() {
            playerSpawns.clear()
            npcSpawns.clear()
            objectSpawns.clear()
            floorItemSpawns.clear()
            worldSpawns.clear()
        }
    }
}
