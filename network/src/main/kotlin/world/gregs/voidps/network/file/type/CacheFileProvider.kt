package world.gregs.voidps.network.file.type

import io.ktor.utils.io.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.file.FileProvider
import world.gregs.voidps.network.file.FileProvider.Companion.LARGEST_BLOCK
import world.gregs.voidps.network.file.FileProvider.Companion.OFFSET
import world.gregs.voidps.network.file.FileProvider.Companion.SEPARATOR
import world.gregs.voidps.network.file.FileProvider.Companion.SPLIT
import world.gregs.voidps.network.file.FileProvider.Companion.getInt
import kotlin.math.min

/**
 * Reads sectors directly from the [cache]
 * Average read speeds, low memory usage
 */
class CacheFileProvider(private val cache: Cache) : FileProvider {

    override fun data(index: Int, archive: Int): ByteArray? {
        if (index == 255 && archive == 255) {
            return cache.versionTable
        }
        return cache.sector(index, archive)
    }

    override suspend fun encode(write: ByteWriteChannel, data: ByteArray) {
        val compression = data[0].toInt()
        val size = getInt(data[1], data[2], data[3], data[4]) + if (compression != 0) 8 else 4
        var length = min(size, LARGEST_BLOCK)
        write.writeFully(data, OFFSET, length)
        var written = length
        while (written < size) {
            write.writeByte(SEPARATOR)
            length = if (size - written < SPLIT) size - written else SPLIT - 1
            write.writeFully(data, written + OFFSET, length)
            written += length
        }
    }
}
