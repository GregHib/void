package world.gregs.voidps.engine.map.file

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.map.chunk.Chunk
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Region
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.set

/**
 * Loads map collision and objects from the [raf] created by [MapCompress]
 */
class MapExtract(
    private val collisions: Collisions,
    private val loader: MapObjectLoader
) {
    private val logger = InlineLogger()
    private val indices: MutableMap<Int, Int> = Int2IntOpenHashMap(140_000)
    private lateinit var raf: RandomAccessFile
    private val body = ByteArray(512)

    fun loadMap(file: File) {
        val start = System.currentTimeMillis()
        val reader = BufferReader(file.readBytes())
        val regionCount = reader.readShort()
        val chunks = BooleanArray(256)
        repeat(regionCount) {
            val id = reader.readShort()
            val region = Region(id)
            CollisionReader.allocate(collisions, region)
            var count = 0
            reader.startBitAccess()
            for (chunk in region.toCuboid().toChunks()) {
                chunks[count++] = reader.readBits(1) == 1
            }
            reader.stopBitAccess()
            count = 0
            for (chunk in region.toCuboid().toChunks()) {
                if (!chunks[count++]) {
                    continue
                }
                val position = reader.position()
                if (loadChunk(reader, chunk)) {
                    indices[chunk.id] = position
                }
            }
        }
        raf = RandomAccessFile(file, "r")
        logger.info { "$regionCount ${"region".plural(regionCount)} loaded from file in ${System.currentTimeMillis() - start}ms" }
    }

    fun loadChunk(from: Chunk, to: Chunk, rotation: Int) {
        val position = indices[from.id]?.toLong() ?: return
        val start = System.currentTimeMillis()
        raf.seek(position)
        raf.read(body)
        val reader = BufferReader(body)
        loadChunk(reader, to, rotation)
        logger.info { "$to loaded in ${System.currentTimeMillis() - start}ms" }
    }

    private fun loadChunk(reader: Reader, chunk: Chunk, rotation: Int = 0): Boolean {
        reader.startBitAccess()
        decompressWaterTiles(reader, chunk, rotation)
        decompressObjects(reader, chunk, rotation)
        reader.stopBitAccess()
        return true
    }

    private fun decompressWaterTiles(reader: Reader, chunk: Chunk, rotation: Int) {
        if (reader.readBits(1) == 0) {
            return
        }
        for (x in 0 until 8) {
            for (y in 0 until 8) {
                if (reader.readBits(1) == 1) {
                    collisions.add(
                        absoluteX = chunk.tile.x + rotateX(x, y, rotation),
                        absoluteZ = chunk.tile.y + rotateY(x, y, rotation),
                        level = chunk.plane,
                        mask = CollisionFlag.FLOOR
                    )
                }
            }
        }
    }

    private fun decompressObjects(reader: Reader, chunk: Chunk, chunkRotation: Int) {
        if (reader.readBits(1) == 0) {
            return
        }
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