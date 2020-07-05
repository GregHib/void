import ItemSpawns.ItemSpawn
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.MapLoaded
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import rs.dusk.world.entity.item.Drop

val files: FileLoader by inject()
val bus: EventBus by inject()
val scheduler: Scheduler by inject()

data class ItemSpawn(val id: Int, val amount: Int = 1, val delay: Int = 100, val tile: Tile)

val spawns: MutableMap<Region, MutableList<ItemSpawn>> = mutableMapOf()
val links = mutableMapOf<FloorItem, ItemSpawn>()

fun ItemSpawn.drop() = bus.emit(Drop(id, amount, tile, 0))

Startup then {
    val items: Array<ItemSpawn> = files.load(getProperty("floorItemsPath"))
    items.forEach { spawn ->
        val list = spawns.getOrPut(spawn.tile.region) { mutableListOf() }
        list.add(spawn)
    }
}

// We can do it this way as events and scheduler are guaranteed to be sequential.
var spawnPoint: ItemSpawn? = null

/**
 * When a region is loaded spawn [FloorItem]'s and link to their [ItemSpawn]'s
 */
MapLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { spawn ->
        spawnPoint = spawn
        spawn.drop()
    }
    spawnPoint = null
}

Registered where { entity is FloorItem && spawnPoint != null } then {
    val item = entity as FloorItem
    links[item] = spawnPoint!!
}

/**
 * When a spawn points item is removed, wait for respawn delay before spawning another.
 */
Unregistered where { entity is FloorItem && links.containsKey(entity) } then {
    val item = entity as FloorItem
    val spawn = links.remove(item)!!
    scheduler.add {
        delay(ticks = spawn.delay)
        spawnPoint = spawn
        spawn.drop()
        spawnPoint = null
    }
}