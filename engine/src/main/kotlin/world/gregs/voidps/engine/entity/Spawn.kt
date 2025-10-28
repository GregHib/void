package world.gregs.voidps.engine.entity

import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.event.Wildcards

interface Spawn {
    fun playerSpawn(block: (Player) -> Unit) {
        playerSpawns.add(block)
    }

    fun npcSpawn(id: String = "*", block: (NPC) -> Unit) {
        for (key in Wildcards.find(id)) {
            npcSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun objectSpawn(id: String = "*", block: (GameObject) -> Unit) {
        for (key in Wildcards.find(id)) {
            objectSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun floorItemSpawn(id: String = "*", block: (FloorItem) -> Unit) {
        for (key in Wildcards.find(id)) {
            floorItemSpawns.getOrPut(key) { mutableListOf() }.add(block)
        }
    }

    fun worldSpawn(block: (ConfigFiles) -> Unit) {
        worldSpawns.add(block)
    }

    companion object {
        val playerSpawns = mutableListOf<(Player) -> Unit>()
        val npcSpawns = mutableMapOf<String, MutableList<(NPC) -> Unit>>("*" to mutableListOf())
        val objectSpawns = mutableMapOf<String, MutableList<(GameObject) -> Unit>>("*" to mutableListOf())
        val floorItemSpawns = mutableMapOf<String, MutableList<(FloorItem) -> Unit>>("*" to mutableListOf())
        val worldSpawns = mutableListOf<(ConfigFiles) -> Unit>()

        fun player(player: Player) {
            for (block in playerSpawns) {
                block(player)
            }
        }

        fun npc(npc: NPC) {
            for (block in npcSpawns["*"]!!) {
                block(npc)
            }
            for (block in npcSpawns[npc.id] ?: return) {
                block(npc)
            }
        }

        fun gameObject(gameObject: GameObject) {
            for (block in objectSpawns["*"]!!) {
                block(gameObject)
            }
            for (block in objectSpawns[gameObject.id] ?: return) {
                block(gameObject)
            }
        }

        fun floorItem(floorItem: FloorItem) {
            for (block in floorItemSpawns["*"]!!) {
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

        fun clear() {
            playerSpawns.clear()
            npcSpawns.clear()
            npcSpawns["*"] = mutableListOf()
            objectSpawns.clear()
            objectSpawns["*"] = mutableListOf()
            floorItemSpawns.clear()
            floorItemSpawns["*"] = mutableListOf()
            worldSpawns
        }
    }
}
