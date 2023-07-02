package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.encode.*
import java.io.File
import java.io.RandomAccessFile
import java.math.BigInteger
import java.security.MessageDigest

class Checksum(
    private val encoders: () -> Map<Int, IndexEncoder>
) {

    private val logger = InlineLogger()

    /**
     * Keeps the live cache up to date by checking for any cache modifications
     */
    fun checkChanges(cachePath: String) {
        val cacheDir = File(cachePath)
        if (!cacheDir.exists()) {
            throw IllegalStateException("Unable to find cache.")
        }

        val live = cacheDir.resolve("live/")
        val checksum = live.resolve(CHECKSUM_FILE)
        val mainFile = cacheDir.resolve("main_file_cache.dat2")
        val index255 = cacheDir.resolve("main_file_cache.idx255")

        if (mainFile.exists() && index255.exists()) {
            val encoders = encoders()
            if (checksum.exists()) {
                val outdated = RandomAccessFile(mainFile.path, "r").use { main ->
                    RandomAccessFile(index255.path, "r").use { index ->
                        readChecksum(main, index, checksum, live, encoders)
                    }
                }
                if (outdated > 0) {
                    update(cachePath, live, encoders)
                }
            } else {
                live.mkdir()
                logger.info { "Creating live cache." }
                update(cachePath, live, encoders)
            }
        } else if (!checksum.exists()) {
            throw IllegalStateException("Unable to find cache at '${cachePath}'.")
        }
    }

    /**
     * Compares all values in [checksumFile] with current [liveDirectory] and cache [main], [index255]
     *
     */
    private fun readChecksum(
        main: RandomAccessFile,
        index255: RandomAccessFile,
        checksumFile: File,
        liveDirectory: File,
        indices: Map<Int, IndexEncoder>
    ): Int {
        val start = System.currentTimeMillis()
        val reader = BufferReader(checksumFile.readBytes())
        if (reader.readByte() != VERSION) {
            logger.info { "Checksum file out of date. Refreshing all." }
            return indices.size
        }
        val crc32 = CRC()
        val size = reader.readByte()
        var outdated = 0
        for (i in 0 until size) {
            val index = reader.readByte()
            val crc = reader.readInt()
            val md5 = reader.readString()
            val encoder = indices[index]
            if (encoder == null) {
                logger.warn { "No encoder found for index $index" }
                continue
            }
            val actualCrc = crc32.read(main, index255, index)
            if (crc != actualCrc) {
                outdated++
                continue
            }
            val file = liveDirectory.resolve(indexFile(index))
            if (!file.exists()) {
                outdated++
                continue
            }
            val actualMd5 = md5(file.readBytes())
            if (md5 != actualMd5) {
                outdated++
                continue
            }
            encoder.outdated = false
        }
        logger.info { "Found $outdated outdated cache indices in ${System.currentTimeMillis() - start}ms" }
        return outdated
    }

    /**
     * (Re)encodes any [Indices] which are [IndexEncoder.outdated]
     */
    private fun update(cachePath: String, live: File, encoders: Map<Int, IndexEncoder>) {
        val cache = CacheDelegate(cachePath)
        val writer = BufferWriter(20_000_000)
        for ((index, encoder) in encoders) {
            if (!encoder.outdated) {
                continue
            }
            val start = System.currentTimeMillis()
            writer.clear()
            encoder.encode(writer, cache, index)
            if (writer.position() <= 0) {
                continue
            }
            val indexFile = live.resolve(indexFile(index))
            val bytes = writer.toArray()
            indexFile.writeBytes(bytes)
            encoder.md5 = md5(bytes)
            encoder.crc = cache.getIndexCrc(index)
            logger.info { "Encoded index $index in ${System.currentTimeMillis() - start}ms" }
        }
        writeChecksum(live.resolve(CHECKSUM_FILE), encoders)
    }

    /**
     * Write all the latest checksum values to file
     */
    private fun writeChecksum(file: File, encoders: Map<Int, IndexEncoder>) {
        val writer = BufferWriter(encoders.values.sumOf { it.md5.length + 6 } + 2)
        writer.writeByte(VERSION)
        writer.writeByte(encoders.size)
        for ((index, encoder) in encoders) {
            writer.writeByte(index)
            writer.writeInt(encoder.crc)
            writer.writeString(encoder.md5)
        }
        file.writeBytes(writer.array())
    }

    companion object {
        private fun indexFile(index: Int) = "index$index.dat"

        private const val CHECKSUM_FILE = "checksum.dat"
        private const val VERSION = 1
        private const val OBJECT_DEF_SIZE = 57265

        private fun md5(bytes: ByteArray?): String {
            val hash = MessageDigest.getInstance("MD5").digest(bytes)
            return BigInteger(1, hash).toString(16)
        }

        private fun load(): Map<Int, IndexEncoder> {
            return mapOf(
                Indices.CONFIGS to ConfigEncoder(setOf(
                    Configs.IDENTITY_KIT,
                    Configs.CONTAINERS,
                    Configs.VARP,
                    Configs.VARC,
                    Configs.STRUCTS,
                    Configs.RENDER_ANIMATIONS
                )),
                Indices.INTERFACES to InterfaceEncoder(),
                Indices.MAPS to MapEncoder(OBJECT_DEF_SIZE, "./data/xteas.dat"),
                Indices.HUFFMAN to IndexEncoder(),
                Indices.CLIENT_SCRIPTS to ClientScriptEncoder(),
                Indices.OBJECTS to ShiftEncoder(8),
                Indices.ENUMS to ShiftEncoder(8),
                Indices.NPCS to ShiftEncoder(7),
                Indices.ITEMS to ShiftEncoder(8),
                Indices.ANIMATIONS to ShiftEncoder(7),
                Indices.GRAPHICS to ShiftEncoder(8),
                Indices.QUICK_CHAT_MESSAGES to QuickChatEncoder(),
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            Checksum(::load).checkChanges("./data/cache/")
        }
    }

}