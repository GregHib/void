import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.map.MapLoaded
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.SpawnObject

val files: FileLoader by inject()
val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val logger = InlineLogger()

val spawns: MutableMap<Region, MutableList<Location>> = mutableMapOf()

Startup then {
    val locations: Array<Location> = files.load(getProperty("objectsPath"))
    locations.forEach { location ->
        val list = spawns.getOrPut(location.tile.region) { mutableListOf() }
        list.add(location)
    }
}

MapLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { location ->
        spawn(location)
    }
}

/**
 * Spawns an object, optionally removing after a set time
 */
SpawnObject then {
    val location = Location(id, tile, type, rotation, owner)
    spawn(location)
    // Revert
    if (ticks >= 0) {
        objects.setTimer(location, scheduler.add {
            try {
                delay(ticks)
            } finally {
                despawn(location)
            }
        })
    }
}

fun despawn(location: Location) {
    batcher.update(
        location.tile.chunkPlane,
        ObjectRemoveMessage(location.tile.offset(), location.type, location.rotation)
    )
    objects.removeTemp(location)
    bus.emit(Unregistered(location))
}

fun spawn(location: Location) {
    if (location.id == -1) {
        val removal =
            objects[location.tile].firstOrNull { it.tile == location.tile && it.type == location.type && it.rotation == location.rotation }
        if(removal == null) {
            logger.debug { "Cannot find object to despawn $location" }
        } else {
            despawn(removal)
        }
    } else {
        batcher.update(
            location.tile.chunkPlane,
            ObjectAddMessage(location.tile.offset(), location.id, location.type, location.rotation)
        )
        objects.addTemp(location)
        bus.emit(Registered(location))
    }
}