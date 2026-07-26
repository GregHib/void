package content.entity.item.spawn

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawn
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.type.Tile

class FloorItemRespawn(val spawns: ItemSpawns) : Script {

    init {
        floorItemDespawn {
            if (isSpawnItem(this)) {
                val spawn = spawns.get(tile) ?: return@floorItemDespawn
                val tile = tile
                // TODO use lifecycle
                World.queue("respawn_item_${tile.id}") {
                    if (!spawnItemExists(tile, spawn)) {
                        FloorItems.add(tile, spawn.id, spawn.amount, revealTicks = spawn.delay, owner = "")
                    }
                }
            }
        }
    }

    fun isSpawnItem(item: FloorItem): Boolean {
        val spawn = spawns.get(item.tile) ?: return false
        return item.id == spawn.id && item.amount == spawn.amount && item.owner == null
    }

    fun spawnItemExists(tile: Tile, spawn: ItemSpawn): Boolean = FloorItems.at(tile).any {
        it.id == spawn.id && it.amount == spawn.amount && (it.owner == null || it.owner == "")
    }
}
