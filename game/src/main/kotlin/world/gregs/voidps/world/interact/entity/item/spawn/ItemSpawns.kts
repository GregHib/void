import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.FloorItem
import world.gregs.voidps.engine.entity.item.FloorItemFactory
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLoaded
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject

val files: FileLoader by inject()
val scheduler: Scheduler by inject()
val factory: FloorItemFactory by inject()

data class ItemSpawnBuilder(
    val id: Int,
    val amount: Int = 1,
    val delay: Int = 100,
    val tile: ItemTile,
) {
    data class ItemTile(val x: Int, val y: Int, val plane: Int = 0)

    fun build() = ItemSpawn(id, amount, delay, Tile(tile.x, tile.y, tile.plane))
}

@JsonDeserialize(builder = ItemSpawnBuilder::class)
data class ItemSpawn(val id: Int, val amount: Int = 1, val delay: Int = 100, val tile: Tile)

val spawns: MutableMap<Region, MutableList<ItemSpawn>> = mutableMapOf()
val links = mutableMapOf<FloorItem, ItemSpawn>()

on<World, Startup> {
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
    val floorItem = factory.add(id, amount, tile, 0) ?: return
    links[floorItem] = this
}


/**
 * When a region is loaded spawn [FloorItem]'s
 */
on<World, RegionLoaded> {
    spawns[region]?.forEach { spawn ->
        spawn.drop()
    }
}

/**
 * When a spawn points item is removed, wait for respawn delay before spawning another.
 */
on<Unregistered>({ links.containsKey(it) }) { item: FloorItem ->
    val spawn = links.remove(item)!!
    scheduler.launch {
        delay(ticks = spawn.delay)
        spawn.drop()
    }
}