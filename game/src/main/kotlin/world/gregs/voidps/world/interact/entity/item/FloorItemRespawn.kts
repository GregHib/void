package world.gregs.voidps.world.interact.entity.item

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.floorItemDespawn
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.ItemSpawns
import world.gregs.voidps.world.interact.world.spawn.loadItemSpawns

val items: FloorItems by inject()
val spawns: ItemSpawns by inject()
val definitions: ItemDefinitions by inject()

worldSpawn {
    loadItemSpawns(items, spawns)
}

adminCommand("reload") {
    if (content == "item defs" || content == "items" || content == "floor items") {
        items.clear()
        definitions.load()
        loadItemSpawns(items, spawns)
    }
}

floorItemDespawn { floorItem ->
    if (isSpawnItem(floorItem)) {
        val spawn = spawns.get(floorItem.tile) ?: return@floorItemDespawn
        items.add(floorItem.tile, spawn.id, spawn.amount, revealTicks = spawn.delay, owner = "")
    }
}

fun isSpawnItem(item: FloorItem): Boolean {
    val spawn = spawns.get(item.tile) ?: return false
    return item.id == spawn.id && item.amount == spawn.amount && item.owner == null
}