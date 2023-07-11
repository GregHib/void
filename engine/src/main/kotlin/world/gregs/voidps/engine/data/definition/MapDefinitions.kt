package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveCache
import world.gregs.voidps.cache.active.encode.ZoneObject
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollision
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.set

/**
 * Loads map collision and objects from [ActiveCache] index created by [MapEncoder]
 */
class MapDefinitions(
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
    private var position = 0

    fun loadCache(cache: Cache, xteas: Xteas): MapDefinitions {
        val start = System.currentTimeMillis()
        val maps = MapDecoder(xteas).loadCache(cache)
        val reader = CollisionReader(collisions)
        for (map in maps) {
            val region = Region(map.id)
            reader.read(region, map)
            val regionTileX = region.tile.x
            val regionTileY = region.tile.y
            for (obj in map.objects) {
                val def = definitions.get(obj.id)
                objects.set(obj.id, regionTileX + obj.x, regionTileY + obj.y, obj.level, obj.shape, obj.rotation, def)
            }
        }
        logger.info { "Loaded ${maps.size} maps ${objects.size} ${"object".plural(objects.size)} from cache in ${System.currentTimeMillis() - start}ms" }
        return this
    }

    fun load(directory: File): MapDefinitions {
        val start = System.currentTimeMillis()
        val file = directory.resolve(ActiveCache.indexFile(Index.MAPS))
        val reader = BufferReader(file.readBytes())
        val regions = reader.readInt()
        readEmptyTiles(reader)
        readTiles(reader)
        position = reader.position()
        readFullTiles(reader)
        readObjects(reader)
        logger.info { "Loaded $regions maps ${objects.size} ${"object".plural(objects.size)} from file in ${System.currentTimeMillis() - start}ms" }
        raf = RandomAccessFile(file, "r")
        return this
    }

    private fun readEmptyTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val regionLevel = RegionLevel(reader.readInt())
            val regionX = regionLevel.x
            val regionY = regionLevel.y
            val level = regionLevel.level
            for (zoneX in 0 until 64 step 8) {
                for (zoneY in 0 until 64 step 8) {
                    val x = regionX + zoneX
                    val y = regionY + zoneY
                    collisions.allocateIfAbsent(x, y, level)
                }
            }
        }
        for (i in 0 until reader.readInt()) {
            val zone = Zone(reader.readInt()).tile
            collisions.allocateIfAbsent(zone.x, zone.y, zone.level)
        }
    }

    private fun readTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val zoneIndex = reader.readInt()
            tileIndices[zoneIndex] = reader.position()
            val value = reader.readLong()
            collisions.flags[zoneIndex] = IntArray(ZONE_SIZE) {
                if (value ushr it and 0x1 == 1L) CollisionFlag.FLOOR else 0
            }
        }
    }

    private fun readObjects(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val zoneIndex = reader.readInt()
            objectIndices[zoneIndex] = reader.position()
            for (j in 0 until reader.readShort()) {
                val obj = ZoneObject(reader.readInt())
                val def = definitions.getValue(obj.id)
                objects.set(obj, zoneIndex, def)
            }
        }
    }

    private fun readFullTiles(reader: BufferReader) {
        for (i in 0 until reader.readInt()) {
            val zoneIndex = reader.readInt()
            tileIndices[zoneIndex] = reader.position()
            fillTiles(zoneIndex)
        }
    }

    fun loadZone(from: Zone, to: Zone, rotation: Int) {
        val start = System.currentTimeMillis()
        val tilePosition = tileIndices[from.id]
        if (tilePosition == null) {
            collisions.allocateIfAbsent(
                absoluteX = to.tile.x,
                absoluteZ = to.tile.y,
                level = to.level
            )
        } else if (tilePosition > position) {
            fillTiles(to.id)
        } else {
            raf.seek(tilePosition.toLong())
            raf.read(tileArray)
            val reader = BufferReader(tileArray)
            readTiles(to, reader, rotation)
        }
        val objectPosition = objectIndices[from.id]
        if (objectPosition != null) {
            raf.seek(objectPosition.toLong())
            raf.read(objectArray)
            val reader = BufferReader(objectArray)
            readObjects(to, reader, rotation)
        }
        val took = System.currentTimeMillis() - start
        if (took > 5) {
            logger.info { "Loaded zone $from -> $to $rotation in ${took}ms" }
        }
    }

    private fun readObjects(zone: Zone, reader: Reader, zoneRotation: Int) {
        val zoneTileX = zone.tile.x
        val zoneTileY = zone.tile.y
        for (i in 0 until reader.readShort()) {
            val obj = ZoneObject(reader.readInt())
            val def = definitions.get(obj.id)
            val rotation = (obj.rotation + zoneRotation) and 0x3
            val rotX = zoneTileX + rotateX(obj.x, obj.y, def.sizeX, def.sizeY, rotation, zoneRotation)
            val rotY = zoneTileY + rotateY(obj.x, obj.y, def.sizeX, def.sizeY, rotation, zoneRotation)
            objects.set(obj.id, rotX, rotY, obj.level, obj.shape, rotation, def)
        }
    }

    private fun readTiles(zone: Zone, reader: Reader, zoneRotation: Int) {
        val intArray = collisions.allocateIfAbsent(
            absoluteX = zone.tile.x,
            absoluteZ = zone.tile.y,
            level = zone.level
        )
        val value = reader.readLong()
        for (i in 0 until ZONE_SIZE) {
            if (value ushr i and 0x1 == 1L) {
                val x = Tile.indexX(i)
                val y = Tile.indexY(i)
                val index = Tile.index(rotateX(x, y, zoneRotation), rotateY(x, y, zoneRotation))
                intArray[index] = intArray[i] or CollisionFlag.FLOOR
            }
        }
    }

    private fun fillTiles(zoneIndex: Int) {
        val array = collisions.flags[zoneIndex]
        if (array == null) {
            collisions.flags[zoneIndex] = IntArray(ZONE_SIZE) { CollisionFlag.FLOOR }
            return
        }
        array.fill(CollisionFlag.FLOOR)
    }

    companion object {
        private const val ZONE_SIZE = 64

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
            zoneRotation: Int
        ): Int {
            var x = sizeX
            var y = sizeY
            val rotation = zoneRotation and 0x3
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
            zoneRotation: Int
        ): Int {
            val rotation = zoneRotation and 0x3
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
            val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).loadCache(cache))
                .load(Yaml(), "./data/definitions/objects.yml")
            val collisions = Collisions()
            val objects = GameObjects(GameObjectCollision(collisions), ZoneBatchUpdates(), objectDefinitions, storeUnused = true)
            val mapDefinitions = MapDefinitions(collisions, objectDefinitions, objects)
            mapDefinitions.load(File("./data/cache/active/"))
        }
    }
}