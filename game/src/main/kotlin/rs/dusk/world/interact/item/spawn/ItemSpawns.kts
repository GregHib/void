import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.model.map.region.Region
import rs.dusk.engine.model.map.region.RegionLoaded
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import rs.dusk.world.interact.item.spawn.Drop

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