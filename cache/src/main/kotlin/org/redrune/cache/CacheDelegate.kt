package org.redrune.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index255
import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.io.RandomAccessFile
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

    private val versionTable = generateNewUkeys(exponent, modulus)

    init {
        logger.info { "Cache read from $path" }
    }

    override val path: String
        get() = delegate.path

    override val clearDataAfterUpdate: Boolean
        get() = delegate.clearDataAfterUpdate

    override var mainFile: RandomAccessFile
        get() = delegate.mainFile
        set(value) {
            delegate.mainFile = value
        }
    override var index255: Index255?
        get() = delegate.index255
        set(value) {
            delegate.index255 = value
        }
    override var closed: Boolean
        get() = delegate.closed
        set(value) {
            delegate.closed = value
        }

    override fun reload() = delegate.reload()

    override fun exists(id: Int) = delegate.exists(id)

    override fun index(id: Int) = delegate.index(id)

    override fun data(index: Int, archive: Int, file: Int, xtea: IntArray?) = delegate.data(index, archive, file, xtea)

    override fun generateOldUkeys() = delegate.generateOldUkeys()

    override fun generateNewUkeys(exponent: BigInteger, modulus: BigInteger) = delegate.generateNewUkeys(exponent, modulus)

    override fun close() = delegate.close()

    override fun first() = delegate.first()

    override fun last() = delegate.last()

    override fun is317() = delegate.is317()

    override fun isOSRS() = delegate.isOSRS()

    override fun isRS3() = delegate.isRS3()

    override fun indices() = delegate.indices()

    override fun getFile(indexId: Int, archiveId: Int): ByteArray? {
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

    override fun valid(index: Int, archive: Int): Boolean {
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