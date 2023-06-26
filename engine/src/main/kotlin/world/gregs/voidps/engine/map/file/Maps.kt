package world.gregs.voidps.engine.map.file

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.collision.CollisionReader
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.Xteas
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest


class Maps(
    cache: Cache,
    xteas: Xteas,
    private val definitions: ObjectDefinitions,
    private val objects: GameObjects,
    private val mapExtract: MapExtract,
    private val collisions: Collisions
) {
    private val crc = cache.getIndexCrc(Indices.MAPS)
    private val decoder = MapDecoder(cache, xteas)

    fun load(mapPath: String = getProperty("mapPath"), checksumPath: String = getProperty("mapChecksum")) {
        val checkSumFile = File(checksumPath)
        val mapFile = File(mapPath)
        if (compress(checkSumFile, mapFile)) {
            cacheLoad()
            compress(mapFile)
            writeChecksumFile(checkSumFile, crc, md5(mapFile))
        } else {
            mapExtract.loadMap(mapFile)
        }
    }

    private fun compress(checksumFile: File, mapFile: File): Boolean {
        if (!checksumFile.exists() || !mapFile.exists()) {
            return true
        } else {
            val reader = BufferReader(checksumFile.readBytes())
            if (reader.length < 4) {
                return true
            }
            val version = reader.readShort()
            if (version != VERSION) {
                return true
            }
            val expectedCrc = reader.readInt()
            if (expectedCrc != crc) {
                return true
            }
            val expectedMd5 = reader.readString()
            return expectedMd5 != md5(mapFile)
        }
    }

    private fun cacheLoad() {
        MapLoader(decoder, CollisionReader(collisions), definitions, objects).run()
    }

    private fun compress(map: File) {
        MapCompress(map, collisions, decoder, definitions).run()
    }

    private fun md5(file: File): String {
        val hash = MessageDigest.getInstance("MD5").digest(file.readBytes())
        return BigInteger(1, hash).toString(16)
    }

    private fun writeChecksumFile(checksumFile: File, crc: Int, md5: String) {
        val writer = BufferWriter(md5.length + 7)
        writer.writeShort(VERSION)
        writer.writeInt(crc)
        writer.writeString(md5)
        checksumFile.writeBytes(writer.array())
    }

    companion object {
        private const val VERSION = 1
    }
}