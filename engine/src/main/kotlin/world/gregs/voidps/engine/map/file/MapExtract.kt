package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.utility.plural
import java.io.File
import java.io.RandomAccessFile

/**
 * Loads map collision and objects from the [raf] created by [MapCompress]
 */
class MapExtract(
    private val collisions: Collisions,
    private val loader: MapObjectLoader
) {
    private val logger = InlineLogger()
    private val indices = mutableMapOf<Chunk, Pair<Int, Int>>()
    private val regions = mutableMapOf<Region, Pair<Int, Int>>()
    private lateinit var raf: RandomAccessFile

    fun loadMap(file: File) {
        val start = System.currentTimeMillis()
        val reader = BufferReader(file.readBytes())
        val regionCount = reader.readShort()
        repeat(regionCount) {
            val id = reader.readShort()
            val region = Region(id)
            val startPosition = reader.position()
            for (chunk in region.toCuboid().toChunks()) {
                val position = reader.position()
                loadChunk(reader, chunk)
                indices[chunk] = position to reader.position() - position
            }
            regions[region] = startPosition to reader.position() - startPosition
        }
        logger.info { "$regionCount ${"region".plural(regionCount)} loaded from file in ${System.currentTimeMillis() - start}ms" }
        raf = RandomAccessFile(file, "r")
    }

    fun loadChunk(source: Chunk, target: Chunk, rotation: Int) {
        val (position, length) = indices[source] ?: return
        val start = System.currentTimeMillis()
        val array = ByteArray(length)
        raf.seek(position.toLong())
        raf.read(array)
        val reader = BufferReader(array)
        loadChunk(reader, target, rotation)
        logger.info { "$target loaded in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadChunk(reader: Reader, chunk: Chunk, rotation: Int = 0) {
        reader.startBitAccess()
        decompressWaterTiles(reader, chunk, rotation)
        decompressObjects(reader, chunk, rotation)
        reader.finishBitAccess()
    }

    private fun decompressWaterTiles(reader: Reader, chunk: Chunk, rotation: Int) {
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                if (reader.readBits(1) == 1) {
                    collisions.add(
                        x = chunk.tile.x + rotateX(x, y, rotation),
                        y = chunk.tile.y + rotateY(x, y, rotation),
                        plane = chunk.plane,
                        flag = CollisionFlag.WATER
                    )
                }
            }
        }
    }

    private fun decompressObjects(reader: Reader, chunk: Chunk, chunkRotation: Int) {
        val objectCount = reader.readBits(8)
        repeat(objectCount) {
            val id = reader.readBits(16)
            val x = reader.readBits(3)
            val y = reader.readBits(3)
            val type = reader.readBits(5)
            val rotation = reader.readBits(3)
            loader.load(chunk, id, x, y, type, (rotation + chunkRotation) and 0x3, chunkRotation)
        }
    }

    companion object {
        private fun rotateX(x: Int, y: Int, rotation: Int): Int {
            return if (rotation == 1) y else if (rotation == 2) 7 - x else if (rotation == 3) 7 - y else x
        }

        private fun rotateY(x: Int, y: Int, rotation: Int): Int {
            return if (rotation == 1) 7 - x else if (rotation == 2) 7 - y else if (rotation == 3) x else y
        }
    }
}