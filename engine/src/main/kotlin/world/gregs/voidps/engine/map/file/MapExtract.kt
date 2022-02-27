package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.utility.plural
import java.io.File
import java.io.RandomAccessFile

/**
 * Loads map collision and objects from the [mapFile] created by [MapCompress]
 */
class MapExtract(
    private val collisions: Collisions,
    private val loader: MapObjectLoader
) {
    private val logger = InlineLogger()
    private val indices = mutableMapOf<Int, Pair<Int, Int>>()
    private lateinit var file: RandomAccessFile

    fun run(mapFile: File, indexFile: File) {
        loadIndices(indexFile)
        loadMap(mapFile)
        file = RandomAccessFile(mapFile, "r")
    }

    private fun loadIndices(indexFile: File) {
        indices.clear()
        val start = System.currentTimeMillis()
        val reader = BufferReader(indexFile.readBytes())
        repeat(reader.readShort()) {
            val region = reader.readShort()
            val position = reader.readInt()
            val length = reader.readInt()
            indices[region] = position to length
        }
        logger.info { "${indices.size} ${"region index".plural(indices.size, "es")} loaded from file in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadMap(mapFile: File) {
        val start = System.currentTimeMillis()
        val reader = BufferReader(mapFile.readBytes())
        val regions = reader.readShort()
        repeat(regions) {
            reader.startBitAccess()
            val id = reader.readBits(16)
            val region = Region(id)
            decompressWaterTiles(reader, region)
            decompressObjects(reader, region)
            reader.finishBitAccess()
        }
        logger.info { "$regions ${"region".plural(regions)} loaded from file in ${System.currentTimeMillis() - start}ms" }
    }

    fun loadChunk(source: Chunk, target: Chunk, rotation: Int) {
        val (position, length) = indices[source.region.id] ?: return
        val array = ByteArray(length)
        file.seek(position.toLong())
        file.read(array, 0, length)
        val reader = BufferReader(array)
        val region = Region(reader.readBits(16))
        decompressWaterTiles(reader, region, source, target)
        decompressObjects(reader, region, source, target)
    }

    private fun decompressWaterTiles(reader: Reader, region: Region, source: Chunk, target: Chunk) {
        val local = source.tile.minus(source.tile.region.tile)
        val cube = local.toCuboid(8, 8)
        for (plane in 0 until 4) {
            for (x in 0 until 64) {
                for (y in 0 until 64) {
                    if (reader.readBits(1) == 1) {
                        if (cube.contains(x, y) && plane == source.plane) {
                            collisions.add(target.tile.x + x.rem(8), target.tile.y + y.rem(8), plane, CollisionFlag.WATER)
                        }
                    }
                }
            }
        }
    }

    private fun decompressObjects(reader: Reader, region: Region, source: Chunk, target: Chunk) {
        val local = source.tile.minus(source.tile.region.tile)
        val cube = local.toCuboid(8, 8)
        val objectCount = reader.readBits(14)
        repeat(objectCount) {
            val id = reader.readBits(16)
            val x = reader.readBits(6)
            val y = reader.readBits(6)
            val plane = reader.readBits(2)
            val type = reader.readBits(5)
            val rotation = reader.readBits(3)
            if (cube.contains(x, y) && plane == source.plane) {
                val tile = Tile(target.tile.x + x.rem(8), target.tile.y + y.rem(8), plane)
                loader.load(id, tile, type, rotation)
            }
        }
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