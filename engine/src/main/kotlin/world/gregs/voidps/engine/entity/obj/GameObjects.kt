package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.network.login.protocol.encode.send
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAddition
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectRemoval
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import java.io.File

/**
 * Stores GameObjects and modifications mainly for verifying interactions
 * "original" objects refer to those [set] on game load from the cache or map file
 * "temporary" objects are [add]ed or [remove]ed with a reset timer
 * "permanent" objects are [add]ed or [remove]ed without a reset timer (but don't persist after server restart)
 * Note: [Spawn] and [Despawn] events are only emitted for temporary and permanent objects, original objects that are added or removed do not emit events.
 * @param storeUnused store non-interactive and objects without configs for debugging and content dev (uses ~240MB more ram).
 */
class GameObjects(
    private val batches: ZoneBatchUpdates,
    private val storeUnused: Boolean = false,
) : ZoneBatchUpdates.Sender {
    private val collisionAdd = GameObjectCollisionAdd()
    private val collisionRemove = GameObjectCollisionRemove()
    private val map = GameObjectHashMap()
    private val replacements: MutableMap<Int, Int> = Int2IntOpenHashMap()
    val timers = GameObjectTimers()
    var size = 0
        private set

    fun load(file: File) {
        size = map.load(file)
    }

    fun save(file: File) {
        map.save(file)
    }

    /**
     * Adds a temporary object with [id] [tile] [shape] and [rotation]
     * Optionally removed after [ticks]
     */
    fun add(id: String, tile: Tile, shape: Int = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation: Int = 0, ticks: Int = NEVER, collision: Boolean = true): GameObject {
        val obj = GameObject(ObjectDefinitions.get(id).id, tile, shape, rotation)
        add(obj)
        timers.add(obj, ticks) {
            remove(obj, collision)
        }
        return obj
    }

    /**
     * Adds temporary objects to [replacements] or un-flags the original removed object
     */
    fun add(obj: GameObject, collision: Boolean = true) {
        if (obj.intId == -1) {
            return
        }
        val original = map[obj]
        if (original == obj.value(replaced = true)) {
            // Re-add original
            map.remove(obj, REPLACED)
            batches.add(obj.tile.zone, ObjectAddition(obj.tile.id, obj.intId, obj.shape, obj.rotation))
            if (collision) {
                collisionAdd.modify(obj)
            }
            size++
        } else {
            map.add(obj, REPLACED)
            if (replaced(original)) {
                // Remove replacements (if exists)
                val current = replacements[obj.index]
                if (current != null) {
                    val currentObj = remove(current, obj, collision)
                    Despawn.gameObject(currentObj)
                }
            } else if (original > 0) {
                // Remove original (if exists)
                remove(original, obj, collision)
            }

            // Add replacement
            replacements[obj.index] = obj.value(replaced = true)
            batches.add(obj.tile.zone, ObjectAddition(obj.tile.id, obj.intId, obj.shape, obj.rotation))
            if (collision) {
                collisionAdd.modify(obj)
            }
            size++
            Spawn.gameObject(obj)
        }
    }

    private fun remove(objectValue: Int, obj: GameObject, collision: Boolean): GameObject {
        val gameObject = GameObject(id(objectValue), obj.x, obj.y, obj.level, shape(objectValue), rotation(objectValue))
        batches.add(obj.tile.zone, ObjectRemoval(obj.tile.id, gameObject.shape, gameObject.rotation))
        if (collision) {
            collisionRemove.modify(gameObject)
        }
        size--
        return gameObject
    }

    /**
     * Sets the original placement of a game object
     */
    fun set(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int, definition: ObjectDefinition) {
        collisionAdd.modify(definition, x, y, level, shape, rotation)
        if (interactive(definition)) {
            map[x, y, level, ObjectLayer.layer(shape)] = value(false, id, shape, rotation)
            size++
        }
    }

    /**
     * Decide to store [GameObject]s which don't have options or configs
     * Skipping unused objects uses less ram but makes content creation harder.
     */
    private fun interactive(definition: ObjectDefinition) = storeUnused || definition.options != null || definition.contains("id")

    /**
     * Removes an object, optionally reverting after [ticks]
     */
    fun remove(obj: GameObject, ticks: Int = NEVER, collision: Boolean = true) {
        remove(obj)
        timers.add(obj, ticks) {
            add(obj, collision)
        }
    }

    /**
     * Flags original objects as removed, or removes temporarily replaced objects
     *
     * Note: If a temp object is added and removed on top of an original removed object
     * then the original object will no longer be removed.
     */
    fun remove(obj: GameObject, collision: Boolean = true) {
        if (obj.intId == -1) {
            return
        }
        val original = map[obj]
        if (replacements[obj.index] == obj.value(replaced = true)) {
            // Remove replacement
            replacements.remove(obj.index)
            batches.add(obj.tile.zone, ObjectRemoval(obj.tile.id, obj.shape, obj.rotation))
            if (collision) {
                collisionRemove.modify(obj)
            }
            size--
            Despawn.gameObject(obj)
            // Re-add original (if exists)
            map.remove(obj, REPLACED)
            if (original > 1) {
                val originalObj = GameObject(id(original), obj.x, obj.y, obj.level, shape(original), rotation(original))
                batches.add(obj.tile.zone, ObjectAddition(obj.tile.id, originalObj.intId, originalObj.shape, originalObj.rotation))
                if (collision) {
                    collisionAdd.modify(originalObj)
                }
                size++
            }
        } else if (original == obj.value(replaced = false) && original != 0) {
            // Remove original
            map.add(obj, REPLACED)
            batches.add(obj.tile.zone, ObjectRemoval(obj.tile.id, obj.shape, obj.rotation))
            if (collision) {
                collisionRemove.modify(obj)
            }
            size--
        }
    }

    /**
     * Replaces [original] object with [id], optionally reverting after [ticks]
     */
    fun replace(
        original: GameObject,
        id: String,
        tile: Tile = original.tile,
        shape: Int = original.shape,
        rotation: Int = original.rotation,
        ticks: Int = NEVER,
        collision: Boolean = true,
    ): GameObject {
        val replacement = GameObject(ObjectDefinitions.get(id).id, tile, shape, rotation)
        replace(original, replacement, ticks, collision)
        return replacement
    }

    /**
     * Replaces [original] object with [replacement], optionally reverting after [ticks]
     */
    fun replace(original: GameObject, replacement: GameObject, ticks: Int = NEVER, collision: Boolean = true) {
        remove(original, collision)
        add(replacement, collision)
        timers.add(setOf(original, replacement), ticks) {
            remove(replacement, collision)
            add(original, collision)
        }
    }

    fun find(tile: Tile, filter: (GameObject) -> Boolean) = findOrNull(tile, filter) ?: error("Object not found at $tile")

    fun findOrNull(tile: Tile, filter: (GameObject) -> Boolean) = get(tile, ObjectLayer.WALL, filter)
        ?: get(tile, ObjectLayer.WALL_DECORATION, filter)
        ?: get(tile, ObjectLayer.GROUND, filter)
        ?: get(tile, ObjectLayer.GROUND_DECORATION, filter)

    private fun get(tile: Tile, layer: Int, block: (GameObject) -> Boolean): GameObject? {
        val obj = getLayer(tile, layer) ?: return null
        if (block.invoke(obj)) {
            return obj
        }
        return null
    }

    fun find(tile: Tile, id: String) = findOrNull(tile, id) ?: error("Object '$id' not found at $tile")

    /**
     * Get object by string [id]
     */
    fun findOrNull(tile: Tile, id: String) = get(tile, ObjectLayer.WALL, id)
        ?: get(tile, ObjectLayer.WALL_DECORATION, id)
        ?: get(tile, ObjectLayer.GROUND, id)
        ?: get(tile, ObjectLayer.GROUND_DECORATION, id)

    private fun get(tile: Tile, layer: Int, id: String): GameObject? {
        val obj = getLayer(tile, layer) ?: return null
        if (obj.id == id) {
            return obj
        }
        return null
    }

    /**
     * Get all objects on [tile]
     */
    fun at(tile: Tile) = listOfNotNull(
        getLayer(tile, ObjectLayer.WALL),
        getLayer(tile, ObjectLayer.WALL_DECORATION),
        getLayer(tile, ObjectLayer.GROUND),
        getLayer(tile, ObjectLayer.GROUND_DECORATION),
    )

    /**
     * Get object by integer [id]
     */
    fun findOrNull(tile: Tile, id: Int) = get(tile, ObjectLayer.WALL, id)
        ?: get(tile, ObjectLayer.WALL_DECORATION, id)
        ?: get(tile, ObjectLayer.GROUND, id)
        ?: get(tile, ObjectLayer.GROUND_DECORATION, id)

    private fun get(tile: Tile, layer: Int, id: Int): GameObject? {
        val obj = getLayer(tile, layer) ?: return null
        if (obj.intId == id) {
            return obj
        }
        return null
    }

    /**
     * Get object by [shape]
     */
    fun getShape(tile: Tile, shape: Int): GameObject? {
        val obj = getLayer(tile, ObjectLayer.layer(shape)) ?: return null
        if (obj.shape == shape) {
            return obj
        }
        return null
    }

    /**
     * Get object by [layer]
     */
    fun getLayer(tile: Tile, layer: Int): GameObject? {
        val value = map[tile.x, tile.y, tile.level, layer]
        if (empty(value)) {
            return null
        }
        if (replaced(value)) {
            val replacement = replacements[index(tile, layer)] ?: return null
            return GameObject(id(replacement), tile.x, tile.y, tile.level, shape(replacement), rotation(replacement))
        }
        return GameObject(id(value), tile.x, tile.y, tile.level, shape(value), rotation(value))
    }

    /**
     * Checks if an object exists
     */
    fun contains(obj: GameObject): Boolean {
        val value = map[obj]
        val replacement = if (replaced(value)) {
            replacements[obj.index] ?: return false
        } else {
            value
        }
        return id(replacement) == obj.intId && shape(replacement) == obj.shape && rotation(replacement) == obj.rotation
    }

    /**
     * Resets all original objects in [zone]
     */
    fun reset(zone: Zone, collision: Boolean = true) {
        forEachReplaced(zone) { tile, layer, value ->
            if (value != 1) {
                add(GameObject(id(value), tile, shape(value), rotation(value)), collision)
            }
            val replaced = replacements[index(tile, layer)]
            if (replaced != null) {
                remove(GameObject(id(replaced), tile, shape(replaced), rotation(replaced)), collision)
            }
        }
    }

    /**
     * Clears all replacement objects
     * Note: this won't reset permanently removed original objects
     */
    fun reset() {
        timers.reset()
        replacements.map { (index, value) ->
            GameObject(id(value), x(index), y(index), level(index), shape(value), rotation(value))
        }.forEach {
            remove(it)
        }
        replacements.clear()
    }

    /**
     * Clears [zone] of all original and replacement objects
     * Note: Doesn't undo collision changes
     */
    fun clear(zone: Zone) {
        map.deallocateZone(zone.tile.x, zone.tile.y, zone.level)
    }

    /**
     * Clears all original and replacement objects
     * Note: Doesn't undo collision changes
     */
    fun clear() {
        map.clear()
        replacements.clear()
    }

    override fun send(player: Player, zone: Zone) {
        forEachReplaced(zone) { tile, layer, value ->
            if (value != 1) {
                player.client?.send(ObjectRemoval(tile.id, shape(value), rotation(value)))
            }
            val replaced = replacements[index(tile, layer)]
            if (replaced != null) {
                player.client?.send(ObjectAddition(tile.id, id(replaced), shape(replaced), rotation(replaced)))
            }
        }
    }

    private fun forEachReplaced(zone: Zone, block: (Tile, Int, Int) -> Unit) {
        val zoneTileX = zone.tile.x
        val zoneTileY = zone.tile.y
        val level = zone.level
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                for (layer in 0 until 4) {
                    val value = map[zoneTileX + x, zoneTileY + y, level, layer]
                    if (empty(value) || !replaced(value)) {
                        continue
                    }
                    val tile = zone.tile.add(x, y)
                    block.invoke(tile, layer, value)
                }
            }
        }
    }

    companion object {
        const val NEVER = -1
        private const val REPLACED = 0x1

        private fun empty(value: Int) = value == -1 || value == 0

        /**
         * Value represents an objects id, shape and rotation plus and extra bit for whether the object has been [REPLACED] or removed.
         */
        internal fun value(replaced: Boolean, id: Int, shape: Int, rotation: Int): Int = replaced.toInt() or (rotation shl 1) + (shape shl 3) + (id shl 8)

        private fun id(value: Int): Int = value shr 8 and 0x1ffff
        private fun shape(value: Int): Int = value shr 3 and 0x1f
        private fun rotation(value: Int): Int = value shr 1 and 0x3
        private fun replaced(value: Int) = value and REPLACED == REPLACED
        private fun GameObject.value(replaced: Boolean): Int = replaced.toInt() or ((packed shr 30).toInt() shl 1)

        /**
         * Index represents a [Tile] and [ObjectLayer]
         */
        private fun index(tile: Tile, layer: Int) = tile.id or (layer shl 30)
        private fun level(index: Int) = index shr 28 and 0x2
        private fun layer(index: Int) = index shr 30 and 0x2
        private fun x(index: Int) = index shr 14 and 0x3fff
        private fun y(index: Int) = index and 0x3fff
        private val GameObject.index: Int
            get() = (packed and 0x3fffffff).toInt() or (ObjectLayer.layer(shape) shl 30)
    }
}

/**
 * Replaces an existing map objects with [id] [tile] [shape] and [rotation], modifying [collision] and
 * optionally removed after [ticks]
 */
fun GameObject.replace(id: String, tile: Tile = this.tile, shape: Int = this.shape, rotation: Int = this.rotation, ticks: Int = -1, collision: Boolean = true): GameObject = get<GameObjects>().replace(this, id, tile, shape, rotation, ticks, collision)

/**
 * Removes an existing map [GameObject] and its [collision], optionally reverted after [ticks]
 */
fun GameObject.remove(ticks: Int = -1, collision: Boolean = true) {
    get<GameObjects>().remove(this, ticks, collision)
}
