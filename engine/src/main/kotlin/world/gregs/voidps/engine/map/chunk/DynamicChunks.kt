package world.gregs.voidps.engine.map.chunk

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.file.MapExtract
import kotlin.collections.set

class DynamicChunks(
    private val objects: Objects,
    private val collisions: Collisions,
    private val extract: MapExtract
) {
    private val chunks: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()

    fun isDynamic(chunk: Chunk) = chunks.containsKey(chunk.id)

    fun getDynamicChunk(chunk: Chunk) = chunks[chunk.id]

    /**
     * @param source The chunk to be copied
     * @param target The chunk things will be copied to
     */
    fun set(source: Chunk, target: Chunk = source, rotation: Int = 0) {
        chunks[source.id] = target.rotatedId(rotation) to target.region.id
        update(source, target, rotation)
    }

    fun remove(chunk: Chunk) {
        chunks.remove(chunk.id)
        update(chunk, chunk, 0)
    }

    private fun update(source: Chunk, target: Chunk, rotation: Int) {
        objects.clear(target)
        for (tile in target.toRectangle()) {
            collisions[tile.x, tile.y, tile.plane] = 0
        }
        extract.loadChunk(source, target, rotation)
        World.events.emit(ReloadChunk(source))
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

        private fun toChunkPosition(chunkX: Int, chunkY: Int, plane: Int): Int {
            return chunkY + (chunkX shl 14) + (plane shl 28)
        }

        private fun toRotatedChunkPosition(chunkX: Int, chunkY: Int, plane: Int, rotation: Int): Int {
            return rotation shl 1 or (plane shl 24) or (chunkX shl 14) or (chunkY shl 3)
        }

    }
}

val instanceModule = module {
    single { DynamicChunks(get(), get(), get()) }
}