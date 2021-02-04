import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLoaded
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.network.codec.game.encode.ObjectAddEncoder
import world.gregs.voidps.network.codec.game.encode.ObjectRemoveEncoder
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.obj.RemoveObject
import world.gregs.voidps.world.interact.entity.obj.spawn.SpawnObject
import world.gregs.voidps.world.interact.entity.obj.spawn.spawnObject

val files: FileLoader by inject()
val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val factory: GameObjectFactory by inject()
val logger = InlineLogger()
val addEncoder: ObjectAddEncoder by inject()
val removeEncoder: ObjectRemoveEncoder by inject()

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
        spawnObject(gameObject.id, gameObject.tile, gameObject.type, gameObject.rotation)
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

/**
 * Removes an object, optionally reverting after a set time
 */
RemoveObject then {
    despawn(gameObject)
    // Revert
    if (ticks >= 0) {
        objects.setTimer(gameObject, scheduler.add {
            try {
                delay(ticks)
            } finally {
                respawn(gameObject)
            }
        })
    }
}

fun despawn(gameObject: GameObject) {
    batcher.update(gameObject.tile.chunk) { player ->
        removeEncoder.encode(player, gameObject.tile.offset(), gameObject.type, gameObject.rotation)
    }
    objects.removeTemp(gameObject)
    bus.emit(Unregistered(gameObject))
}

fun spawnCustom(gameObject: GameObject) {
    if (gameObject.id == -1) {
        val removal = objects[gameObject.tile].firstOrNull { it.tile == gameObject.tile && it.type == gameObject.type && it.rotation == gameObject.rotation }
        if (removal == null) {
            logger.debug { "Cannot find object to despawn $gameObject" }
        } else {
            despawn(removal)
        }
    } else {
        respawn(gameObject)
    }
}

fun respawn(gameObject: GameObject) {
    batcher.update(gameObject.tile.chunk) { player ->
        addEncoder.encode(player, gameObject.tile.offset(), gameObject.id, gameObject.type, gameObject.rotation)
    }
    objects.addTemp(gameObject)
    bus.emit(Registered(gameObject))
}