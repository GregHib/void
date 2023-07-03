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
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.active.ActiveIndexEncoder
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region
import java.io.File

class MapEncoder(
    private val xteasPath: String
) : ActiveIndexEncoder(Indices.MAPS) {

    private val logger = InlineLogger()
    private var empty = true
    private var objectCount = 0
    private var tileCount = 0

    override fun encode(writer: Writer, cache: Cache) {
        objectCount = 0
        tileCount = 0
        val lastArchiveId = cache.lastArchiveId(Indices.OBJECTS)
        val objectSize = lastArchiveId * 256 + (cache.archiveCount(Indices.OBJECTS, lastArchiveId))
        val definitions = MapDecoder(loadXteas(xteasPath)).loadCache(cache)
        val start = System.currentTimeMillis()
        val tiles = LongArray(TOTAL_ZONE_COUNT)
        val objects = Int2ObjectOpenHashMap<MutableList<Int>>()
        val chunks = IntOpenHashSet(85_000)
        val full = IntOpenHashSet(18_000)
        val all = IntOpenHashSet()
        val planes = IntOpenHashSet()
        var regions = 0
        for (definition in definitions) {
            val region = Region(definition.id)
            val regionChunkX = region.tile.chunk.x
            val regionChunkY = region.tile.chunk.y
            empty = true
            val emptyPlane = BooleanArray(4) { true }
            for (plane in 0 until 4) {
                for (localX in 0 until 8) {
                    for (localY in 0 until 8) {
                        all.add(Chunk.id(regionChunkX + localX, regionChunkY + localY, plane))
                    }
                }
            }
            for (plane in 0 until 4) {
                for (localX in 0 until 64) {
                    for (localY in 0 until 64) {
                        val blocked = definition.getTile(localX, localY, plane).isTile(BLOCKED_TILE)
                        if (!blocked) {
                            continue
                        }
                        var height = plane
                        val bridge = definition.getTile(localX, localY, 1).isTile(BRIDGE_TILE)
                        if (bridge) {
                            height--
                            if (height < 0) {
                                continue
                            }
                        }
                        tileCount++
                        empty = false
                        emptyPlane[height] = false
                        val chunk = Chunk.id(regionChunkX + (localX shr 3), regionChunkY + (localY shr 3), height)
                        val offset = (localX and 0x7) or ((localY and 0x7) shl 3)
                        tiles[chunk] = tiles[chunk] or (1L shl offset)
                        all.remove(chunk)
                        if (tiles[chunk] == -1L) {
                            full.add(chunk)
                            chunks.remove(chunk)
                        } else {
                            chunks.add(chunk)
                        }
                    }
                }
            }
            for (plane in emptyPlane.indices) {
                if (!emptyPlane[plane]) {
                    continue
                }
                planes.add(region.toPlane(plane).id)
                for (x in 0 until 8) {
                    for (y in 0 until 8) {
                        all.remove(Chunk.id(regionChunkX + x, regionChunkY + y, plane))
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
                objects.getOrPut(tile.chunk.id) { IntArrayList() }.add(ZoneObject.pack(obj.id, tile.x and 0x7, tile.y and 0x7, obj.plane, obj.shape, obj.rotation))
            }
            if (!empty) {
                regions++
            }
        }
        writer.writeInt(regions)
        writeEmptyTiles(writer, all, planes)
        writeTiles(writer, chunks, tiles)
        writeFilledChunks(writer, full)
        writeObjects(writer, objects)
        logger.info { "Compressed $regions maps ($objectCount objects, $tileCount tiles) to ${writer.position() / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun writeEmptyTiles(writer: Writer, all: Set<Int>, planes: Set<Int>) {
        writer.writeInt(planes.size)
        for (plane in planes) {
            writer.writeInt(plane)
        }
        writer.writeInt(all.size)
        for (chunk in all) {
            writer.writeInt(chunk)
        }
    }

    private fun writeTiles(writer: Writer, chunks: Set<Int>, collisions: LongArray) {
        writer.writeInt(chunks.size)
        for (chunk in chunks) {
            writer.writeInt(chunk)
            writer.writeLong(collisions[chunk])
        }
    }

    private fun writeObjects(writer: Writer, objects: Map<Int, List<Int>>) {
        writer.writeInt(objects.size)
        objects.forEach { (chunk, objs) ->
            writer.writeInt(chunk)
            writer.writeShort(objs.size)
            for (obj in objs) {
                writer.writeInt(obj)
            }
        }
    }

    private fun writeFilledChunks(writer: Writer, full: Set<Int>) {
        writer.writeInt(full.size)
        for (chunk in full) {
            writer.writeInt(chunk)
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