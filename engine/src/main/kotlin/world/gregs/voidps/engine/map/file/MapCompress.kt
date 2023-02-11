package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.region.Region
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
        val chunks = region.toCuboid().toChunks().map { needsEncoding(it, def) }
        writer.startBitAccess()
        for (encode in chunks) {
            writer.writeBits(1, encode)
        }
        writer.stopBitAccess()
        for (chunk in region.toCuboid().toChunks()) {
            if (!needsEncoding(chunk, def)) {
                continue
            }
            writer.startBitAccess()
            compressWaterTiles(writer, chunk)
            compressObjects(writer, def, chunk.x.rem(8), chunk.y.rem(8), chunk.plane)
            writer.stopBitAccess()
        }
        return true
    }

    private fun needsEncoding(chunk: Chunk, def: MapDefinition): Boolean {
        if (hasWaterTiles(chunk)) {
            return true
        }
        if (def.objects.any { it.x / 8 == chunk.x.rem(8) && it.y / 8 == chunk.y.rem(8) && it.plane == chunk.plane }) {
            return true
        }
        return false
    }

    private fun hasWaterTiles(chunk: Chunk) = (0 until 8).any { x -> (0 until 8).any { y -> collisions.check(chunk.tile.x + x, chunk.tile.y + y, chunk.plane, CollisionFlag.FLOOR) } }

    private fun compressWaterTiles(writer: Writer, chunk: Chunk) {
        val hasWater = hasWaterTiles(chunk)
        writer.writeBits(1, hasWater)
        if (hasWater) {
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    val water = collisions.check(chunk.tile.x + x, chunk.tile.y + y, chunk.plane, CollisionFlag.FLOOR)
                    writer.writeBits(1, water)
                }
            }
        }
    }

    private fun compressObjects(writer: Writer, def: MapDefinition, chunkX: Int, chunkY: Int, plane: Int) {
        val objs = def.objects.filter { it.x / 8 == chunkX && it.y / 8 == chunkY && it.plane == plane }
        writer.writeBits(1, objs.isNotEmpty())
        if (objs.isNotEmpty()) {
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
}