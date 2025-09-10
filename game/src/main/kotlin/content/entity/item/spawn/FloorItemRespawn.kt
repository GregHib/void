package content.entity.item.spawn

import world.gregs.voidps.engine.entity.floorItemDespawn
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class FloorItemRespawn {

    val items: FloorItems by inject()
    val spawns: ItemSpawns by inject()

    init {
        floorItemDespawn { floorItem ->
            if (isSpawnItem(floorItem)) {
                val spawn = spawns.get(floorItem.tile) ?: return@floorItemDespawn
                items.add(floorItem.tile, spawn.id, spawn.amount, revealTicks = spawn.delay, owner = "")
            }
        }
    }

    fun isSpawnItem(item: FloorItem): Boolean {
        val spawn = spawns.get(item.tile) ?: return false
        return item.id == spawn.id && item.amount == spawn.amount && item.owner == null
    }
}
