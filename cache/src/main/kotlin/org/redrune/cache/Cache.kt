package org.redrune.cache

import com.displee.cache.index.Index255
import java.math.BigInteger

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface Cache {

    var index255: Index255?

    fun getFile(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray?

    fun getArchive(indexId: Int, archiveId: Int): ByteArray?

    fun generateVersionTable(exponent: BigInteger, modulus: BigInteger): ByteArray

    fun close()

    fun getIndexCrc(indexId: Int): Int

    fun lastIndexId(indexId: Int): Int

    fun archiveCount(indexId: Int, archiveId: Int): Int
}