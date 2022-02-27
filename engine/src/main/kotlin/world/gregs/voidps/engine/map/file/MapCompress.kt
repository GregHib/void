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
 * Writes all map collision and objects into a [mapFile] for faster load times via [MapExtract].
 */
class MapCompress(
    private val mapFile: File,
    private val indexFile: File,
    private val collisions: Collisions,
    private val decoder: MapDecoder
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        val start = System.currentTimeMillis()
        var count = 0
        val indices = BufferWriter(20_000)
        val writer = BufferWriter(22_000_000)
        val regions = regionCount()
        writer.writeShort(regions)
        indices.writeShort(regions)
        for (x in 0 until 256) {
            for (y in 0 until 256) {
                if (compressed(writer, indices, x, y)) {
                    count++
                }
            }
        }
        val data = writer.toArray()
        mapFile.writeBytes(data)
        indexFile.writeBytes(indices.toArray())
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

    private fun compressed(writer: Writer, indices: Writer, x: Int, y: Int): Boolean {
        val region = Region(x, y)
        val def = decoder.getOrNull(region.id) ?: return false
        val start = writer.position()
        // TODO test how much extra space it would take to load by chunk plane
        writer.startBitAccess()
        writer.writeBits(16, region.id)
        compressWaterTiles(writer, region)
        compressObjects(writer, def)
        writer.finishBitAccess()

        if(region.id == 12850) {
            println("Objects: ${def.objects.size} - $start ${writer.position()}")
        }
        indices.writeShort(region.id)
        indices.writeInt(start)
        indices.writeInt(writer.position() - start)
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