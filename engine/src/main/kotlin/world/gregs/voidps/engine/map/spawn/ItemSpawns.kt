package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class ItemSpawns(
    private val items: FloorItems
) {
    private lateinit var spawns: Map<Region, List<ItemSpawn>>
    private val loaded = mutableSetOf<Region>()
    private val respawns = mutableMapOf<Entity, EventHandler>()

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
        respawns[item] = item.events.on<FloorItem, Unregistered> {
            delay(spawn.delay) {
                drop(spawn)
            }
        }
    }

    fun clear() {
        respawns.forEach { (entity, handler) ->
            entity.events.remove(handler)
        }
        respawns.clear()
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