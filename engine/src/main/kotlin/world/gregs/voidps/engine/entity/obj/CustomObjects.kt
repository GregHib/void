package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.addObject
import world.gregs.voidps.engine.map.chunk.removeObject
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import world.gregs.voidps.network.chunk.ChunkUpdate

val customObjectModule = module {
    single(createdAtStart = true) {
        CustomObjects(get(), get(), get(), get(), get())
    }
}

class CustomObjects(
    private val objects: Objects,
    private val scheduler: Scheduler,
    private val batches: ChunkBatches,
    private val factory: GameObjectFactory,
    private val collision: GameObjectCollision,
) {
    private val logger = InlineLogger()
    lateinit var spawns: Map<Region, List<GameObject>>

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
        id: String,
        tile: Tile,
        type: Int,
        rotation: Int,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ): GameObject {
        val gameObject = factory.spawn(id, tile, type, rotation, owner)
        spawnCustom(gameObject, collision)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(gameObject, scheduler.add(ticks) {
                despawn(gameObject, collision)
            })
        }
        return gameObject
    }

    private fun spawnCustom(gameObject: GameObject, collision: Boolean) {
        if (gameObject.id.isEmpty()) {
            val removal = objects[gameObject.tile].firstOrNull { it.tile == gameObject.tile && it.type == gameObject.type && it.rotation == gameObject.rotation }
            if (removal == null) {
                logger.debug { "Cannot find object to despawn $gameObject" }
            } else {
                despawn(removal, collision)
            }
        } else {
            val update = addObject(gameObject)
            batches.update(gameObject.tile.chunk, update)
            add(gameObject, update)
        }
    }

    private fun despawn(gameObject: GameObject, updateCollision: Boolean) {
        val update = removeObject(gameObject)
        batches.update(gameObject.tile.chunk, update)
        remove(gameObject, update)
        if (updateCollision) {
            collision.modifyCollision(gameObject, GameObjectCollision.REMOVE_MASK)
        }
        gameObject.events.emit(Unregistered)
    }

    private fun remove(gameObject: GameObject, update: ChunkUpdate) {
        gameObject.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(gameObject.tile.chunk, it)
        }
        if (objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject["update"] = update
        }
        objects.removeTemp(gameObject)
    }

    private fun respawn(gameObject: GameObject, updateCollision: Boolean) {
        val update = addObject(gameObject)
        batches.update(gameObject.tile.chunk, update)
        if (updateCollision) {
            collision.modifyCollision(gameObject, GameObjectCollision.ADD_MASK)
        }
        gameObject.events.emit(Registered)
    }

    private fun add(gameObject: GameObject, update: ChunkUpdate) {
        gameObject.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(gameObject.tile.chunk, it)
        }
        if (!objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject["update"] = update
        }
        objects.addTemp(gameObject)
    }

    /**
     * Removes an object, optionally reverting after a set time
     */
    fun remove(
        original: GameObject,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ) {
        despawn(original, collision)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(original, scheduler.add(ticks) {
                respawn(original, collision)
            })
        }
    }

    /**
     * Replaces one object with another, optionally reverting after a set time
     */
    fun replace(
        original: GameObject,
        id: String,
        tile: Tile,
        type: Int = 0,
        rotation: Int = 0,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ) {
        val replacement = factory.spawn(id, tile, type, rotation, owner)

        switch(original, replacement, collision)
        // Revert
        if (ticks >= 0) {
            objects.setTimer(replacement, scheduler.add(ticks) {
                switch(replacement, original, collision)
            })
        }
    }

    /**
     * Replaces two objects, linking them to the same job so both revert after timeout
     */
    fun replace(
        firstOriginal: GameObject,
        firstReplacement: String,
        firstTile: Tile,
        firstRotation: Int,
        secondOriginal: GameObject,
        secondReplacement: String,
        secondTile: Tile,
        secondRotation: Int,
        ticks: Int,
        firstOwner: String? = null,
        secondOwner: String? = null,
        collision: Boolean = true
    ) {
        val firstReplacement = factory.spawn(firstReplacement, firstTile, firstOriginal.type, firstRotation, firstOwner)
        val secondReplacement = factory.spawn(secondReplacement, secondTile, secondOriginal.type, secondRotation, secondOwner)
        switch(firstOriginal, firstReplacement, collision)
        switch(secondOriginal, secondReplacement, collision)
        // Revert
        if (ticks >= 0) {
            val job = scheduler.add(ticks) {
                switch(firstReplacement, firstOriginal, collision)
                switch(secondReplacement, secondOriginal, collision)
            }
            objects.setTimer(firstReplacement, job)
            objects.setTimer(secondReplacement, job)
        }

    }

    private fun switch(original: GameObject, replacement: GameObject, updateCollision: Boolean) {
        val removeUpdate = removeObject(original)
        if (original.tile != replacement.tile) {
            batches.update(original.tile.chunk, removeUpdate)
        }
        val addUpdate = addObject(replacement)
        batches.update(replacement.tile.chunk, addUpdate)
        remove(original, removeUpdate)
        add(replacement, addUpdate)
        if (updateCollision) {
            collision.modifyCollision(original, GameObjectCollision.REMOVE_MASK)
        }
        original.events.emit(Unregistered)
        if (updateCollision) {
            collision.modifyCollision(replacement, GameObjectCollision.ADD_MASK)
        }
        replacement.events.emit(Registered)
    }

    init {
        load()
    }

    fun load() = timedLoad("object spawn") {
        val data: Array<Map<String, Any>> = get<FileStorage>().load(getProperty("objectsPath"))
        val definition: ObjectDefinitions = get()
        val objects = data.map {
            val t = it["tile"] as Map<String, Any>
            GameObject(definition.get(it["id"] as Int).stringId, Tile(t["x"] as Int, t["y"] as Int), it["type"] as Int, it["rotation"] as Int)
        }
        this.spawns = objects.groupBy { obj -> obj.tile.region }
        data.size
    }
}

/**
 * Removes an existing map [GameObject].
 * The removal can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to removed just for one player.
 * [collision] can also be used to disable collision changes
 */
fun GameObject.remove(ticks: Int = -1, owner: String? = null, collision: Boolean = true) {
    get<CustomObjects>().remove(this, ticks, owner, collision)
}

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 * [collision] can also be used to disable collision changes
 */
fun GameObject.replace(id: String, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null, collision: Boolean = true) {
    get<CustomObjects>().replace(this, id, tile, type, rotation, ticks, owner, collision)
}

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replace just for one player.
 */
fun replaceObjectPair(
    firstOriginal: GameObject,
    firstReplacement: String,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: GameObject,
    secondReplacement: String,
    secondTile: Tile,
    secondRotation: Int,
    ticks: Int,
    owner: String? = null,
    collision: Boolean = true
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
    owner,
    owner,
    collision
)

/**
 * Spawns a temporary object with [id] [tile] [type] and [rotation] provided.
 * Can be removed after [ticks] or -1 for permanent (until server restarts or removed)
 */
fun spawnObject(
    id: String,
    tile: Tile,
    type: Int,
    rotation: Int,
    ticks: Int = -1,
    owner: String? = null,
    collision: Boolean = true
) = get<CustomObjects>().spawn(
    id,
    tile,
    type,
    rotation,
    ticks,
    owner,
    collision
)