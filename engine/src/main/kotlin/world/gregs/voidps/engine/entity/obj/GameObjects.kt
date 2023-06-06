package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.network.encode.chunk.ObjectAddition
import world.gregs.voidps.network.encode.chunk.ObjectRemoval
import world.gregs.voidps.network.encode.send

/**
 * Stores GameObjects and modifications mainly for verifying interactions
 * "original" objects refer to those [set] on game load from the cache or map file
 * "temporary" objects are [add]ed or [remove]ed with a reset timer
 * "permanent" objects are [add]ed or [remove]ed without a reset timer (but don't persist after server restart)
 */
class GameObjects(
    private val collisions: GameObjectCollision,
    private val batches: ChunkBatchUpdates,
    private val definitions: ObjectDefinitions
) : ChunkBatchUpdates.Sender {
    private val map = GameObjectMap()
    private val replacements: MutableMap<Int, Int> = Int2IntOpenHashMap()
    val timers = GameObjectTimers()
    var size = 0
        private set

    /**
     * Adds a temporary object with [id] [tile] [type] and [rotation]
     * Optionally removed after [ticks]
     */
    fun add(id: String, tile: Tile, type: Int = ObjectType.INTERACTIVE, rotation: Int = 0, ticks: Int = NEVER, collision: Boolean = true): GameObject {
        val obj = GameObject(definitions.get(id).id, tile, type, rotation)
        add(obj)
        timers.add(obj, ticks) {
            remove(obj, collision)
        }
        return obj
    }

    /**
     * Adds temporary objects to [replacements] or un-flags original removed objects
     */
    fun add(obj: GameObject, collision: Boolean = true) {
        if (obj.intId == -1) {
            return
        }
        val group = obj.group
        val value = map[obj.x, obj.y, obj.plane, group]
        val original = toObject(value)
        if (replaced(value) && original == obj.value) {
            // Re-add original
            map.remove(obj.x, obj.y, obj.plane, group, REPLACED)
            batches.add(obj.tile.chunk, ObjectAddition(obj.tile.id, obj.intId, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(obj, add = true)
            }
            size++
        } else {
            // Remove original (if exists)
            map.add(obj.x, obj.y, obj.plane, group, REPLACED)
            if (original != 0) {
                val originalObj = GameObject(original, obj.x, obj.y, obj.plane)
                batches.add(obj.tile.chunk, ObjectRemoval(obj.tile.id, originalObj.type, originalObj.rotation))
                if (collision) {
                    collisions.modify(originalObj, add = false)
                }
                size--
            }

            // Add replacement
            replacements[obj.index] = obj.value
            batches.add(obj.tile.chunk, ObjectAddition(obj.tile.id, obj.intId, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(obj, add = true)
            }
            size++
        }
    }

    /**
     * Sets the original placement of a game object
     */
    fun set(id: Int, x: Int, y: Int, plane: Int, type: Int, rotation: Int, definition: ObjectDefinition) {
        if (id == -1) {
            return
        }
        val group = ObjectGroup.group(type)
        if (group != ObjectGroup.WALL_DECORATION) {
            collisions.modify(definition, x, y, plane, type, rotation, add = true)
        }
        if (interactive(definition)) {
            map[x, y, plane, group] = toValue(GameObject.value(id, type, rotation))
            size++
        }
    }

    /**
     * Decide to store [GameObject]s which don't have options or configs
     * Skipping unused objects uses ~75MB less ram but makes content creation harder.
     */
    private fun interactive(definition: ObjectDefinition) = LOAD_UNUSED || definition.options != null || definition.has("id")

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
        val group = obj.group
        val value = map[obj.x, obj.y, obj.plane, group]
        val original = toObject(value)
        if (replaced(value) && replacements[obj.index] == obj.value) {
            // Remove replacement
            replacements.remove(obj.index)
            batches.add(obj.tile.chunk, ObjectRemoval(obj.tile.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(obj, add = false)
            }
            size--
            // Re-add original (if exists)
            map.remove(obj.tile.x, obj.tile.y, obj.tile.plane, group, REPLACED)
            if (original != 0) {
                val originalObj = GameObject(original, obj.x, obj.y, obj.plane)
                batches.add(obj.tile.chunk, ObjectAddition(obj.tile.id, originalObj.intId, originalObj.type, originalObj.rotation))
                if (collision) {
                    collisions.modify(originalObj, add = true)
                }
                size++
            }
        } else if (original == obj.value && original != 0) {
            // Remove original
            map.add(obj.tile.x, obj.tile.y, obj.tile.plane, group, REPLACED)
            batches.add(obj.tile.chunk, ObjectRemoval(obj.tile.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(obj, add = false)
            }
            size--
        }
    }

    /**
     * Replaces [original] object with [id], optionally reverting after [ticks]
     */
    fun replace(original: GameObject, id: String, tile: Tile = original.tile, type: Int = original.type, rotation: Int = original.rotation, ticks: Int = NEVER, collision: Boolean = true): GameObject {
        val replacement = GameObject(definitions.get(id).id, tile, type, rotation)
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

    /**
     * Get object by string [id]
     */
    operator fun get(tile: Tile, id: String) = get(tile, ObjectGroup.WALL, id)
        ?: get(tile, ObjectGroup.WALL_DECORATION, id)
        ?: get(tile, ObjectGroup.INTERACTIVE, id)
        ?: get(tile, ObjectGroup.GROUND_DECORATION, id)

    private fun get(tile: Tile, group: Int, id: String): GameObject? {
        val obj = getGroup(tile, group) ?: return null
        if (obj.id == id) {
            return obj
        }
        return null
    }

    /**
     * Get all objects on [tile]
     */
    operator fun get(tile: Tile) = listOfNotNull(
        getGroup(tile, ObjectGroup.WALL),
        getGroup(tile, ObjectGroup.WALL_DECORATION),
        getGroup(tile, ObjectGroup.INTERACTIVE),
        getGroup(tile, ObjectGroup.GROUND_DECORATION)
    )

    /**
     * Get object by integer [id]
     */
    operator fun get(tile: Tile, id: Int) = get(tile, ObjectGroup.WALL, id)
        ?: get(tile, ObjectGroup.WALL_DECORATION, id)
        ?: get(tile, ObjectGroup.INTERACTIVE, id)
        ?: get(tile, ObjectGroup.GROUND_DECORATION, id)

    private fun get(tile: Tile, group: Int, id: Int): GameObject? {
        val obj = getGroup(tile, group) ?: return null
        if (obj.intId == id) {
            return obj
        }
        return null
    }

    /**
     * Get object by type [type]
     */
    fun getType(tile: Tile, type: Int): GameObject? {
        val obj = getGroup(tile, ObjectGroup.group(type)) ?: return null
        if (obj.type == type) {
            return obj
        }
        return null
    }

    /**
     * Get object by group [group]
     */
    fun getGroup(tile: Tile, group: Int): GameObject? {
        val value = map[tile.x, tile.y, tile.plane, group]
        if (empty(value)) {
            return null
        }
        if (replaced(value)) {
            val replacement = replacements[index(tile.x, tile.y, tile.plane, group)] ?: return null
            return GameObject(replacement, tile.x, tile.y, tile.plane)
        }
        return GameObject(toObject(value), tile.x, tile.y, tile.plane)
    }

    /**
     * Checks if an object exists
     */
    fun contains(obj: GameObject): Boolean {
        val value = map[obj.x, obj.y, obj.plane, obj.group]
        val replacement = if (replaced(value)) {
            replacements[obj.index] ?: return false
        } else {
            toObject(value)
        }
        return GameObject.id(replacement) == obj.intId && GameObject.type(replacement) == obj.type && GameObject.rotation(replacement) == obj.rotation
    }

    /**
     * Resets all original objects in [chunk]
     */
    fun reset(chunk: Chunk, collision: Boolean = true) {
        forEachReplaced(chunk) { tile, group, int ->
            val value = toObject(int)
            if (value != 0) {
                add(GameObject(GameObject.id(value), tile, GameObject.type(value), GameObject.rotation(value)), collision)
            }
            val replaced = replacements[index(tile.x, tile.y, tile.plane, group)]
            if (replaced != null) {
                remove(GameObject(GameObject.id(replaced), tile, GameObject.type(replaced), GameObject.rotation(replaced)), collision)
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
            GameObject(GameObject.id(value), x(index), y(index), level(index), GameObject.type(value), GameObject.rotation(value))
        }.forEach {
            remove(it)
        }
        replacements.clear()
    }

    /**
     * Clears [chunk] of all original and replacement objects
     * Note: Doesn't undo collision changes
     */
    fun clear(chunk: Chunk) {
        map.deallocateIfPresent(chunk.tile.x, chunk.tile.y, chunk.plane)
    }

    /**
     * Clears all original and replacement objects
     * Note: Doesn't undo collision changes
     */
    fun clear() {
        map.clear()
        replacements.clear()
    }

    override fun send(player: Player, chunk: Chunk) {
        forEachReplaced(chunk) { tile, group, int ->
            val value = toObject(int)
            if (value != 0) {
                player.client?.send(ObjectRemoval(tile.id, GameObject.type(value), GameObject.rotation(value)))
            }
            val replaced = replacements[index(tile.x, tile.y, tile.plane, group)]
            if (replaced != null) {
                player.client?.send(ObjectAddition(tile.id, GameObject.id(replaced), GameObject.type(replaced), GameObject.rotation(replaced)))
            }
        }
    }

    private fun forEachReplaced(chunk: Chunk, block: (Tile, Int, Int) -> Unit) {
        val chunkX = chunk.tile.x
        val chunkY = chunk.tile.y
        val plane = chunk.plane
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                for (group in 0 until 4) {
                    val value = map[chunkX + x, chunkY + y, plane, group]
                    if (!replaced(value)) {
                        continue
                    }
                    val tile = chunk.tile.add(x, y)
                    block.invoke(tile, group, value)
                }
            }
        }
    }

    companion object {
        const val NEVER = -1

        private fun toValue(value: Int) = value shl 1
        private fun toObject(value: Int) = value shr 1

        private fun replaced(value: Int) = empty(value) || value and REPLACED == REPLACED

        private fun empty(value: Int) = value == -1 || value == 0
        private const val REPLACED = 0x1

        internal var LOAD_UNUSED = false

        private val GameObject.index: Int
            get() = index(x, y, plane, group)

        private fun index(x: Int, y: Int, level: Int, group: Int) = level + (group shl 2) + (x shl 4) + (y shl 18)
        private fun level(index: Int) = index and 0x2
        private fun group(index: Int) = index shr 2 and 0x2
        private fun x(index: Int) = index shr 4 and 0x3fff
        private fun y(index: Int) = index shr 18 and 0x3fff
    }
}

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation], modifying [collision] and
 * optionally removed after [ticks]
 */
fun GameObject.replace(id: String, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, collision: Boolean = true): GameObject {
    return get<GameObjects>().replace(this, id, tile, type, rotation, ticks, collision)
}

/**
 * Removes an existing map [GameObject] and its [collision], optionally reverted after [ticks]
 */
fun GameObject.remove(ticks: Int = -1, collision: Boolean = true) {
    get<GameObjects>().remove(this, ticks, collision)
}
