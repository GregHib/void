package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import java.io.File
import java.io.RandomAccessFile
import java.lang.ref.WeakReference
import kotlin.collections.set

/**
 * Loads map collision and objects from the [raf] created by [MapCompress]
 */
class MapExtract(
    private val collisions: Collisions,
    private val definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val xteas: Xteas
) {
    private val logger = InlineLogger()
    private val objectIndices: MutableMap<Int, Int> = Int2IntOpenHashMap(140_000)
    private val tileIndices: MutableMap<Int, Int> = Int2IntOpenHashMap(140_000)
    private var fillMarker = 0
    private lateinit var raf: RandomAccessFile
    private val objectArray = ByteArray(2048)
    private val tileArray = ByteArray(12)

    fun loadMap(file: File) {
        var start = System.currentTimeMillis()
        val zones = fillEmptyZones()
        logger.info { "Loaded $zones ${"empty zones".plural(zones)} in ${System.currentTimeMillis() - start}ms" }
        start = System.currentTimeMillis()
        val reader = BufferReader(file.readBytes())
        readObjects(reader)
        readTiles(reader)
        fillMarker = reader.position()
        readFullTiles(reader)
        logger.info { "Loaded ${objects.size} ${"object".plural(objects.size)} from file in ${System.currentTimeMillis() - start}ms" }

        raf = RandomAccessFile(file, "r")
    }

    private fun readTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunk = Chunk(reader.readInt())
            tileIndices[chunk.id] = reader.position()
            val intArray = collisions.allocateIfAbsent(
                absoluteX = chunk.tile.x,
                absoluteZ = chunk.tile.y,
                level = chunk.plane
            )
            val value = reader.readLong()
            for (index in 0 until 64) {
                if (value ushr index and 0x1 == 1L) {
                    intArray[index] = intArray[index] or CollisionFlag.FLOOR
                }
            }
        }
    }


    private fun readObjects(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunkId = reader.readInt()
            objectIndices[chunkId] = reader.position()
            for (j in 0 until reader.readShort()) {
                val obj = ZoneObject(reader.readInt())
                val def = definitions.get(obj.id)
                objects.set(obj, chunkId, def)
            }
        }
    }

    private fun readFullTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val chunk = Chunk(reader.readInt())
            tileIndices[chunk.id] = reader.position()
            fillTiles(chunk)
        }
    }

    private fun fillEmptyZones(): Int {
        var zones = 0
        for (id in xteas.keys) {
            val region = Region(id)
            val regionX = region.tile.x
            val regionY = region.tile.y
            zones += 64
            for (plane in 0 until 4) {
                for (zoneX in 0 until 8) {
                    for (zoneY in 0 until 8) {
                        val x = regionX + zoneX * 8
                        val y = regionY + zoneY * 8
                        collisions.allocateIfAbsent(x, y, plane)
                    }
                }
            }
        }
        return zones
    }

    fun loadChunk(from: Chunk, to: Chunk, rotation: Int) {
        val objectPosition = objectIndices[from.id]?.toLong()
        val tilePosition = tileIndices[from.id]?.toLong()
        val start = System.currentTimeMillis()
        if (objectPosition != null) {
            raf.seek(objectPosition)
            raf.read(objectArray)
            val reader = BufferReader(objectArray)
            readObjects(to, reader, rotation)
        }
        if (tilePosition != null) {
            if (tilePosition > fillMarker) {
                fillTiles(to)
            } else {
                raf.seek(tilePosition)
                raf.read(tileArray)
                val reader = BufferReader(tileArray)
                readTiles(to, reader, rotation)
            }
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
            objects.set(obj.id, rotX, rotY, obj.plane, obj.type, rotation, def)
        }
    }

    private fun readTiles(chunk: Chunk, reader: Reader, chunkRotation: Int) {
        val intArray = collisions.allocateIfAbsent(
            absoluteX = chunk.tile.x,
            absoluteZ = chunk.tile.y,
            level = chunk.plane
        )
        val value = reader.readLong()
        for (i in 0 until 64) {
            if (value ushr i and 0x1 == 1L) {
                val x = i and 0x7
                val y = i shr 3 and 0x7
                val index = (rotateX(x, y, chunkRotation) and 0x7) or ((rotateY(x, y, chunkRotation) and 0x7) shl 3)
                intArray[index] = intArray[i] or CollisionFlag.FLOOR
            }
        }
    }

    private fun fillTiles(chunk: Chunk) {
        collisions.allocateIfAbsent(
            absoluteX = chunk.tile.x,
            absoluteZ = chunk.tile.y,
            level = chunk.plane
        ).fill(CollisionFlag.FLOOR)
    }

    companion object {
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
            val cache = WeakReference(CacheDelegate("./data/cache"))
            val itemDefinitions = ItemDefinitions(ItemDecoder(cache.get()!!))
                .load(FileStorage(), "./data/definitions/items.yml")
            val definitions = ObjectDefinitions(ObjectDecoder(cache.get()!!, true, false))
                .load(FileStorage(), "./data/definitions/objects.yml", itemDefinitions)
            val xteas = Xteas().apply { XteaLoader().load(this, "./data/xteas.dat") }
            cache.clear()
            val collisions = Collisions()
            val objcol = GameObjectCollision(collisions)
            val objects = GameObjects(objcol, ChunkBatchUpdates(), definitions, storeUnused = true)
            val extract = MapExtract(collisions, definitions, objects, xteas)
            extract.loadMap(File("./data/map-test.dat"))
        }
    }
}