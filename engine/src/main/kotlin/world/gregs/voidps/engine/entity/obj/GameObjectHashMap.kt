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

    operator fun get(obj: GameObject): Int = data.getOrDefault(index(obj), -1)

    operator fun get(x: Int, y: Int, level: Int, layer: Int): Int = data.getOrDefault(index(x, y, level, layer), -1)

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
        val currentFlags = data.getOrDefault(index, -1)
        data[index] = currentFlags and mask.inv()
    }

    fun deallocateZone(zoneX: Int, zoneY: Int, level: Int) {
        val zone = Zone.id(zoneX, zoneY, level)
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                data.remove(index(zone, Tile.index(x, y, ObjectLayer.WALL)))
                data.remove(index(zone, Tile.index(x, y, ObjectLayer.WALL_DECORATION)))
                data.remove(index(zone, Tile.index(x, y, ObjectLayer.GROUND)))
                data.remove(index(zone, Tile.index(x, y, ObjectLayer.GROUND_DECORATION)))
            }
        }
    }

    fun clear() {
        data.clear()
    }

    fun save(file: File) {
        val writer = ArrayWriter(4 + data.size * 8)
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

    fun load(file: File): Int {
        val reader = ArrayReader(file.readBytes())
        val size = reader.readInt()
        val keys = IntArray(size)
        val values = IntArray(size)
        reader.readBytes(keys)
        reader.readBytes(values)
        data = Int2IntOpenHashMap(keys, values)
        return size
    }

    companion object {
        private const val EXPECTED_OBJECT_COUNT = 74_000
        private fun index(obj: GameObject): Int = index(obj.x, obj.y, obj.level, ObjectLayer.layer(obj.shape))
        private fun index(x: Int, y: Int, level: Int, layer: Int): Int = index(Zone.tileIndex(x, y, level), Tile.index(x, y, layer))
        private fun index(zone: Int, tile: Int): Int = zone or (tile shl 24)
    }
}
