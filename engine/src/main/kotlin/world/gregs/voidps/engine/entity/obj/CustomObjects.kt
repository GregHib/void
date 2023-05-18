package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.batch.addObject
import world.gregs.voidps.engine.client.update.batch.removeObject
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.network.chunk.ChunkUpdate

class CustomObjects(
    private val objects: Objects,
    private val batches: ChunkBatches,
    private val factory: GameObjectFactory,
    private val collision: GameObjectCollision,
) {
    private val logger = InlineLogger()

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
            val name = "object_despawn_${gameObject.id}_${gameObject.tile}"
            World.run(name, ticks) {
                despawn(gameObject, collision)
            }
            objects.setTimer(gameObject, name)
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
            collision.modifyCollision(gameObject, add = false)
        }
        gameObject.events.emit(Unregistered)
    }

    private fun remove(gameObject: GameObject, update: ChunkUpdate) {
        val previousUpdate = gameObject.update
        if(previousUpdate != null) {
            batches.removeInitial(gameObject.tile.chunk, previousUpdate)
            gameObject.update = null
        }
        if (objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject.update = update
        }
        objects.removeTemp(gameObject)
    }

    private fun respawn(gameObject: GameObject, updateCollision: Boolean) {
        val update = addObject(gameObject)
        batches.update(gameObject.tile.chunk, update)
        if (updateCollision) {
            collision.modifyCollision(gameObject, add = false)
        }
        gameObject.events.emit(Registered)
    }

    private fun add(gameObject: GameObject, update: ChunkUpdate) {
        val previousUpdate = gameObject.update
        if (previousUpdate != null) {
            batches.removeInitial(gameObject.tile.chunk, previousUpdate)
            gameObject.update = null
        }
        if (!objects.isOriginal(gameObject)) {
            batches.addInitial(gameObject.tile.chunk, update)
            gameObject.update = update
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
            val name = "object_respawn_${original.id}_${original.tile}"
            World.run(name, ticks) {
                respawn(original, collision)
            }
            objects.setTimer(original, name)
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
    ): GameObject {
        val replacement = factory.spawn(id, tile, type, rotation, owner)
        switch(original, replacement, collision)
        // Revert
        if (ticks >= 0) {
            val name = "object_replace_${original.id}_${original.tile}_${replacement.id}_${replacement.tile}_"
            World.run(name, ticks) {
                switch(replacement, original, collision)
            }
            objects.setTimer(replacement, name)
        }
        return replacement
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
            val name = "object_double_replace_${firstOriginal.id}_${firstOriginal.tile}_${firstOriginal.id}_${firstOriginal.tile}"
            World.run(name, ticks) {
                switch(firstReplacement, firstOriginal, collision)
                switch(secondReplacement, secondOriginal, collision)
            }
            objects.setTimer(firstReplacement, name)
            objects.setTimer(secondReplacement, name)
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
            collision.modifyCollision(original, add = false)
        }
        original.events.emit(Unregistered)
        if (updateCollision) {
            collision.modifyCollision(replacement, add = true)
        }
        replacement.events.emit(Registered)
    }

    fun clear() {
        val objs = objects.getAll()
        for (obj in objs) {
            remove(obj)
        }
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
fun GameObject.replace(id: String, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null, collision: Boolean = true): GameObject {
    return get<CustomObjects>().replace(this, id, tile, type, rotation, ticks, owner, collision)
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