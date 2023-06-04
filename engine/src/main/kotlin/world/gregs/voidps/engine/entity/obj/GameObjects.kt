package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.network.encode.chunk.ObjectAddition
import world.gregs.voidps.network.encode.chunk.ObjectRemoval

/**
 * Stores GameObjects and modifications mainly for verifying interactions
 */
class GameObjects(
    private val collisions:  GameObjectCollision,
    private val batches: ChunkBatchUpdates,
) {
    private val map = GameObjectMap()
    private val replacements: MutableMap<Int, Int> = Int2IntOpenHashMap()
    var size = 0
        private set

    /**
     * Adds temporary objects to [replacements] or un-flags original removed objects
     */
    fun add(tile: Tile, obj: GameMapObject, collision: Boolean = true) {
        val group = obj.group
        val value = map[tile.x, tile.y, tile.plane, group]
        val original = toObject(value)
        if (replaced(value) && original == obj.value) {
            // Re-add original
            map.remove(tile.x, tile.y, tile.plane, group, REPLACED)
            batches.add(tile.chunk, ObjectAddition(tile.id, obj.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(tile, obj, add = true)
            }
            size++
        } else {
            // Remove original (if exists)
            map.add(tile.x, tile.y, tile.plane, group, REPLACED)
            if (original != 0) {
                val originalObj = GameMapObject(original)
                batches.add(tile.chunk, ObjectRemoval(tile.id, originalObj.type, originalObj.rotation))
                if (collision) {
                    collisions.modify(tile, originalObj, add = false)
                }
                size--
            }

            // Add replacement
            replacements[index(tile.x, tile.y, tile.plane, group)] = obj.value
            batches.add(tile.chunk, ObjectAddition(tile.id, obj.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(tile, obj, add = true)
            }
            size++
        }
    }

    /**
     * Sets the original placement of a game object
     */
    fun set(x: Int, y: Int, plane: Int, id: Int, type: Int, rotation: Int, definition: ObjectDefinition) {
        val group = ObjectGroup.group(type)
        if (group != ObjectGroup.WALL_DECORATION) {
            collisions.modify(definition, x, y, plane, type, rotation, add = true)
        }
        if (group == ObjectGroup.INTERACTIVE_OBJECT && interactive(definition)) {
            map[x, y, plane, group] = toValue(GameMapObject.value(id, type, rotation))
        }
        size++
    }

    private fun interactive(definition: ObjectDefinition) = DEBUG || definition.options != null || definition.has("id")

    /**
     * Flags original objects as removed, or removes temporarily replaced objects
     *
     * Note: If a temp object is added and removed on top of an original removed object
     * then the original object will no longer be removed.
     */
    fun remove(tile: Tile, obj: GameMapObject, collision: Boolean = true) {
        val group = obj.group
        val value = map[tile.x, tile.y, tile.plane, group]
        val original = toObject(value)
        if (replaced(value) && replacements[index(tile.x, tile.y, tile.plane, group)] == obj.value) {
            // Remove replacement
            replacements.remove(index(tile.x, tile.y, tile.plane, group))
            batches.add(tile.chunk, ObjectRemoval(tile.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(tile, obj, add = false)
            }
            size--
            // Re-add original (if exists)
            map.remove(tile.x, tile.y, tile.plane, group, REPLACED)
            if (original != 0) {
                val originalObj = GameMapObject(original)
                if (collision) {
                    batches.add(tile.chunk, ObjectAddition(tile.id, originalObj.id, originalObj.type, originalObj.rotation))
                    collisions.modify(tile, originalObj, add = true)
                }
                size++
            }
        } else if (original == obj.value && original != 0) {
            // Remove original
            map.add(tile.x, tile.y, tile.plane, group, REPLACED)
            batches.add(tile.chunk, ObjectRemoval(tile.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(tile, obj, add = true)
            }
            size--
        }
    }

    fun get(tile: Tile, group: Int): GameMapObject? {
        val value = map[tile.x, tile.y, tile.plane, group]
        if (value == -1 || value == 0) {
            return null
        }
        if (replaced(value)) {
            return GameMapObject(replacements[index(tile.x, tile.y, tile.plane, group)] ?: return null)
        }
        return GameMapObject(toObject(value))
    }

    fun clear() {
        map.clear()
        replacements.clear()
    }

    companion object {
        private fun toValue(value: Int) = value shl 1
        private fun toObject(value: Int) = value shr 1
        private fun replaced(value: Int) = value and REPLACED == REPLACED

        private const val REPLACED = 0x1
        var DEBUG = false

        private fun index(x: Int, y: Int, level: Int, group: Int): Int {
            return level + (group shl 2) + (x shl 4) + (y shl 18)
        }
    }

}