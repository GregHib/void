package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ItemSpawns(
    private val items: FloorItems
) {
    private lateinit var spawns: Map<Region, List<ItemSpawn>>
    private val loaded = mutableSetOf<Region>()

    init {
        on<FloorItem, Unregistered>({ it.contains("respawn") }) { floorItem ->
            val spawn: ItemSpawn = floorItem["respawn"]
            drop(spawn)
        }
    }

    fun load(region: Region) {
        val spawns = spawns[region] ?: return
        if (loaded.contains(region)) {
            return
        }
        loaded.add(region)
        for (spawn in spawns) {
            drop(spawn)
        }
    }

    private fun drop(spawn: ItemSpawn) {
        val item = items.add(spawn.id, spawn.amount, spawn.tile, revealTicks = 0)
        item["respawn"] = spawn
    }

    fun clear() {
        items.clear()
        loaded.clear()
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("itemSpawnsPath")): ItemSpawns {
        timedLoad("item spawn") {
            val data: List<Map<String, Any>> = storage.load(path)
            val areas = data.map { ItemSpawn.fromMap(it) }
            this.spawns = areas.groupBy { it.tile.region }
            areas.size
        }
        return this
    }
}