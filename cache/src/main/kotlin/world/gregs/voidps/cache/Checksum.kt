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
    private val encoders: () -> List<IndexEncoder>
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
            val crc = CRC(RandomAccessFile(mainFile.path, "r"), RandomAccessFile(index255.path, "r"))
            if (checksum.exists()) {
                val outdated = readChecksum(checksum, live, encoders, crc)
                if (outdated > 0) {
                    update(cachePath, live, encoders, crc)
                }
            } else {
                live.mkdir()
                logger.info { "Creating live cache." }
                update(cachePath, live, encoders, crc)
            }
            crc.close()
        } else if (!checksum.exists()) {
            throw IllegalStateException("Unable to find cache at '${cachePath}'.")
        }
    }

    /**
     * Compares all values in [checksumFile] with current [liveDirectory] and cache [main], [index255]
     *
     */
    private fun readChecksum(
        checksumFile: File,
        liveDirectory: File,
        indices: List<IndexEncoder>,
        crc32: CRC
    ): Int {
        val start = System.currentTimeMillis()
        val reader = BufferReader(checksumFile.readBytes())
        if (reader.readByte() != VERSION) {
            logger.info { "Checksum file out of date. Refreshing all." }
            return indices.size
        }
        val size = reader.readByte()
        var outdated = 0
        for (i in 0 until size) {
            val index = reader.readByte()
            val config = reader.readByte()
            val crc = reader.readInt()
            val md5 = reader.readString()
            val encoder = indices.firstOrNull { it.index == index && it.config == config }
            if (encoder == null) {
                logger.warn { "No encoder found for index $index" }
                continue
            }
            encoder.crc = crc32.read(index)
            if (crc != encoder.crc) {
                logger.debug { "CRC mismatch ${encoder.index}_${encoder.config} Expected: $crc Actual: ${encoder.crc}" }
                outdated++
                continue
            }
            val file = encoder.file(liveDirectory)
            if (!file.exists()) {
                logger.debug { "Missing file ${encoder.index}_${encoder.config} ${file.path}" }
                outdated++
                continue
            }
            encoder.md5 = md5(file.readBytes())
            if (md5 != encoder.md5) {
                logger.debug { "MD5 mismatch ${encoder.index}_${encoder.config} Expected: $md5 Actual: ${encoder.md5}" }
                outdated++
                continue
            }
            encoder.outdated = false
        }
        logger.info { "Found $outdated/$size outdated cache indices in ${System.currentTimeMillis() - start}ms" }
        return outdated
    }

    /**
     * (Re)encodes any [Indices] which are [IndexEncoder.outdated]
     */
    private fun update(cachePath: String, live: File, encoders: List<IndexEncoder>, crc32: CRC) {
        val cache = CacheDelegate(cachePath)
        val writer = BufferWriter(20_000_000)
        for (encoder in encoders) {
            val start = System.currentTimeMillis()
            if (!encoder.outdated) {
                continue
            }
            writer.clear()
            encoder.encode(writer, cache)
            if (writer.position() <= 0) {
                continue
            }
            val file = encoder.file(live)
            val bytes = writer.toArray()
            file.writeBytes(bytes)
            encoder.md5 = md5(bytes)
            encoder.crc = crc32.read(encoder.index)
            logger.info { "Encoded index ${encoder.index}_${encoder.config} in ${System.currentTimeMillis() - start}ms" }
        }
        writeChecksum(live.resolve(CHECKSUM_FILE), encoders)
    }

    /**
     * Write all the latest checksum values to file
     */
    private fun writeChecksum(file: File, encoders: List<IndexEncoder>) {
        val writer = BufferWriter(encoders.sumOf { it.md5.length + 7 } + 2)
        writer.writeByte(VERSION)
        writer.writeByte(encoders.size)
        for (encoder in encoders) {
            writer.writeByte(encoder.index)
            writer.writeByte(encoder.config)
            writer.writeInt(encoder.crc)
            writer.writeString(encoder.md5)
        }
        file.writeBytes(writer.array())
    }

    companion object {
        private const val CHECKSUM_FILE = "checksum.dat"
        private const val VERSION = 1
        private const val OBJECT_DEF_SIZE = 57265

        private fun md5(bytes: ByteArray?): String {
            val hash = MessageDigest.getInstance("MD5").digest(bytes)
            return BigInteger(1, hash).toString(16)
        }

        private fun load(): List<IndexEncoder> {
            return listOf(
                ConfigEncoder(Configs.IDENTITY_KIT),
                ConfigEncoder(Configs.CONTAINERS),
                ConfigEncoder(Configs.VARP),
                ConfigEncoder(Configs.VARC),
                ConfigEncoder(Configs.STRUCTS),
                ConfigEncoder(Configs.RENDER_ANIMATIONS),
                InterfaceEncoder(),
                MapEncoder(OBJECT_DEF_SIZE, "./data/xteas.dat"),
                HuffmanEncoder(),
                ClientScriptEncoder(),
                ShiftEncoder(Indices.OBJECTS, 8),
                ShiftEncoder(Indices.ENUMS, 8),
                ShiftEncoder(Indices.NPCS, 7),
                ShiftEncoder(Indices.ITEMS, 8),
                ShiftEncoder(Indices.ANIMATIONS, 7),
                ShiftEncoder(Indices.GRAPHICS, 8),
                QuickChatEncoder(),
            )
        }

        @JvmStatic
        fun main(args: Array<String>) {
            Checksum(::load).checkChanges("./data/cache/")
        }
    }

}