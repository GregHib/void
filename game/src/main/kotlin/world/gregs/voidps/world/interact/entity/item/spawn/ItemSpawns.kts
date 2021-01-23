import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLoaded
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.item.spawn.Drop

val files: FileLoader by inject()
val bus: EventBus by inject()
val scheduler: Scheduler by inject()

data class ItemSpawn(val id: Int, val amount: Int = 1, val delay: Int = 100, val tile: Tile)

val spawns: MutableMap<Region, MutableList<ItemSpawn>> = mutableMapOf()
val links = mutableMapOf<FloorItem, ItemSpawn>()

Startup then {
    val items: Array<ItemSpawn> = files.load(getProperty("floorItemsPath"))
    items.forEach { spawn ->
        val list = spawns.getOrPut(spawn.tile.region) { mutableListOf() }
        list.add(spawn)
    }
}

/**
 * Spawns a immediately visible floor item and link it to it's spawn point
 */
fun ItemSpawn.drop() {
    val floorItem = bus.emit(Drop(id, amount, tile, 0)) ?: return
    links[floorItem] = this
}


/**
 * When a region is loaded spawn [FloorItem]'s
 */
RegionLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { spawn ->
        spawn.drop()
    }
}

/**
 * When a spawn points item is removed, wait for respawn delay before spawning another.
 */
Unregistered where { entity is FloorItem && links.containsKey(entity as FloorItem) } then {
    val item = entity as FloorItem
    val spawn = links.remove(item)!!
    scheduler.add {
        delay(ticks = spawn.delay)
        spawn.drop()
    }
}