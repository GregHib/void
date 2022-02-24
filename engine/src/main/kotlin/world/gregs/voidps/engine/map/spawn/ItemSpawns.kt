package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

fun loadItemSpawns(items: FloorItems, storage: FileStorage = get(), path: String = getProperty("itemSpawnsPath")) {
    timedLoad("item spawn") {
        val data: List<Map<String, Any>> = storage.load(path)
        val areas = data.map { ItemSpawn.fromMap(it) }
        val membersWorld = World["members", false]
        for (spawn in areas) {
            if (!membersWorld && spawn.members) {
                continue
            }
            val item = items.add(spawn.id, spawn.amount, spawn.tile, revealTicks = 0)
            item["respawn"] = spawn
        }
        areas.size
    }
}