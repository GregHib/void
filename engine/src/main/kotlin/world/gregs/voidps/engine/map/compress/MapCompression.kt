package world.gregs.voidps.engine.map.compress

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.data.MapDefinition
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.utility.plural
import java.io.File

class MapCompression(
    private val path: String,
    private val collisions: Collisions,
    cache: Cache,
    xteas: Xteas
) : Runnable {
    private val decoder = MapDecoder(cache, xteas)
    private val reader = CollisionReader(collisions)

    private val logger = InlineLogger()

    override fun run() {
        val file = File(path)
        if (file.exists()) {
            logger.debug { "Map file found - skipping compression." }
            return
        }
        file.parentFile.mkdirs()
        val start = System.currentTimeMillis()
        var count = 0
        val writer = BufferWriter(50_000_000)
        writer.startBitAccess()
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
        logger.debug { "Maps compressed to ${file.path} - ${data.size / 1000000}mb" }
        logger.info { "$count ${"map".plural(count)} compressed in ${System.currentTimeMillis() - start}ms" }
    }

    private fun compressed(writer: Writer, x: Int, y: Int): Boolean {
        val region = Region(x, y)
        val def = decoder.getOrNull(region.id) ?: return false
        reader.read(region, def)
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