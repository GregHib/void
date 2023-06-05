package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.network.encode.chunk.ObjectAddition
import world.gregs.voidps.network.encode.chunk.ObjectRemoval
import world.gregs.voidps.network.encode.send

/**
 * Stores GameObjects and modifications mainly for verifying interactions
 */
class GameObjects(
    private val collisions: GameObjectCollision,
    private val batches: ChunkBatchUpdates,
) : ChunkBatchUpdates.Sender {
    private val map = GameObjectMap()
    private val replacements: MutableMap<Int, Int> = Int2IntOpenHashMap()
    var size = 0
        private set

    /**
     * Adds temporary objects to [replacements] or un-flags original removed objects
     */
    fun add(obj: GameMapObject, collision: Boolean = true) {
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
                val originalObj = GameMapObject(original, obj.x, obj.y, obj.plane)
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
    fun set(x: Int, y: Int, plane: Int, id: Int, type: Int, rotation: Int, definition: ObjectDefinition) {
        val group = ObjectGroup.group(type)
        if (group != ObjectGroup.WALL_DECORATION) {
            collisions.modify(definition, x, y, plane, type, rotation, add = true)
        }
        if (group == ObjectGroup.INTERACTIVE && interactive(definition)) {
            map[x, y, plane, group] = toValue(GameMapObject.value(id, type, rotation))
            size++
        }
    }

    private fun interactive(definition: ObjectDefinition) = LOAD_UNUSED || definition.options != null || definition.has("id")

    /**
     * Flags original objects as removed, or removes temporarily replaced objects
     *
     * Note: If a temp object is added and removed on top of an original removed object
     * then the original object will no longer be removed.
     */
    fun remove(obj: GameMapObject, collision: Boolean = true) {
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
                val originalObj = GameMapObject(original, obj.x, obj.y, obj.plane)
                if (collision) {
                    batches.add(obj.tile.chunk, ObjectAddition(obj.tile.id, originalObj.intId, originalObj.type, originalObj.rotation))
                    collisions.modify(originalObj, add = true)
                }
                size++
            }
        } else if (original == obj.value && original != 0) {
            // Remove original
            map.add(obj.tile.x, obj.tile.y, obj.tile.plane, group, REPLACED)
            batches.add(obj.tile.chunk, ObjectRemoval(obj.tile.id, obj.type, obj.rotation))
            if (collision) {
                collisions.modify(obj, add = true)
            }
            size--
        }
    }

    operator fun get(tile: Tile, id: String) = get(tile, ObjectGroup.INTERACTIVE, id)

    operator fun get(tile: Tile, group: Int, id: String): GameMapObject? {
        val obj = get(tile, group) ?: return null
        if (obj.id == id) {
            return obj
        }
        return null
    }

    operator fun get(tile: Tile, group: Int, id: Int): GameMapObject? {
        val obj = get(tile, group) ?: return null
        if (obj.intId == id) {
            return obj
        }
        return null
    }

    operator fun get(tile: Tile, group: Int): GameMapObject? {
        val value = map[tile.x, tile.y, tile.plane, group]
        if (value == -1 || value == 0) {
            return null
        }
        if (replaced(value)) {
            return GameMapObject(replacements[index(tile.x, tile.y, tile.plane, group)] ?: return null, tile.x, tile.y, tile.plane)
        }
        return GameMapObject(toObject(value), tile.x, tile.y, tile.plane)
    }

    fun clear(chunk: Chunk) {
        val array = map.allocateIfAbsent(chunk.tile.x, chunk.tile.y, chunk.plane)
        for (i in array.indices) {
            array[i] = array[i] and REPLACED.inv()
        }
        for (tile in chunk.toCuboid()) {
            replacements.remove(index(tile.x, tile.y, tile.plane, ObjectGroup.WALL))
            replacements.remove(index(tile.x, tile.y, tile.plane, ObjectGroup.WALL_DECORATION))
            replacements.remove(index(tile.x, tile.y, tile.plane, ObjectGroup.INTERACTIVE))
            replacements.remove(index(tile.x, tile.y, tile.plane, ObjectGroup.GROUND_DECORATION))
        }
    }

    fun clear() {
        map.clear()
        replacements.clear()
    }

    fun contains(obj: GameMapObject): Boolean {
        val value = map[obj.x, obj.y, obj.plane, obj.group]
        if (value == -1 || value == 0) {
            return false
        }
        if (replaced(value)) {
            replacements[obj.index] ?: return false
            return true
        }
        return true
    }

    override fun send(player: Player, chunk: Chunk) {
        for (tile in chunk.toCuboid()) {
            for (group in ObjectGroup.all) {
                val int = map[tile.x, tile.y, tile.plane, group]
                if (replaced(int)) {
                    val value = toObject(int)
                    if (value != 0) {
                        player.client?.send(ObjectRemoval(tile.id, GameMapObject.type(value), GameMapObject.rotation(value)))
                    }
                    val replaced = replacements[index(tile.x, tile.y, tile.plane, group)]
                    if (replaced != null) {
                        player.client?.send(ObjectAddition(tile.id, GameMapObject.id(replaced), GameMapObject.type(replaced), GameMapObject.rotation(replaced)))
                    }
                }
            }
        }
    }

    companion object {
        private fun toValue(value: Int) = value shl 1
        private fun toObject(value: Int) = value shr 1

        private fun replaced(value: Int) = value and REPLACED == REPLACED
        private const val REPLACED = 0x1

        var LOAD_UNUSED = false // Don't bother loading objects which don't have options or configs (saves ~75MB ram)

        private val GameMapObject.index: Int
            get() = index(x, y, plane, group)

        private fun index(x: Int, y: Int, level: Int, group: Int) = level + (group shl 2) + (x shl 4) + (y shl 18)
    }

}