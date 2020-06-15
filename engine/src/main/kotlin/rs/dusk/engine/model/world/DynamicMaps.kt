package rs.dusk.engine.model.world

import org.koin.dsl.module
import rs.dusk.engine.event.EventBus
import rs.dusk.utility.inject

class DynamicMaps {
    private val bus: EventBus by inject()
    val chunks: MutableMap<Int, Int> = mutableMapOf()
    val regions = mutableSetOf<Int>()

    fun set(dynamic: Chunk, plane: Int, target: Chunk, targetPlane: Int = plane, rotation: Int = 0) {
        chunks[toChunkPosition(dynamic.x, dynamic.y, plane)] =
            toRotatedChunkPosition(target.x, target.y, targetPlane, rotation)
        regions.add(dynamic.region.id)
        bus.emit(ReloadRegion(dynamic.region))
    }

    fun remove(chunk: Chunk, plane: Int) {
        chunks.remove(toChunkPosition(chunk.x, chunk.y, plane))
        val region = chunk.region
        val regionChunk = region.chunk
        var cleared = false
        plane@ for (z in 0 until 4) {
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    if (chunks.containsKey(toChunkPosition(regionChunk.x + x, regionChunk.y + y, z))) {
                        cleared = true
                        break@plane
                    }
                }
            }
        }

        if(!cleared) {
            regions.remove(region.id)
        }
        bus.emit(ReloadRegion(region))
    }

    companion object {

        fun toChunkPosition(chunkX: Int, chunkY: Int, plane: Int): Int {
            return chunkY + (chunkX shl 14) + (plane shl 28)
        }

        fun toRotatedChunkPosition(chunkX: Int, chunkY: Int, plane: Int, rotation: Int): Int {
            return rotation shl 1 or (plane shl 24) or (chunkX shl 14) or (chunkY shl 3)
        }
    }
}

val instanceModule = module {
    single { DynamicMaps() }
}