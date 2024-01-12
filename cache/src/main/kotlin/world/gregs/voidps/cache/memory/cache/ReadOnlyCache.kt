package world.gregs.voidps.cache.memory.cache

import world.gregs.voidps.cache.Cache

open class ReadOnlyCache(
    protected val indices: IntArray,
    protected val archives: Array<IntArray?>,
    protected val fileCounts: Array<IntArray?>,
    protected val files: Array<Array<IntArray?>?>,
    private val hashes: Map<Int, Int>?
) : Cache {

    override fun files(index: Int, archive: Int): IntArray? {
        return files.getOrNull(index)?.getOrNull(archive)
    }

    override fun archives(index: Int): IntArray? {
        return archives.getOrNull(index)
    }

    override fun indexes(): Int {
        return indices.size
    }

    override fun indices(): IntArray {
        return indices
    }

    override fun archiveCount(indexId: Int, archiveId: Int): Int {
        return fileCounts.getOrNull(indexId)?.getOrNull(archiveId) ?: 0
    }

    override fun lastFileId(indexId: Int, archive: Int): Int {
        return files.getOrNull(indexId)?.getOrNull(archive)?.last() ?: -1
    }

    override fun lastArchiveId(indexId: Int): Int {
        return archives.getOrNull(indexId)?.last() ?: -1
    }

    override fun archiveId(name: String): Int {
        return hashes?.get(name.hashCode()) ?: -1
    }

    override fun getFile(index: Int, name: String, xtea: IntArray?): ByteArray? {
        return data(index, archiveId(name), 0, xtea)
    }

    override fun getFile(index: Int, archive: Int, file: Int, xtea: IntArray?): ByteArray? {
        return data(index, archive, file, xtea)
    }

    override fun close() {
    }

    override fun getArchiveId(index: Int, name: String): Int {
        return archiveId(name)
    }

    override fun getIndexCrc(indexId: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getArchiveId(index: Int, archive: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getArchives(index: Int): IntArray {
        return archives(index) ?: IntArray(0)
    }

    override fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray?) {
        TODO("Not yet implemented")
    }

    override fun update(): Boolean {
        return false
    }

    override fun getArchiveData(index: Int, archive: Int): Map<Int, ByteArray?>? {
        TODO("Not yet implemented")
    }

}