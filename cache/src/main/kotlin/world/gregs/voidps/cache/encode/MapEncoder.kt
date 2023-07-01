package world.gregs.voidps.cache.encode

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Checksum
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.encode.CollisionReader.Companion.FLOOR
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.region.Region

class MapEncoder(
    val xteas: Map<Int, IntArray>
) : Checksum.IndexEncoder {
    val logger = InlineLogger()

    override fun encode(writer: Writer, cache: Cache, index: Int) {
        val start = System.currentTimeMillis()
        val collisions = Collisions()
        val decoder = MapDecoder(cache, xteas)
        val collisionReader = CollisionReader(collisions)
        var count = 0L
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

    private fun writeTiles(writer: Writer, chunks: MutableSet<Chunk>, collisions: Collisions) {
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

    private fun writeFilledChunks(writer: Writer, full: MutableSet<Chunk>) {
        writer.writeInt(full.size)
        for (chunk in full) {
            writer.writeInt(chunk.id)
        }
    }
}