package world.gregs.voidps.network.file.type

import io.ktor.utils.io.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.network.file.FileProvider

/**
 * Reads all [Cache] sectors into memory
 * Fast read speed, high memory usage
 */
class MemoryFileProvider(cache: Cache) : FileProvider {

    private val sectors: Array<Array<ByteArray?>?> = arrayOfNulls(256)

    init {
        val index255 = arrayOfNulls<ByteArray>(256)
        sectors[255] = index255
        index255[255] = FileProvider.encode(cache.versionTable)
        for (index in cache.indices()) {
            val archives = arrayOfNulls<ByteArray>(cache.lastArchiveId(index) + 1)
            sectors[index] = archives
            for (archive in cache.archives(index)) {
                val data = cache.sector(index, archive) ?: continue
                archives[archive] = FileProvider.encode(data)
            }
            index255[index] = FileProvider.encode(cache.sector(255, index) ?: continue)
        }
    }

    override fun data(index: Int, archive: Int): ByteArray? = sectors.getOrNull(index)?.getOrNull(archive)

    override suspend fun encode(write: ByteWriteChannel, data: ByteArray) {
        write.writeFully(data, 1, data.size - 1)
    }
}
