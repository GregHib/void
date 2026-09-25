package world.gregs.voidps.engine.entity.obj

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import java.io.File

/**
 * Stores [GameObject]s by zone + [ObjectLayer]
 */
class GameObjectHashMap {
    private var data = Int2IntOpenHashMap(EXPECTED_OBJECT_COUNT)

    operator fun get(obj: GameObject): Int = data.getOrDefault(index(obj), ABSENT)

    operator fun get(x: Int, y: Int, level: Int, layer: Int): Int = data.getOrDefault(index(x, y, level, layer), ABSENT)

    operator fun set(x: Int, y: Int, level: Int, layer: Int, mask: Int) {
        data[index(x, y, level, layer)] = mask
    }

    fun add(obj: GameObject, mask: Int) {
        val index = index(obj)
        val currentFlags = data.getOrDefault(index, 0)
        data[index] = currentFlags or mask
    }

    fun remove(obj: GameObject, mask: Int) {
        val index = index(obj)
        val currentFlags = data.getOrDefault(index, ABSENT)
        // Storing a cleared flag for a tile we don't hold reads back as neither empty nor replaced
        if (currentFlags == ABSENT) {
            return
        }
        data[index] = currentFlags and mask.inv()
    }

    fun deallocateZone(zone: Zone) {
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                data.remove(index(zone.id, Tile.index(x, y, ObjectLayer.WALL)))
                data.remove(index(zone.id, Tile.index(x, y, ObjectLayer.WALL_DECORATION)))
                data.remove(index(zone.id, Tile.index(x, y, ObjectLayer.GROUND)))
                data.remove(index(zone.id, Tile.index(x, y, ObjectLayer.GROUND_DECORATION)))
            }
        }
    }

    fun clear() {
        data.clear()
    }

    fun save(file: File, storeUnused: Boolean) {
        val writer = ArrayWriter(HEADER_BYTES + data.size * 8)
        writer.writeInt(MAGIC)
        writer.writeInt(if (storeUnused) STORE_UNUSED else 0)
        writer.writeInt(data.size)
        val list = data.toList()
        for (pair in list) {
            writer.writeInt(pair.first)
        }
        for (pair in list) {
            writer.writeInt(pair.second)
        }
        file.writeBytes(writer.toArray())
    }

    /**
     * Loads objects from [file], returning the number read or [INVALID] if it wasn't written in
     * this format or was written with a different [storeUnused], in which case nothing is loaded
     */
    fun load(file: File, storeUnused: Boolean): Int {
        if (file.length() < HEADER_BYTES) {
            return INVALID
        }
        val reader = ArrayReader(file.readBytes())
        if (reader.readInt() != MAGIC) {
            return INVALID
        }
        val flags = reader.readInt()
        if ((flags and STORE_UNUSED != 0) != storeUnused) {
            return INVALID
        }
        val size = reader.readInt()
        val keys = IntArray(size)
        val values = IntArray(size)
        reader.readBytes(keys)
        reader.readBytes(values)
        data = Int2IntOpenHashMap(keys, values)
        return size
    }

    companion object {
        const val INVALID = -1

        // Files written before the header exist in the wild; their first int is an object count,
        // which can never reach this value, so they're rejected and rebuilt
        private const val MAGIC = 0x564F4944
        private const val STORE_UNUSED = 0x1
        private const val HEADER_BYTES = 12
        private const val EXPECTED_OBJECT_COUNT = 74_000
        private const val ABSENT = -1

        private fun index(obj: GameObject): Int = index(obj.x, obj.y, obj.level, ObjectLayer.layer(obj.shape))
        private fun index(x: Int, y: Int, level: Int, layer: Int): Int = index(Zone.tileIndex(x, y, level), Tile.index(x, y, layer))
        private fun index(zone: Int, tile: Int): Int = zone or (tile shl 24)
    }
}
