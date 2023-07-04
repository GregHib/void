package world.gregs.voidps.engine.map.chunk

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.engine.map.region.Region
import java.util.*
import kotlin.collections.set

class DynamicChunks(
    private val objects: GameObjects,
    private val collisions: Collisions,
    private val extract: MapDefinitions
) {
    private val chunks: MutableMap<Int, Int> = Int2IntArrayMap()
    private val regions = IntOpenHashSet()

    fun isDynamic(region: Region) = regions.contains(region.id)

    fun getDynamicChunk(chunk: Chunk) = chunks[chunk.id]

    /**
     * @param from The chunk to be copied
     * @param to The chunk things will be copied to
     */
    fun copy(from: Chunk, to: Chunk = from, rotation: Int = 0) {
        chunks[to.id] = from.rotatedId(rotation)
        update(from, to, rotation, true)
    }

    /**
     * @param from The region to be copied
     * @param to The region to be replaced
     */
    fun copy(from: Region, to: Region) {
        val targetChunks = LinkedList(to.toCuboid().toChunks())
        for (chunk in from.toCuboid().toChunks()) {
            copy(chunk, targetChunks.poll())
        }
    }

    /**
     * Clear the dynamic [chunk] and replace it with the original
     */
    fun clear(chunk: Chunk) {
        chunks.remove(chunk.id)
        update(chunk, chunk, 0, false)
    }

    /**
     * Clear the dynamic [region] and replace it with the original
     */
    fun clear(region: Region) {
        for (chunk in region.toCuboid().toChunks()) {
            clear(chunk)
        }
    }

    private fun update(from: Chunk, to: Chunk, rotation: Int, set: Boolean) {
        objects.reset(to)
        collisions.clear(to)
        extract.loadChunk(from, to, rotation)
        for (region in to.toCuboid(radius = 3).toRegions()) {
            if (set) {
                regions.add(region.id)
            } else if (region.toRectangle().toChunks().none { chunks.containsKey(it.id) }) {
                regions.remove(region.id)
            }
        }
        World.events.emit(ReloadChunk(to))
    }

    companion object {

        fun Chunk.dynamicId() =
            toChunkPosition(x, y, plane)

        fun Chunk.rotatedId(rotation: Int) =
            toRotatedChunkPosition(
                x,
                y,
                plane,
                rotation
            )

        fun getChunk(id: Int) = Chunk(getX(id), getY(id), getPlane(id))

        private fun getX(id: Int): Int {
            return id shr 14 and 0x7ff
        }

        private fun getY(id: Int): Int {
            return id shr 3 and 0x7ff
        }

        private fun getPlane(id: Int): Int {
            return id shr 28 and 0x7ff
        }

        private fun toChunkPosition(chunkX: Int, chunkY: Int, plane: Int): Int {
            return chunkY + (chunkX shl 14) + (plane shl 28)
        }

        private fun toRotatedChunkPosition(chunkX: Int, chunkY: Int, plane: Int, rotation: Int): Int {
            return rotation shl 1 or (plane shl 24) or (chunkX shl 14) or (chunkY shl 3)
        }

    }
}