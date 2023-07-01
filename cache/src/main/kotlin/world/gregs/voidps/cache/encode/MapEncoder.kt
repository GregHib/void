package world.gregs.voidps.cache.encode

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Checksum
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region

class MapEncoder(
    private val objectDefinitionsSize: Int,
    private val xteas: Map<Int, IntArray>
) : Checksum.IndexEncoder {
    private val logger = InlineLogger()
    private var empty = true
    private var objectCount = 0
    private var tileCount = 0

    override fun encode(writer: Writer, cache: Cache, index: Int) {
        objectCount = 0
        tileCount = 0
        val start = System.currentTimeMillis()
        val tiles = LongArray(TOTAL_ZONE_COUNT)
        val objects = Int2ObjectOpenHashMap<MutableList<Int>>()
        val chunks = IntOpenHashSet()
        val full = IntOpenHashSet()
        var regions = 0
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val id = region.id
                val tileData = cache.getFile(Indices.MAPS, "m${id shr 8}_${id and 0xff}", null) ?: continue
                empty = true
                val bridge = BooleanArray(MAP_SQUARE_TILE_COUNT)
                val buffer = BufferReader(tileData)
                loadTiles(buffer, bridge, region, chunks, full, tiles)
                val objectData = cache.getFile(Indices.MAPS, "l${id shr 8}_${id and 0xff}", xteas[id])!!
                val reader = BufferReader(objectData)
                loadObjects(reader, bridge, region, objects)
                if (!empty) {
                    regions++
                }
            }
        }
        writeObjects(writer, objects)
        writeTiles(writer, chunks, tiles)
        writeFilledChunks(writer, full)
        logger.info { "Compressed $regions maps ($objectCount objects, $tileCount tiles) to ${writer.position() / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadTiles(
        buffer: BufferReader,
        bridge: BooleanArray,
        region: Region,
        chunks: MutableSet<Int>,
        full: MutableSet<Int>,
        tiles: LongArray
    ) {
        val under = mutableListOf<Chunk>()
        val regionChunkX = region.tile.chunk.x
        val regionChunkY = region.tile.chunk.y
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    var config = buffer.readUnsignedByte()
                    if (config == 0) {
                        continue
                    }
                    var settings = 0
                    while (config != 0) {
                        when {
                            config == 1 -> {
                                buffer.skip(1)
                                break
                            }
                            config <= 49 -> buffer.skip(1)
                            config <= 81 -> settings = config - 49
                        }
                        config = buffer.readUnsignedByte()
                    }
                    if (plane == 1 && settings and BRIDGE_TILE == BRIDGE_TILE) {
                        bridge[tileIndex(localX, localY)] = true
                    }
                    if (settings and BLOCKED_TILE == BLOCKED_TILE) {
                        if (plane == 0) {
                            under.add(Chunk(localX, localY, plane))
                        } else {
                            addTile(regionChunkX, regionChunkY, localX, localY, if (bridge[tileIndex(localX, localY)]) plane - 1 else plane, chunks, full, tiles)
                        }
                    }
                }
            }
        }
        for (local in under) {
            if (bridge[tileIndex(local.x, local.y)]) {
                continue
            }
            addTile(regionChunkX, regionChunkY, local.x, local.y, local.plane, chunks, full, tiles)
        }
    }

    private fun addTile(regionChunkX: Int, regionChunkY: Int, localX: Int, localY: Int, height: Int, chunks: MutableSet<Int>, full: MutableSet<Int>, collisions: LongArray) {
        tileCount++
        empty = false
        val chunk = Chunk.id(regionChunkX + (localX shr 3), regionChunkY + (localY shr 3), height)
        val offset = (localX and 0x7) or ((localY and 0x7) shl 3)
        collisions[chunk] = collisions[chunk] or (1L shl offset)
        if (collisions[chunk] == -1L) {
            full.add(chunk)
            chunks.remove(chunk)
        } else {
            chunks.add(chunk)
        }
    }

    private fun loadObjects(
        reader: BufferReader,
        bridge: BooleanArray,
        region: Region,
        objects: MutableMap<Int, MutableList<Int>>
    ) {
        var objectId = -1
        while (true) {
            val skip = reader.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var local = 0
            while (true) {
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                local += loc - 1

                // Data
                val localX = local shr 6 and 0x3f
                val localY = local and 0x3f
                var plane = local shr 12

                // Decrease bridges
                if (bridge[tileIndex(localX, localY)]) {
                    // Validate plane
                    if (plane == 0) {
                        reader.skip(1)
                        continue
                    }
                    plane--
                }

                val details = reader.readUnsignedByte()
                val shape = details shr 2
                val rotation = details and 0x3

                // Valid object
                if (objectId >= objectDefinitionsSize) {
                    logger.info { "Skipped out of bounds object $region $objectId $localX $localY" }
                    continue
                }
                objectCount++
                empty = false
                val tile = region.tile.add(localX, localY)
                val chunkX = tile.chunk.tile.x
                val chunkY = tile.chunk.tile.y
                objects.getOrPut(tile.chunk.id) { IntArrayList() }.add(ZoneObject.pack(objectId, tile.x - chunkX, tile.y - chunkY, plane, shape, rotation))
            }
        }
    }

    private fun writeTiles(writer: Writer, chunks: MutableSet<Int>, collisions: LongArray) {
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

    private fun writeFilledChunks(writer: Writer, full: MutableSet<Int>) {
        writer.writeInt(full.size)
        for (chunk in full) {
            writer.writeInt(chunk)
        }
    }


    companion object {

        private fun tileIndex(localX: Int, localY: Int) = localX + (localY shl 6)

        private const val TOTAL_ZONE_COUNT: Int = 2048 * 2048 * 4
        private const val MAP_SQUARE_TILE_COUNT: Int = 64 * 64
        private const val BLOCKED_TILE = 0x1
        private const val BRIDGE_TILE = 0x2
    }
}