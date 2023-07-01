package world.gregs.voidps.cache.encode

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Checksum
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.data.MapTile
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.encode.CollisionReader.Companion.FLOOR
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region

class MapEncoder(
    val xteas: Map<Int, IntArray>
) : Checksum.IndexEncoder {
    val logger = InlineLogger()
    var previous = MapDefinition()
    val archiveNames = mutableMapOf<Int, Region>()
    val tiles = mutableMapOf<Int, Boolean>()
    val definitions = mutableMapOf<Region, MapDefinition>()
    val objects = mutableMapOf<Chunk, MutableList<ZoneObject>>()
    val chunks = mutableSetOf<Chunk>()
    val full = mutableSetOf<Chunk>()
    val regions = mutableSetOf<Region>()
    var tileCount = 0
    var objectCount = 0

    init {
        println("Xteas: ${xteas.size}")
    }

    /*
        Expected:
        Regions: 1620
        Objects: 3581470
        Tiles: 3181782
        Full: 35312
        Chunks: 31614
     */

    override fun encode(writer: Writer, cache: Cache, index: Int) {
        if (archiveNames.isEmpty()) {
            loadArchiveNames(cache, index)
        }

        val collisions = Collisions()
        val decoder = MapDecoder(cache, xteas)
        val collisionReader = CollisionReader(collisions)
        val start = System.currentTimeMillis()
        var count = 0L
        val writer = BufferWriter(20_000_000)
        var total = 0
        val objects = mutableMapOf<Chunk, MutableList<ZoneObject>>()
        val chunks = mutableSetOf<Chunk>()
        val full = mutableSetOf<Chunk>()
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val def = decoder.getOrNull(region.id) ?: continue
                collisionReader.read(region, def)
                var empty = true
                for (chunk in region.toRectangle().toChunks()) {
                    val tiles = (0 until 8).sumOf { x -> (0 until 8).count { y -> collisions[chunk.tile.x + x, chunk.tile.y + y, chunk.plane] and FLOOR != 0 } }
                    count += tiles
                    if (tiles == 64) {
                        full.add(chunk)
                        empty = false
                    } else if (tiles > 0) {
                        chunks.add(chunk)
                        empty = false
                    }
                }
                for (obj in def.objects) {
                    val size = 57265//definitions.definitions.size
                    if (obj.id >= size) {
                        logger.info { "Skipped out of bounds object $obj $region" }
                        continue
                    }
                    val x = region.tile.x + obj.x
                    val y = region.tile.y + obj.y
                    val tile = Tile(x, y)
                    empty = false
                    val chunkX = tile.chunk.tile.x
                    val chunkY = tile.chunk.tile.y
                    total++
                    objects.getOrPut(tile.chunk) { mutableListOf() }.add(ZoneObject(obj.id, tile.x - chunkX, tile.y - chunkY, obj.plane, obj.shape, obj.rotation))
                }
                if (!empty) {
                    regions.add(region)
                }
            }
        }
        writeObjects(writer, objects)
        writeTiles(writer, chunks, collisions)
        writeFilledChunks(writer, full)
        val data = writer.toArray()
//        file.writeBytes(data)
        println("Regions: ${regions.size}")
        println("Objects: ${total}")
        println("Tiles: ${count}")
        println("Full: ${full.size}")
        println("Chunks: ${chunks.size}")
        logger.info { "Compressed ${regions.size} map ($total objects, $count tiles) to ${data.size / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadArchiveNames(cache: Cache, index: Int) {
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                var name = "l${x}_$y"
                var id = cache.getArchiveId(index, name)
                if (id != -1) {
                    tiles[id] = false
                    archiveNames[id] = Region(x, y)
                }
                name = "m${x}_$y"
                id = cache.getArchiveId(index, name)
                if (id != -1) {
                    tiles[id] = true
                    archiveNames[id] = Region(x, y)
                }
            }
        }
    }

    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        encode(writer, index, archive, file, BufferReader(data))
    }

    fun encode(writer: Writer, index: Int, archive: Int, file: Int, reader: Reader) {
        val region = archiveNames[archive] ?: return
        val tiles = tiles[archive] ?: return
        if (tiles) {
            val definition = MapDefinition()
            definitions[region] = definition
            readTiles(definition, reader, region)
        } else {
            // Assumes all tile maps are packed before objects maps
            readObject(definitions[region]!!, reader, region)
        }
        regions.add(region)
    }


    private fun writeTiles(writer: BufferWriter, chunks: MutableSet<Chunk>, collisions: Collisions) {
        writer.writeInt(chunks.size)
        for (chunk in chunks) {
            writer.writeInt(chunk.id)
            val array = collisions.allocateIfAbsent(
                chunk.tile.x,
                chunk.tile.y,
                chunk.plane
            )
            var long = 0L
            for (i in 0 until 64) {
                if (array[i] and FLOOR != 0) {
                    long = long or (1L shl i)
                }
            }
            writer.writeLong(long)
        }
    }

    fun readTiles(definition: MapDefinition, buffer: Reader, region: Region) {
        val regionX = region.tile.x
        val regionY = region.tile.y
        for (plane in 0 until 4) {
            for (localX in 0 until 64) {
                for (localY in 0 until 64) {
                    var height = 0
                    var attrOpcode = 0
                    var overlayPath = 0
                    var overlayRotation = 0
                    var overlayId = 0
                    var settings = 0
                    var underlayId = 0
                    loop@ while (true) {
                        val config = buffer.readUnsignedByte()
                        if (config == 0) {
                            break@loop
                        } else if (config == 1) {
                            height = buffer.readUnsignedByte()
                            break@loop
                        } else if (config <= 49) {
                            attrOpcode = config
                            overlayId = buffer.readUnsignedByte()
                            overlayPath = (config - 2) / 4
                            overlayRotation = 3 and (config - 2)
                        } else if (config <= 81) {
                            settings = config - 49
                        } else {
                            underlayId = (config - 81) and 0xff
                        }
                    }
                    if (height != 0 || attrOpcode != 0 || overlayPath != 0 || overlayRotation != 0 || overlayId != 0 || settings != 0 || underlayId != 0) {
                        definition.setTile(localX, localY, plane, MapTile(
                            height,
                            attrOpcode,
                            overlayId,
                            overlayPath,
                            overlayRotation,
                            settings,
                            underlayId
                        ))
                        val blocked = definition.getTile(localX, localY, plane).isTile(BLOCKED_TILE)
                        if (!blocked) {
                            continue
                        }
                        var height = plane
                        val bridge = definition.getTile(localX, localY, 1).isTile(BRIDGE_TILE)
                        if (bridge) {
                            height--
                        }
                        if (height >= 0) {
                            definition.setCol(regionX + localX, regionY + localY, plane)
                        }
                    }
                }
            }
        }
        var empty = true
        for (chunk in region.toRectangle().toChunks()) {
            val tiles = (0 until 8).sumOf { x -> (0 until 8).count { y -> definition.getCol(chunk.tile.x + x, chunk.tile.y + y, chunk.plane) } }
            tileCount += tiles
            if (tiles == 64) {
                full.add(chunk)
                empty = false
            } else if (tiles > 0) {
                chunks.add(chunk)
                empty = false
            }
        }
        if(!empty) {
            regions.add(region)
        }
    }

    fun readObject(definition: MapDefinition, reader: Reader, region: Region) {
        var empty = true
        var objectId = -1
        while (true) {
            val skip = reader.readLargeSmart()
            if (skip == 0) {
                break
            }
            objectId += skip
            var tile = 0
            while (true) {
                val loc = reader.readSmart()
                if (loc == 0) {
                    break
                }
                tile += loc - 1

                // Data
                val localX = tile shr 6 and 0x3f
                val localY = tile and 0x3f
                var plane = tile shr 12
                val obj = reader.readUnsignedByte()

                // Decrease bridges
                if (definition.getTile(localX, localY, 1).isTile(BRIDGE_TILE)) {
                    plane--
                }

                // Validate plane
                if (plane !in 0 until 4) {
                    continue
                }

                val shape = obj shr 2
                val rotation = obj and 0x3

                // Valid object
                if (objectId < 57265) {
                    val x = region.tile.x + localX
                    val y = region.tile.y + localY
                    val tile = Tile(x, y)
                    empty = false
                    val chunkX = tile.chunk.tile.x
                    val chunkY = tile.chunk.tile.y
                    objectCount++
                    objects.getOrPut(tile.chunk) { mutableListOf() }.add(ZoneObject(objectId, tile.x - chunkX, tile.y - chunkY, plane, shape, rotation))
//                objects.add(MapObject(objectId, localX, localY, plane, type, rotation))
                }
                if (!empty) {
                    regions.add(region)
                }
            }
        }
    }

    private fun writeObjects(writer: Writer, objects: Map<Chunk, List<ZoneObject>>) {
        writer.writeInt(objects.size)
        objects.forEach { (chunk, objs) ->
            writer.writeInt(chunk.id)
            writer.writeShort(objs.size)
            for (obj in objs) {
                writer.writeInt(obj.packed)
            }
        }
    }

    private fun writeTiles(writer: Writer, chunks: MutableSet<Chunk>) {
        writer.writeInt(chunks.size)
        for (chunk in chunks) {
            writer.writeInt(chunk.id)
            val array = definitions[chunk.region]!!.getArray(chunk.tile.x, chunk.tile.y, chunk.plane)!!
            var long = 0L
            for (i in 0 until 64) {
                if (array[i]) {
                    long = long or (1L shl i)
                }
            }
            writer.writeLong(long)
        }
    }

    private fun writeFilledChunks(writer: Writer, full: MutableSet<Chunk>) {
        writer.writeInt(full.size)
        for (chunk in full) {
            writer.writeInt(chunk.id)
        }
    }

    companion object {
        private const val BLOCKED_TILE = 0x1
        private const val BRIDGE_TILE = 0x2
    }
}