package org.redrune.cache

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.math.BigInteger

/**
 * @author Tyluur <contact@kiaira.tech>
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since 2020-01-07
 */

val cacheModule = module {
    single { Cache(getProperty("cachePath"), getProperty<String>("fsRsaPrivate"), getProperty<String>("fsRsaModulus")) }
}

class Cache(directory: String, exponent: BigInteger, modulus: BigInteger) : CacheLibrary(directory) {

    constructor(directory: String, exponent: String, modulus: String) : this(directory, BigInteger(exponent, 16), BigInteger(modulus, 16))

    private val logger = InlineLogger()

    private val versionTable = generateNewUkeys(exponent, modulus)

    init {
        logger.info { "Cache read from $path" }
    }

    fun getFile(indexId: Int, archiveId: Int): ByteArray? {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        val index = if (indexId == 255) index255 else index(indexId)
        if (index == null) {
            logger.warn { "Unable to find valid index for file request [indexId=$indexId, archiveId=$archiveId]}" }
            return null
        }
        val archiveSector = index.readArchiveSector(archiveId)
        if (archiveSector == null) {
            logger.warn { "Unable to read archive sector $archiveId in index $indexId" }
            return null
        }
        return archiveSector.data
    }

    fun valid(index: Int, archive: Int): Boolean {
        if (archive < 0) {
            return false
        }
        if (index != 255) {
            if (!exists(index) || index(index).archive(archive) == null) {
                return false
            }
        } else if (archive != 255) {
            if (!index(index).contains(archive)) {
                return false
            }
        }
        return true
    }

}