package world.gregs.voidps.cache

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import java.lang.ref.Reference
import java.lang.ref.SoftReference

class CacheDelegate(directory: String) : Cache {

    private val delegate: Reference<CacheLibrary>

    private val logger = InlineLogger()

    init {
        val start = System.currentTimeMillis()
        delegate = SoftReference(CacheLibrary(directory))
        logger.info { "Cache read from $directory in ${System.currentTimeMillis() - start}ms" }
    }

    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?) =
        delegate.get()?.data(index, archive, file, xtea)

    override fun getFile(index: Int, name: String, xtea: IntArray?) = delegate.get()?.data(index, name, xtea)

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

    override fun update(): Boolean {
        delegate.get()?.update()
        return true
    }
}