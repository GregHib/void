package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.utility.plural
import java.io.File

/**
 * Writes all map collision and objects into a [file] for faster load times via [MapExtract].
 */
class MapCompress(
    private val file: File,
    private val collisions: Collisions,
    private val decoder: MapDecoder
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        val start = System.currentTimeMillis()
        var count = 0
        val writer = BufferWriter(20_000_000)
        val regions = regionCount()
        writer.writeShort(regions)
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                if (compressed(writer, x, y)) {
                    count++
                }
            }
        }
        val data = writer.toArray()
        file.writeBytes(data)
        logger.info { "$count ${"map".plural(count)} compressed to ${data.size / 1000000}mb in ${System.currentTimeMillis() - start}ms" }
    }

    private fun regionCount(): Int {
        var counter = 0
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                val region = Region(x, y)
                decoder.getOrNull(region.id) ?: continue
                counter++
            }
        }
        return counter
    }

    private fun compressed(writer: Writer, x: Int, y: Int): Boolean {
        val region = Region(x, y)
        val def = decoder.getOrNull(region.id) ?: return false
        writer.writeShort(region.id)
        for (chunk in region.toCuboid().toChunks()) {
            writer.startBitAccess()
            compressWaterTiles(writer, chunk)
            compressObjects(writer, def, chunk.x.rem(8), chunk.y.rem(8), chunk.plane)
            writer.finishBitAccess()
        }
        return true
    }

    private fun compressWaterTiles(writer: Writer, chunk: Chunk) {
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                val water = collisions.check(chunk.tile.x + x, chunk.tile.y + y, chunk.plane, CollisionFlag.WATER)
                writer.writeBits(1, water)
            }
        }
    }

    private fun compressObjects(writer: Writer, def: MapDefinition, chunkX: Int, chunkY: Int, plane: Int) {
        val objs = def.objects.filter { it.x / 8 == chunkX && it.y / 8 == chunkY && it.plane == plane }
        writer.writeBits(8, objs.size)
        for (location in objs) {
            writer.writeBits(16, location.id)
            writer.writeBits(3, location.x.rem(8))
            writer.writeBits(3, location.y.rem(8))
            writer.writeBits(5, location.type)
            writer.writeBits(3, location.rotation)
        }
    }
}