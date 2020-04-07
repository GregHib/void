package org.redrune.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index255
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.math.BigInteger

val cacheModule = module {
    single(createdAtStart = true) { CacheDelegate(getProperty("cachePath"), getProperty<String>("fsRsaPrivate"), getProperty<String>("fsRsaModulus")) as Cache }
}

/**
 * @author Tyluur <contact@kiaira.tech>
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since January 01, 2020
 */
class CacheDelegate(directory: String, exponent: BigInteger, modulus: BigInteger) : Cache {

    private val delegate = CacheLibrary(directory)

    constructor(directory: String, exponent: String, modulus: String) : this(directory, BigInteger(exponent, 16), BigInteger(modulus, 16))

    private val logger = InlineLogger()

    private val versionTable = generateVersionTable(exponent, modulus)

    init {
        logger.info { "Cache read from $directory" }
    }

    override var index255: Index255?
        get() = delegate.index255
        set(value) {
            delegate.index255 = value
        }


    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?) = delegate.data(index, archive, file, xtea)

    override fun getArchive(indexId: Int, archiveId: Int): ByteArray? {
        if (indexId == 255 && archiveId == 255) {
            return versionTable
        }
        val index = if (indexId == 255) index255 else delegate.index(indexId)
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

    override fun generateVersionTable(exponent: BigInteger, modulus: BigInteger) = delegate.generateNewUkeys(exponent, modulus)

    override fun close() = delegate.close()

    override fun getIndexCrc(indexId: Int): Int {
        return delegate.index(indexId).crc
    }

    override fun lastIndexId(indexId: Int): Int {
        return delegate.index(indexId).last()?.id ?: 0
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return delegate.index(indexId).archive(archiveId)?.fileIds()?.size ?: 0
    }
}