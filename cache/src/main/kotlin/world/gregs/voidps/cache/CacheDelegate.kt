package world.gregs.voidps.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index255
import com.github.michaelbull.logging.InlineLogger
import java.math.BigInteger

class CacheDelegate(directory: String) : Cache {

    private val delegate = CacheLibrary(directory)

    private val logger = InlineLogger()

    init {
        logger.info { "Cache read from $directory" }
    }

    override var index255: Index255?
        get() = delegate.index255
        set(value) {
            delegate.index255 = value
        }

    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?) =
        delegate.data(index, archive, file, xtea)

    override fun getFile(index: Int, name: String, xtea: IntArray?) = delegate.data(index, name, xtea)

    override fun getArchive(indexId: Int, archiveId: Int): ByteArray? {
        val index = if (indexId == 255) index255 else delegate.index(indexId)
        if (index == null) {
            logger.debug { "Unable to find valid index for file request [indexId=$indexId, archiveId=$archiveId]}" }
            return null
        }
        val archiveSector = index.readArchiveSector(archiveId)
        if (archiveSector == null) {
            logger.debug { "Unable to read archive sector $archiveId in index $indexId" }
            return null
        }
        return archiveSector.data
    }

    override fun generateVersionTable(exponent: BigInteger, modulus: BigInteger) = delegate.generateNewUkeys(exponent, modulus)

    override fun close() = delegate.close()

    override fun getIndexCrc(indexId: Int): Int {
        return delegate.index(indexId).crc
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return delegate.index(indexId).archive(archiveId)?.fileIds()?.size ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return delegate.index(indexId).archive(archive)?.last()?.id ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return delegate.index(indexId).last()?.id ?: 0
    }

    override fun getArchiveId(index: Int, name: String): Int {
        return delegate.index(index).archiveId(name)
    }

    override fun getArchiveId(index: Int, hash: Int): Int {
        delegate.index(index).archives().forEach { archive ->
            if (archive.hashName == hash) {
                return archive.id
            }
        }
        return -1
    }

    override fun getArchives(index: Int): IntArray {
        return delegate.index(index).archiveIds()
    }

    override fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray?) {
        delegate.put(index, archive, file, data, xteas)
    }

    override fun update(): Boolean {
        delegate.update()
        return true
    }
}