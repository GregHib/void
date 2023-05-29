package world.gregs.voidps.cache

import com.displee.cache.index.Index255
import java.math.BigInteger

interface Cache {

    var index255: Index255?

    fun getFile(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray?

    fun getFile(index: Int, name: String, xtea: IntArray? = null): ByteArray?

    fun getArchive(indexId: Int, archiveId: Int): ByteArray?

    fun generateVersionTable(exponent: BigInteger, modulus: BigInteger): ByteArray

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

    fun writeArchiveSector(index: Int, archive: Int, data: ByteArray)

    fun readArchiveSector(index: Int, archive: Int): ByteArray

    fun update(): Boolean

}