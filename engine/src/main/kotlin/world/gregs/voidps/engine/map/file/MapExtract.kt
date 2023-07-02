package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.RegionPlane
import world.gregs.yaml.Yaml
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.set

/**
 * Loads map collision and objects from the [raf] created by [MapCompress]
 */
class MapExtract(
    private val collisions: Collisions,
    private val definitions: ObjectDefinitions,
    private val objects: GameObjects
) {
    private val logger = InlineLogger()
    private val objectIndices: MutableMap<Int, Int> = Int2IntOpenHashMap(70_000)
    private val tileIndices: MutableMap<Int, Int> = Int2IntOpenHashMap(90_000)
    private lateinit var raf: RandomAccessFile
    private val objectArray = ByteArray(2048)
    private val tileArray = ByteArray(12)

    fun loadMap(file: File): MapExtract {
        val start = System.currentTimeMillis()
        val reader = BufferReader(file.readBytes())
        val regions = reader.readInt()
        readEmptyTiles(reader)
        readTiles(reader)
        readFullTiles(reader)
        readObjects(reader)
        logger.info { "Loaded $regions maps ${objects.size} ${"object".plural(objects.size)} from file in ${System.currentTimeMillis() - start}ms" }
        raf = RandomAccessFile(file, "r")
        return this
    }

    private fun readEmptyTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val regionPlane = RegionPlane(reader.readInt())
            val regionX = regionPlane.x
            val regionY = regionPlane.y
            val plane = regionPlane.plane
            for (zoneX in 0 until 64 step 8) {
                for (zoneY in 0 until 64 step 8) {
                    val x = regionX + zoneX
                    val y = regionY + zoneY
                    collisions.allocateIfAbsent(x, y, plane)
                }
            }
        }
        for (i in 0 until reader.readInt()) {
            val chunk = Chunk(reader.readInt()).tile
            collisions.allocateIfAbsent(chunk.x, chunk.y, chunk.plane)
        }
    }

    private fun readTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunkIndex = reader.readInt()
            tileIndices[chunkIndex] = reader.position()
            val value = reader.readLong()
            collisions.flags[chunkIndex] = IntArray(CHUNK_SIZE) {
                if (value ushr it and 0x1 == 1L) CollisionFlag.FLOOR else 0
            }
        }
    }

    private fun readObjects(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunkIndex = reader.readInt()
            objectIndices[chunkIndex] = reader.position()
            for (j in 0 until reader.readShort()) {
                val obj = ZoneObject(reader.readInt())
                val def = definitions.get(obj.id)
                objects.set(obj, chunkIndex, def)
            }
        }
    }

    private fun readFullTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunkIndex = reader.readInt()
            fillTiles(chunkIndex)
        }
    }

    fun loadChunk(from: Chunk, to: Chunk, rotation: Int) {
        val start = System.currentTimeMillis()
        val tilePosition = tileIndices[from.id]?.toLong()
        if (tilePosition == null) {
            fillTiles(to.id)
        } else {
            raf.seek(tilePosition)
            raf.read(tileArray)
            val reader = BufferReader(tileArray)
            readTiles(to, reader, rotation)
        }
        val objectPosition = objectIndices[from.id]?.toLong()
        if (objectPosition != null) {
            raf.seek(objectPosition)
            raf.read(objectArray)
            val reader = BufferReader(objectArray)
            readObjects(to, reader, rotation)
        }
        logger.info { "Loaded $from -> $to $rotation in ${System.currentTimeMillis() - start}ms" }
    }

    private fun readObjects(chunk: Chunk, reader: Reader, chunkRotation: Int) {
        val chunkX = chunk.tile.x
        val chunkY = chunk.tile.y
        for (i in 0 until reader.readShort()) {
            val obj = ZoneObject(reader.readInt())
            val def = definitions.get(obj.id)
            val rotation = (obj.rotation + chunkRotation) and 0x3
            val rotX = chunkX + rotateX(obj.x, obj.y, def.sizeX, def.sizeY, rotation, chunkRotation)
            val rotY = chunkY + rotateY(obj.x, obj.y, def.sizeX, def.sizeY, rotation, chunkRotation)
            objects.set(obj.id, rotX, rotY, obj.plane, obj.shape, rotation, def)
        }
    }

    private fun readTiles(chunk: Chunk, reader: Reader, chunkRotation: Int) {
        val intArray = collisions.allocateIfAbsent(
            absoluteX = chunk.tile.x,
            absoluteZ = chunk.tile.y,
            level = chunk.plane
        )
        val value = reader.readLong()
        for (i in 0 until CHUNK_SIZE) {
            if (value ushr i and 0x1 == 1L) {
                val x = Tile.indexX(i)
                val y = Tile.indexY(i)
                val index = Tile.index(rotateX(x, y, chunkRotation), rotateY(x, y, chunkRotation))
                intArray[index] = intArray[i] or CollisionFlag.FLOOR
            }
        }
    }

    private fun fillTiles(chunkIndex: Int) {
        val array = collisions.flags[chunkIndex]
        if (array == null) {
            collisions.flags[chunkIndex] = IntArray(CHUNK_SIZE) { CollisionFlag.FLOOR }
            return
        }
        array.fill(CollisionFlag.FLOOR)
    }

    companion object {
        private const val CHUNK_SIZE = 64

        private fun rotateX(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x) and 0x7
        }

        private fun rotateY(x: Int, y: Int, rotation: Int): Int {
            return (if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y) and 0x7
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

        @JvmStatic
        fun main(args: Array<String>) {
            val cache = CacheDelegate("./data/cache")
            val definitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).loadCache(cache))
                .load(Yaml(), "./data/definitions/objects.yml", null)
            val collisions = Collisions()
            val objects = GameObjects(GameObjectCollision(collisions), ChunkBatchUpdates(), definitions, storeUnused = true)
            val extract = MapExtract(collisions, definitions, objects)
            extract.loadMap(File("./data/cache/live/index5.dat"))
            println(objects.size)
        }
    }
}