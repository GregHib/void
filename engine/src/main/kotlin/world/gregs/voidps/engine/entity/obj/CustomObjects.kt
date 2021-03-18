package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.network.encode.addObject
import world.gregs.voidps.network.encode.removeObject
import world.gregs.voidps.utility.get

val customObjectModule = module {
    single(createdAtStart = true) {
        val files: FileLoader = get()
        val spawns: MutableMap<Region, MutableList<GameObject>> = mutableMapOf()
        val gameObjects: Array<GameObject> = files.load(getProperty("objectsPath"))
        gameObjects.forEach { gameObject ->
            val list = spawns.getOrPut(gameObject.tile.region) { mutableListOf() }
            list.add(gameObject)
        }
        CustomObjects(get(), get(), get(), get(), get(), spawns)
    }
}

class CustomObjects(
    private val objects: Objects,
    private val scheduler: Scheduler,
    private val bus: EventBus,
    private val batcher: ChunkBatcher,
    private val factory: GameObjectFactory,
    private val spawns: MutableMap<Region, MutableList<GameObject>>
) {

    private val logger = InlineLogger()

    init {
        batcher.addInitial { player, chunk, messages ->
            objects.getAdded(chunk)?.forEach {
                if (it.visible(player)) {
                    messages += { player ->
                        player.client?.addObject(it.tile.offset(), it.id, it.type, it.rotation)
                    }
                }
            }
            objects.getRemoved(chunk)?.forEach {
                if (it.visible(player)) {
                    messages += { player ->
                        player.client?.removeObject(it.tile.offset(), it.type, it.rotation)
                    }
                }
            }
        }
    }

    fun load(region: Region) {
        val spawns = spawns[region] ?: return
        spawns.forEach { gameObject ->
            spawnObject(gameObject.id, gameObject.tile, gameObject.type, gameObject.rotation)
        }
    }

    /**
     * Spawns an object, optionally removing after a set time
     */
    fun spawn(
        id: Int,
        tile: Tile,
        type: Int,
        rotation: Int,
        ticks: Int = -1,
        owner: String? = null
    ) {
        val gameObject = factory.spawn(id, tile, type, rotation, owner)
        spawnCustom(gameObject)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(gameObject, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    despawn(gameObject)
                }
            })
        }
    }

    private fun spawnCustom(gameObject: GameObject) {
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

    private fun despawn(gameObject: GameObject) {
        batcher.update(gameObject.tile.chunk) { player ->
            player.client?.removeObject(gameObject.tile.offset(), gameObject.type, gameObject.rotation)
        }
        objects.removeTemp(gameObject)
        bus.emit(Unregistered(gameObject))
    }

    private fun respawn(gameObject: GameObject) {
        batcher.update(gameObject.tile.chunk) { player ->
            player.client?.addObject(gameObject.tile.offset(), gameObject.id, gameObject.type, gameObject.rotation)
        }
        objects.addTemp(gameObject)
        bus.emit(Registered(gameObject))
    }

    /**
     * Removes an object, optionally reverting after a set time
     */
    fun remove(
        original: GameObject,
        ticks: Int = -1,
        owner: String? = null
    ) {
        despawn(original)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(original, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    respawn(original)
                }
            })
        }
    }

    /**
     * Replaces one object with another, optionally reverting after a set time
     */
    fun replace(
        original: GameObject,
        id: Int,
        tile: Tile,
        type: Int = 0,
        rotation: Int = 0,
        ticks: Int = -1,
        owner: String? = null
    ) {
        val replacement = factory.spawn(id, tile, type, rotation)

        switch(original, replacement)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(replacement, scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    switch(replacement, original)
                }
            })
        }
    }

    /**
     * Replaces two objects, linking them to the same job so both revert after timeout
     */
    fun replace(
        firstOriginal: GameObject,
        firstReplacement: Int,
        firstTile: Tile,
        firstRotation: Int,
        secondOriginal: GameObject,
        secondReplacement: Int,
        secondTile: Tile,
        secondRotation: Int,
        ticks: Int,
        owner: String? = null
    ) {
        val firstReplacement = factory.spawn(firstReplacement, firstTile, firstOriginal.type, firstRotation)
        val secondReplacement = factory.spawn(secondReplacement, secondTile, secondOriginal.type, secondRotation)
        switch(firstOriginal, firstReplacement)
        switch(secondOriginal, secondReplacement)
        // Revert
        if (ticks >= 0) {
            val job = scheduler.launch {
                try {
                    delay(ticks)
                } finally {
                    switch(firstReplacement, firstOriginal)
                    switch(secondReplacement, secondOriginal)
                }
            }
            objects.setTimer(firstReplacement, job)
            objects.setTimer(secondReplacement, job)
        }

    }

    private fun switch(original: GameObject, replacement: GameObject) {
        if (original.tile != replacement.tile) {
            batcher.update(original.tile.chunk) { player ->
                player.client?.removeObject(original.tile.offset(), original.type, original.rotation)
            }
        }
        batcher.update(replacement.tile.chunk) { player ->
            player.client?.addObject(replacement.tile.offset(), replacement.id, replacement.type, replacement.rotation)
        }
        if (original.tile != replacement.tile) {
            objects.removeTemp(original)
        } else {
            objects.removeAddition(original)
        }
        objects.addTemp(replacement)
        bus.emit(Unregistered(original))
        bus.emit(Registered(replacement))
    }
}

/**
 * Removes an existing map [GameObject].
 * The removal can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to removed just for one player.
 */
fun GameObject.remove(ticks: Int = -1, owner: String? = null) {
    get<CustomObjects>().remove(this, ticks, owner)
}

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 */
fun GameObject.replace(id: Int, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null) {
    get<CustomObjects>().replace(this, id, tile, type, rotation, ticks, owner)
}

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replaced just for one player.
 */
fun replaceObjectPair(
    firstOriginal: GameObject,
    firstReplacement: Int,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: GameObject,
    secondReplacement: Int,
    secondTile: Tile,
    secondRotation: Int,
    ticks: Int,
    owner: String? = null
) = get<CustomObjects>().replace(
    firstOriginal,
    firstReplacement,
    firstTile,
    firstRotation,
    secondOriginal,
    secondReplacement,
    secondTile,
    secondRotation,
    ticks,
    owner
)

/**
 * Spawns a temporary object with [id] [tile] [type] and [rotation] provided.
 * Can be removed after [ticks] or -1 for permanent (until server restarts or removed)
 */
fun spawnObject(
    id: Int,
    tile: Tile,
    type: Int,
    rotation: Int,
    ticks: Int = -1,
    owner: String? = null
) = get<CustomObjects>().spawn(
    id,
    tile,
    type,
    rotation,
    ticks,
    owner
)