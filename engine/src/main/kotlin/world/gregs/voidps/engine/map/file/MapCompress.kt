package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
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
        val writer = BufferWriter(22_000_000)
        writer.startBitAccess()
        writer.writeBits(12, regionCount())
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                if (compressed(writer, x, y)) {
                    count++
                }
            }
        }
        writer.finishBitAccess()
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
        writer.writeBits(16, region.id)
        compressWaterTiles(writer, region)
        compressObjects(writer, def)
        return true
    }

    private fun compressWaterTiles(writer: Writer, region: Region) {
        for (plane in 0 until 4) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    val water = collisions.check(region.tile.x + x, region.tile.y + y, plane, CollisionFlag.WATER)
                    writer.writeBits(1, water)
                }
            }
        }
    }

    private fun compressObjects(writer: Writer, def: MapDefinition) {
        writer.writeBits(14, def.objects.size)
        for (location in def.objects) {
            writer.writeBits(16, location.id)
            writer.writeBits(6, location.x)
            writer.writeBits(6, location.y)
            writer.writeBits(2, location.plane)
            writer.writeBits(5, location.type)
            writer.writeBits(3, location.rotation)
        }
    }
}