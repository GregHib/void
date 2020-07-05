import kotlinx.coroutines.Job
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Startup
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
val timers = mutableMapOf<ItemSpawn, Job?>()

fun ItemSpawn.drop() = bus.emit(Drop(id, amount, tile, 0))

Startup then {
    val items: Array<ItemSpawn> = files.load(getProperty("floorItemsPath"))
    items.forEach { spawn ->
        val list = spawns.getOrPut(spawn.tile.region) { mutableListOf() }
        list.add(spawn)
    }
}

MapLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { spawn ->
        spawn.drop()
        // TODO better way of doing this.
        on<Unregistered> {
            where { entity is FloorItem && entity.id == spawn.id && entity.amount == spawn.amount && entity.tile == spawn.tile }
            then {
                timers[spawn]?.cancel()
                timers[spawn] = scheduler.add {
                    delay(ticks = spawn.delay)
                    spawn.drop()
                }
            }
        }
    }
}