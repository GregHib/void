package world.gregs.voidps.cache.active.encode

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveIndexEncoder
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import java.io.File

class MapEncoder(
    private val xteasPath: String
) : ActiveIndexEncoder(Index.MAPS) {

    private val logger = InlineLogger()
    private var empty = true
    private var objectCount = 0
    private var tileCount = 0

    override fun encode(writer: Writer, cache: Cache) {
        objectCount = 0
        tileCount = 0
        val lastArchiveId = cache.lastArchiveId(Index.OBJECTS)
        val objectSize = lastArchiveId * 256 + (cache.archiveCount(Index.OBJECTS, lastArchiveId))
        val definitions = MapDecoder(emptyMap()).loadCache(cache)
        val start = System.currentTimeMillis()
        val tiles = LongArray(TOTAL_ZONE_COUNT)
        val objects = Int2ObjectOpenHashMap<MutableList<Int>>()
        val zones = IntOpenHashSet(85_000)
        val full = IntOpenHashSet(18_000)
        val all = IntOpenHashSet()
        val levels = IntOpenHashSet()
        var regions = 0
        for (definition in definitions) {
            val region = Region(definition.id)
            val regionZoneX = region.tile.zone.x
            val regionZoneY = region.tile.zone.y
            empty = true
            for (level in 0 until 4) {
                for (localX in 0 until 8) {
                    for (localY in 0 until 8) {
                        all.add(Zone.id(regionZoneX + localX, regionZoneY + localY, level))
                    }
                }
            }
            val emptyLevels = BooleanArray(4) { true }
            for (level in 0 until 4) {
                for (localX in 0 until 64) {
                    for (localY in 0 until 64) {
                        val blocked = definition.getTile(localX, localY, level).isTile(BLOCKED_TILE)
                        if (!blocked) {
                            continue
                        }
                        var height = level
                        val bridge = definition.getTile(localX, localY, 1).isTile(BRIDGE_TILE)
                        if (bridge) {
                            height--
                            if (height < 0) {
                                continue
                            }
                        }
                        tileCount++
                        empty = false
                        emptyLevels[height] = false
                        val zone = Zone.id(regionZoneX + (localX shr 3), regionZoneY + (localY shr 3), height)
                        val offset = (localX and 0x7) or ((localY and 0x7) shl 3)
                        tiles[zone] = tiles[zone] or (1L shl offset)
                        all.remove(zone)
                        if (tiles[zone] == -1L) {
                            full.add(zone)
                            zones.remove(zone)
                        } else {
                            zones.add(zone)
                        }
                    }
                }
            }
            for (level in emptyLevels.indices) {
                if (!emptyLevels[level]) {
                    continue
                }
                levels.add(region.toLevel(level).id)
                for (x in 0 until 8) {
                    for (y in 0 until 8) {
                        all.remove(Zone.id(regionZoneX + x, regionZoneY + y, level))
                    }
                }
            }
            for (obj in definition.objects) {
                val tile = region.tile.add(obj.x, obj.y)
                objectCount++
                empty = false
                if (obj.id > objectSize) {
                    logger.info { "Skipping $obj" }
                    continue
                }
                objects.getOrPut(tile.zone.id) { IntArrayList() }.add(ZoneObject.pack(obj.id, tile.x and 0x7, tile.y and 0x7, obj.level, obj.shape, obj.rotation))
            }
            if (!empty) {
                regions++
            }
        }
        writer.writeInt(regions)
        writeEmptyTiles(writer, all, levels)
        writeTiles(writer, zones, tiles)
        writeFilledZones(writer, full)
        writeObjects(writer, objects)
        logger.info { "Compressed $regions maps ($objectCount objects, $tileCount tiles) to ${writer.position() / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun writeEmptyTiles(writer: Writer, all: Set<Int>, levels: Set<Int>) {
        writer.writeInt(levels.size)
        for (level in levels) {
            writer.writeInt(level)
        }
        writer.writeInt(all.size)
        for (zone in all) {
            writer.writeInt(zone)
        }
    }

    private fun writeTiles(writer: Writer, zones: Set<Int>, collisions: LongArray) {
        writer.writeInt(zones.size)
        for (zone in zones) {
            writer.writeInt(zone)
            writer.writeLong(collisions[zone])
        }
    }

    private fun writeObjects(writer: Writer, objects: Map<Int, List<Int>>) {
        writer.writeInt(objects.size)
        objects.forEach { (zone, objs) ->
            writer.writeInt(zone)
            writer.writeShort(objs.size)
            for (obj in objs) {
                writer.writeInt(obj)
            }
        }
    }

    private fun writeFilledZones(writer: Writer, full: Set<Int>) {
        writer.writeInt(full.size)
        for (zone in full) {
            writer.writeInt(zone)
        }
    }

    companion object {
        private fun loadXteas(path: String): Map<Int, IntArray> {
            val xteas = Int2ObjectOpenHashMap<IntArray>()
            val reader = BufferReader(File(path).readBytes())
            while (reader.position() < reader.length) {
                val region = reader.readShort()
                xteas[region] = IntArray(4) { reader.readInt() }
            }
            return xteas
        }

        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val BLOCKED_TILE = 0x1
        private const val BRIDGE_TILE = 0x2

        @JvmStatic
        fun main(args: Array<String>) {
            val cache = CacheDelegate("./data/cache")
            val path = "./data/xteas.dat"
            val writer = BufferWriter(20_000_000)
            MapEncoder(path).encode(writer, cache)
            File("./data/test-map-2.dat").writeBytes(writer.toArray())
        }
    }
}