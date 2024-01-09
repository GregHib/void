package world.gregs.voidps.cache

interface Cache {

    open fun data(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray? {
        return null
    }

    open fun files(index: Int, archive: Int): IntArray? {
        return null
    }

    open fun archives(index: Int): IntArray? {
        return null
    }

    open fun indexes(): Int {
        return 0
    }

    fun getFile(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray?

    fun getFile(index: Int, name: String, xtea: IntArray? = null): ByteArray?

    fun close()

    fun getIndexCrc(indexId: Int): Int

    fun archiveCount(indexId: Int, archiveId: Int): Int

    fun lastFileId(indexId: Int, archive: Int): Int

    fun lastArchiveId(indexId: Int): Int

    fun getArchiveId(index: Int, name: String): Int

    fun getArchiveId(index: Int, archive: Int): Int

    fun getArchives(index: Int): IntArray

    fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray? = null)

    fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray? = null)

    fun update(): Boolean

    fun getArchiveData(index: Int, archive: Int): Map<Int, ByteArray?>?

}