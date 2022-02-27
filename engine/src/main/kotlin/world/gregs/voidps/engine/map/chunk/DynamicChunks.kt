package world.gregs.voidps.engine.map.chunk

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.file.MapExtract
import world.gregs.voidps.engine.map.region.RegionPlane
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class DynamicChunks(
    private val objects: Objects,
    private val collisions: Collisions,
    private val definitions: ObjectDefinitions,
    private val factory: GameObjectFactory,
    private val collision: GameObjectCollision,
    private val extract: MapExtract
) {
    val chunks: MutableMap<Int, Int> = mutableMapOf()
    private val collisionBackup: MutableMap<Int, IntArray?> = mutableMapOf()
    private val objectBackup: MutableMap<Int, List<GameObject>?> = mutableMapOf()

    /**
     * @param source The chunk to be copied
     * @param target The chunk things will be copied to
     */
    fun set(source: Chunk, target: Chunk, rotation: Int = 0) {
        chunks[source.id] = target.rotatedId(rotation)
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                collisions[target.tile.x + x, target.tile.y + y, target.tile.plane] = 0
            }
        }
        extract.loadChunk(source, target, rotation)
//        clearCollision(source, target, rotation)
//        setObjects(source, target, rotation)
        World.events.emit(ReloadChunk(source))
    }

    private fun clearCollision(source: Chunk, target: Chunk, rotation: Int) {
        if (!collisionBackup.containsKey(source.regionPlane.id)) {
            collisionBackup[source.regionPlane.id] = collisions.data[source.regionPlane.id]
        }
        collisions.copy(source, target, rotation)
    }

    private fun setObjects(source: Chunk, target: Chunk, rotation: Int) {
        if (!objectBackup.containsKey(target.id)) {
            objectBackup[target.id] = objects.getStatic(target)
        }
        val sourceObjs = objects.getStatic(source)// Just in-case source is target
        // Clear target
        clearObjects(target)

        // Spawn objs from source in target
        sourceObjs?.forEach { gameObject ->
            val local = gameObject.tile.minus(gameObject.tile.chunk.tile)
            val width = gameObject.def.sizeX
            val height = gameObject.def.sizeY
            val rotatedX = rotateX(
                local.x,
                local.y,
                width,
                height,
                gameObject.rotation,
                rotation
            )
            val rotatedY = rotateY(
                local.x,
                local.y,
                width,
                height,
                gameObject.rotation,
                rotation
            )
            val tile = target.tile.add(rotatedX, rotatedY)
            val rotatedObject = gameObject.copy(tile = tile, rotation = gameObject.rotation + rotation and 0x3)
            factory.setup(rotatedObject, definitions.get(gameObject.id))
            objects.add(rotatedObject)
//            collision.modifyCollision(gameObject, GameObjectCollision.ADD_MASK)
            rotatedObject.events.emit(Registered)
        }
    }

    fun remove(chunk: Chunk) {
        chunks.remove(chunk.id)
        clearObjects(chunk)
        val remove = objectBackup.remove(chunk.id)
        if (remove != null) {
            for (obj in remove) {
                objects.add(obj)
//                collision.modifyCollision(obj, GameObjectCollision.ADD_MASK)
                obj.events.emit(Registered)
            }
        }
        if (isRegionCleared(chunk.regionPlane)) {
            collisions.data[chunk.regionPlane.id] = collisionBackup.remove(chunk.regionPlane.id)
        }
        World.events.emit(ReloadChunk(chunk))
    }

    private fun isRegionCleared(region: RegionPlane): Boolean {
        for (regionChunk in region.chunk.toCuboid(width = 8, height = 8).toChunks()) {
            if (chunks.containsKey(regionChunk.id)) {
                return false
            }
        }
        return true
    }

    private fun clearObjects(chunkPlane: Chunk) {
        objects.getStatic(chunkPlane)?.forEach {
            collision.modifyCollision(it, GameObjectCollision.REMOVE_MASK)
            it.events.emit(Unregistered)
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

        private fun toChunkPosition(chunkX: Int, chunkY: Int, plane: Int): Int {
            return chunkY + (chunkX shl 14) + (plane shl 28)
        }

        private fun toRotatedChunkPosition(chunkX: Int, chunkY: Int, plane: Int, rotation: Int): Int {
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
    single { DynamicChunks(get(), get(), get(), get(), get(), get()) }
}