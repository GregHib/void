package rs.dusk.engine.map.chunk

import org.koin.dsl.module
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.area.area
import rs.dusk.engine.map.region.RegionPlane
import rs.dusk.engine.map.region.RegionReader

class DynamicChunks(private val bus: EventBus, private val objects: Objects, private val reader: RegionReader) {
    val chunks: MutableMap<Int, Int> = mutableMapOf()

    /**
     * @param source The chunk to be copied
     * @param target The chunk things will be copied to
     */
    fun set(source: Chunk, target: Chunk, rotation: Int = 0) {
        chunks[source.id] = target.rotatedId(rotation)
        val sourceObjs = objects.getStatic(source)// Just in-case source is target
        // Clear target
        clearObjects(target)

        // Spawn objs from source in target
        sourceObjs?.forEach { gameObject ->
            val local = gameObject.tile.minus(gameObject.tile.chunk.tile)
            val size = gameObject.size
            val rotatedX = rotateX(
                local.x,
                local.y,
                size.width,
                size.height,
                gameObject.rotation,
                rotation
            )
            val rotatedY = rotateY(
                local.x,
                local.y,
                size.width,
                size.height,
                gameObject.rotation,
                rotation
            )
            val tile = target.tile.add(rotatedX, rotatedY)
            val rotatedObject = gameObject.copy(tile = tile, rotation = gameObject.rotation + rotation and 0x3)
            objects.add(rotatedObject)
            bus.emit(Registered(rotatedObject))
        }
        bus.emit(ReloadChunk(source))
    }

    fun remove(chunk: Chunk) {
        chunks.remove(chunk.id)
        val region = chunk.region
        if(isRegionCleared(chunk.regionPlane)) {
            reader.loading.remove(region)
        }
        clearObjects(chunk)
        bus.emit(ReloadChunk(chunk))
    }

    private fun isRegionCleared(region: RegionPlane): Boolean {
        for(regionChunk in region.chunk.area(width = 8, height = 8)) {
            if (chunks.containsKey(regionChunk.id)) {
                return false
            }
        }
        return true
    }

    fun clearObjects(chunkPlane: Chunk) {
        objects.getStatic(chunkPlane)?.forEach {
            bus.emit(Unregistered(it))
        }
        objects.clear(chunkPlane)
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

        fun toChunkPosition(chunkX: Int, chunkY: Int, plane: Int): Int {
            return chunkY + (chunkX shl 14) + (plane shl 28)
        }

        fun toRotatedChunkPosition(chunkX: Int, chunkY: Int, plane: Int, rotation: Int): Int {
            return rotation shl 1 or (plane shl 24) or (chunkX shl 14) or (chunkY shl 3)
        }

        private fun rotateX(
            objX: Int,
            objY: Int,
            sizeX: Int,
            sizeY: Int,
            objRotation: Int,
            chunkRotation: Int
        ): Int {
            var x = sizeX
            var y = sizeY
            val rotation = chunkRotation and 0x3
            if (objRotation and 0x1 == 1) {
                val temp = x
                x = y
                y = temp
            }
            if (rotation == 0) {
                return objX
            }
            if (rotation == 1) {
                return objY
            }
            return if (rotation == 2) 7 - objX - x + 1 else 7 - objY - y + 1
        }

        private fun rotateY(
            objX: Int,
            objY: Int,
            sizeX: Int,
            sizeY: Int,
            objRotation: Int,
            chunkRotation: Int
        ): Int {
            val rotation = chunkRotation and 0x3
            var x = sizeY
            var y = sizeX
            if (objRotation and 0x1 == 1) {
                val temp = y
                y = x
                x = temp
            }
            if (rotation == 0) {
                return objY
            }
            if (rotation == 1) {
                return 7 - objX - y + 1
            }
            return if (rotation == 2) 7 - objY - x + 1 else objX
        }
    }
}

val instanceModule = module {
    single { DynamicChunks(get(), get(), get()) }
}