package world.gregs.voidps.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.index.Index255
import com.github.michaelbull.logging.InlineLogger
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.math.BigInteger

class CacheDelegate(directory: String) : Cache {

    private val delegate: Reference<CacheLibrary> = SoftReference(CacheLibrary(directory))

    private val logger = InlineLogger()

    init {
        logger.info { "Cache read from $directory" }
    }

    override var index255: Index255?
        get() = delegate.get()?.index255
        set(value) {
            delegate.get()?.index255 = value
        }

    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?) =
        delegate.get()?.data(index, archive, file, xtea)

    override fun getFile(index: Int, name: String, xtea: IntArray?) = delegate.get()?.data(index, name, xtea)

    override fun getArchive(indexId: Int, archiveId: Int): ByteArray? {
        val index = if (indexId == 255) index255 else delegate.get()?.index(indexId)
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

    override fun generateVersionTable(exponent: BigInteger, modulus: BigInteger) = delegate.get()!!.generateNewUkeys(exponent, modulus)

    override fun close() {
        delegate.get()?.close()
        delegate.clear()
    }

    override fun getIndexCrc(indexId: Int): Int {
        return delegate.get()?.index(indexId)?.crc ?: 0
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return delegate.get()?.index(indexId)?.archive(archiveId)?.fileIds()?.size ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return delegate.get()?.index(indexId)?.archive(archive)?.last()?.id ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return delegate.get()?.index(indexId)?.last()?.id ?: 0
    }

    override fun getArchiveId(index: Int, name: String): Int {
        return delegate.get()?.index(index)?.archiveId(name) ?: -1
    }

    override fun getArchiveId(index: Int, hash: Int): Int {
        delegate.get()?.index(index)?.archives()?.forEach { archive ->
            if (archive.hashName == hash) {
                return archive.id
            }
        }
        return -1
    }

    override fun getArchives(index: Int): IntArray {
        return delegate.get()?.index(index)?.archiveIds() ?: intArrayOf()
    }

    override fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray?) {
        delegate.get()?.put(index, archive, file, data, xteas)
    }

    override fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray?) {
        delegate.get()?.put(index, archive, data, xteas)
    }

    override fun writeArchiveSector(index: Int, archive: Int, data: ByteArray) {
        delegate.get()?.index(index)?.writeArchiveSector(archive, data)
    }

    override fun readArchiveSector(index: Int, archive: Int): ByteArray {
        return delegate.get()?.index(index)?.readArchiveSector(archive)?.data ?: byteArrayOf()
    }

    override fun update(): Boolean {
        delegate.get()?.update()
        return true
    }
}