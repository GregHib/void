package content.entity.item.spawn

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.entity.floorItemDespawn
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.item.floor.ItemSpawns
import world.gregs.voidps.engine.entity.item.floor.loadItemSpawns
import world.gregs.voidps.engine.inject

val items: FloorItems by inject()
val spawns: ItemSpawns by inject()
val definitions: ItemDefinitions by inject()

adminCommand("reload") {
    if (content == "item defs" || content == "items" || content == "floor items") {
        val files = configFiles()
        items.clear()
        definitions.load(files.getOrDefault(Settings["definitions.items"], emptyList()))
        loadItemSpawns(items, spawns, files.getOrDefault(Settings["spawns.items"], emptyList()))
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