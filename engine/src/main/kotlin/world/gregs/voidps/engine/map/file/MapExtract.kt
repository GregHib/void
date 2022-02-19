package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.utility.plural
import java.io.File

/**
 * Loads map collision and objects from the [file] created by [MapCompress]
 */
class MapExtract(
    private val file: File,
    private val collisions: Collisions,
    private val loader: MapObjectLoader
) : Runnable {
    private val logger = InlineLogger()

    override fun run() {
        val start = System.currentTimeMillis()
        val reader = BufferReader(file.readBytes())
        reader.startBitAccess()
        val regions = reader.readBits(12)
        repeat(regions) {
            val id = reader.readBits(16)
            val region = Region(id)
            decompressWaterTiles(reader, region)
            decompressObjects(reader, region)
        }
        reader.finishBitAccess()
        logger.info { "$regions ${"region".plural(regions)} loaded from file in ${System.currentTimeMillis() - start}ms" }
    }

    private fun decompressWaterTiles(reader: Reader, region: Region) {
        for (plane in 0 until 4) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    if (reader.readBits(1) == 1) {
                        collisions.add(region.tile.x + x, region.tile.y + y, plane, CollisionFlag.WATER)
                    }
                }
            }
        }
    }

    private fun decompressObjects(reader: Reader, region: Region) {
        val objectCount = reader.readBits(14)
        repeat(objectCount) {
            val id = reader.readBits(16)
            val x = reader.readBits(6)
            val y = reader.readBits(6)
            val plane = reader.readBits(2)
            val type = reader.readBits(5)
            val rotation = reader.readBits(3)
            loader.load(region, id, x, y, plane, type, rotation)
        }
    }
}