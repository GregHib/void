package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.encode.MapEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class Checksum {

    val logger = InlineLogger()

    fun check(cachePath: String) {
        val cache = File(cachePath)
        if (!cache.exists()) {
            throw IllegalStateException("Unable to find cache.")
        }

        val live = cache.resolve("live/")
        val checksum = live.resolve("cache.checksum")
        val mainFile = cache.resolve("main_file_cache.dat2")
        val index255 = cache.resolve("main_file_cache.idx255")

        if (mainFile.exists() && index255.exists()) {
            if (checksum.exists()) {
                // check crcs for differences
            } else {
                live.mkdir()
                logger.info { "Creating live cache." }
                live(cachePath, live.path, indices, configs)
            }
        } else {
            if (checksum.exists()) {
                logger.info { "Loading live cache." }
            } else {
                throw IllegalStateException("Unable to find cache.")
            }
        }
        /*
            Check data\cache\micro\
                if exists
                    Check data\cache
                        if cache exists
                            read crcs
                            compare crcs
                                update different

         */
    }

    val indices = setOf(
        Indices.CONFIGS,
        Indices.INTERFACES,
        Indices.MAPS,
        Indices.HUFFMAN,
        Indices.CLIENT_SCRIPTS,
        Indices.OBJECTS,
        Indices.ENUMS,
        Indices.NPCS,
        Indices.ITEMS,
        Indices.ANIMATIONS,
        Indices.GRAPHICS,
        Indices.VAR_BIT,
        Indices.QUICK_CHAT_MESSAGES,
        Indices.QUICK_CHAT_MENUS
    )
    val configs = setOf(
        Configs.IDENTITY_KIT,
        Configs.CONTAINERS,
        Configs.VARP,
        Configs.VARC,
        Configs.STRUCTS,
        Configs.RENDER_ANIMATIONS
    )

    interface IndexEncoder {
        fun encode(writer: Writer, cache: Cache, index: Int) {
            for (archiveId in cache.getArchives(index)) {
                val files = cache.getArchiveData(index, archiveId) ?: continue
                for ((fileId, data) in files) {
                    if (data == null) {
                        continue
                    }
                    encode(writer, index, archiveId, fileId, data)
                }
            }
        }

        fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
            writer.writeBytes(data)
        }

    }
    private fun loadXteas(file: File): Map<Int, IntArray> {
        val xteas = Int2ObjectOpenHashMap<IntArray>()
        val reader = BufferReader(file.readBytes())
        while (reader.position() < reader.length) {
            val region = reader.readShort()
            xteas[region] = IntArray(4) { reader.readInt() }
        }
        return xteas
    }

    val encoders = mapOf<Int, IndexEncoder>(
        Indices.MAPS to MapEncoder(loadXteas(File("./data/xteas.dat")))
    )

    fun live(cachePath: String, liveCachePath: String, indices: Set<Int>, configs: Set<Int>) {
        val cache = CacheDelegate(cachePath)
        println(cache.getArchiveId(Indices.MAPS, "m50_50"))
        println(cache.getArchiveId(Indices.MAPS, "l50_50"))
        println("m50_50".hashCode())
        println("l50_50".hashCode())
        val live = File(liveCachePath)
        val mainFile = RandomAccessFile("$cachePath/main_file_cache.dat2", "r")
        val raf = RandomAccessFile("$cachePath/main_file_cache.idx255", "r")
        val writer = BufferWriter(20_000_000)
        for (index in indices) {
            val indexFile = live.resolve("live_file_cache.idx${index}")
            val crc = 0
//            if (CacheReader.crc(mainFile, raf, index) != crc) {
                writer.clear()
                val indexEncoder = encoders[index]
                indexEncoder?.encode(writer, cache, index)
                if (writer.position() > 0) {
                    FileOutputStream(indexFile).use { it.write(writer.array(), 0, writer.position()) }
                }
//            }
        }
    }

    fun loadMicroCache() {

    }

    fun readChecksumFile() {

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Checksum().check("./data/cache/")
        }
    }

}