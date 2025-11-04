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
    fun playerSpawn(block: Player.() -> Unit) {
        playerSpawns.add(block)
    }

    fun npcSpawn(id: String = "*", block: NPC.() -> Unit) {
        Wildcards.find(id, Wildcard.Npc) { key ->
            npcSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun objectSpawn(id: String = "*", block: GameObject.() -> Unit) {
        Wildcards.find(id, Wildcard.Object) { key ->
            objectSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun floorItemSpawn(id: String = "*", block: FloorItem.() -> Unit) {
        Wildcards.find(id, Wildcard.Item) { key ->
            floorItemSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun worldSpawn(block: (ConfigFiles) -> Unit) {
        worldSpawns.add(block)
    }

    companion object : AutoCloseable {
        val playerSpawns = ObjectArrayList<(Player) -> Unit>(100)
        val npcSpawns = Object2ObjectOpenHashMap<String, MutableList<(NPC) -> Unit>>(250)
        val objectSpawns = Object2ObjectOpenHashMap<String, MutableList<(GameObject) -> Unit>>(2)
        val floorItemSpawns = Object2ObjectOpenHashMap<String, MutableList<(FloorItem) -> Unit>>(2)
        val worldSpawns = ObjectArrayList<(ConfigFiles) -> Unit>(25)

        fun player(player: Player) {
            for (block in playerSpawns) {
                block(player)
            }
        }

        fun npc(npc: NPC) {
            for (block in npcSpawns["*"] ?: emptyList()) {
                block(npc)
            }
            for (block in npcSpawns[npc.id] ?: return) {
                block(npc)
            }
        }

        fun gameObject(gameObject: GameObject) {
            for (block in objectSpawns["*"] ?: emptyList()) {
                block(gameObject)
            }
            for (block in objectSpawns[gameObject.id] ?: return) {
                block(gameObject)
            }
        }

        fun floorItem(floorItem: FloorItem) {
            for (block in floorItemSpawns["*"] ?: emptyList()) {
                block(floorItem)
            }
            for (block in floorItemSpawns[floorItem.id] ?: return) {
                block(floorItem)
            }
        }

        fun world(configFiles: ConfigFiles) {
            for (block in worldSpawns) {
                block(configFiles)
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
