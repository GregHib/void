package world.gregs.voidps.cache

import java.math.BigInteger
import java.util.*

interface Cache {

    val versionTable: ByteArray

    fun indexCount(): Int

    fun indices(): IntArray

    fun sector(index: Int, archive: Int): ByteArray?

    fun archives(index: Int): IntArray

    fun archiveCount(index: Int): Int

    fun lastArchiveId(indexId: Int): Int

    fun archiveId(index: Int, hash: Int): Int

    fun archiveId(index: Int, name: String): Int = archiveId(index, name.hashCode())

    fun files(index: Int, archive: Int): IntArray

    fun fileCount(indexId: Int, archiveId: Int): Int

    fun lastFileId(indexId: Int, archive: Int): Int

    fun data(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray?

    fun data(index: Int, name: String, xtea: IntArray? = null) = data(index, archiveId(index, name), xtea = xtea)

    fun write(index: Int, archive: Int, file: Int, data: ByteArray, xteas: IntArray? = null)

    fun write(index: Int, archive: String, data: ByteArray, xteas: IntArray? = null)

    fun update(): Boolean

    fun close()

    companion object {
        fun load(properties: Properties): Cache {
            val live = properties.getProperty("server.live").toBoolean()
             val loader = if (live) MemoryCache else FileCache
//            val fileModulus = BigInteger(properties.getProperty("security.file.modulus"), 16)
//            val filePrivate = BigInteger(properties.getProperty("security.file.private"), 16)
//            val delegate = CacheDelegate(properties.getProperty("storage.cache.path"), filePrivate, fileModulus)
            return loader.load(properties)
        }
    }
}