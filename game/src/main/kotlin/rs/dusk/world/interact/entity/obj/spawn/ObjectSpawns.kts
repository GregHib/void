import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.GameObjectFactory
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.RegionLoaded
import rs.dusk.engine.tick.Startup
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.obj.spawn.SpawnObject

val files: FileLoader by inject()
val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val factory: GameObjectFactory by inject()
val logger = InlineLogger()

val spawns: MutableMap<Region, MutableList<GameObject>> = mutableMapOf()

Startup then {
    val gameObjects: Array<GameObject> = files.load(getProperty("objectsPath"))
    gameObjects.forEach { gameObject ->
        val list = spawns.getOrPut(gameObject.tile.region) { mutableListOf() }
        list.add(gameObject)
    }
}

RegionLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { gameObject ->
        objects.add(gameObject)
        bus.emit(Registered(gameObject))
    }
}

/**
 * Spawns an object, optionally removing after a set time
 */
SpawnObject then {
    val gameObject = factory.spawn(id, tile, type, rotation, owner)
    spawnCustom(gameObject)
    // Revert
    if (ticks >= 0) {
        objects.setTimer(gameObject, scheduler.add {
            try {
                delay(ticks)
            } finally {
                despawn(gameObject)
            }
        })
    }
}

fun despawn(gameObject: GameObject) {
    batcher.update(
        gameObject.tile.chunk,
        ObjectRemoveMessage(gameObject.tile.offset(), gameObject.type, gameObject.rotation)
    )
    objects.removeTemp(gameObject)
    bus.emit(Unregistered(gameObject))
}

fun spawnCustom(gameObject: GameObject) {
    if (gameObject.id == -1) {
        val removal =
            objects[gameObject.tile].firstOrNull { it.tile == gameObject.tile && it.type == gameObject.type && it.rotation == gameObject.rotation }
        if(removal == null) {
            logger.debug { "Cannot find object to despawn $gameObject" }
        } else {
            despawn(removal)
        }
    } else {
        batcher.update(
            gameObject.tile.chunk,
            ObjectAddMessage(gameObject.tile.offset(), gameObject.id, gameObject.type, gameObject.rotation)
        )
        objects.addTemp(gameObject)
        bus.emit(Registered(gameObject))
    }
}