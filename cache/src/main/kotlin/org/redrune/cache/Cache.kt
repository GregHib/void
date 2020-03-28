package org.redrune.cache

import com.displee.cache.index.Index
import com.displee.cache.index.Index255
import java.io.RandomAccessFile
import java.math.BigInteger

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
interface Cache {
    val path: String

    val clearDataAfterUpdate: Boolean

    var mainFile: RandomAccessFile

    var index255: Index255?

    var closed: Boolean

    fun reload()

    fun exists(id: Int): Boolean

    fun index(id: Int): Index

    fun data(index: Int, archive: Int, file: Int = 0, xtea: IntArray? = null): ByteArray?

    fun generateOldUkeys(): ByteArray

    fun generateNewUkeys(exponent: BigInteger, modulus: BigInteger): ByteArray

    fun close()

    fun first(): Index?

    fun last(): Index?

    fun is317(): Boolean

    fun isOSRS(): Boolean

    fun isRS3(): Boolean

    fun indices(): Array<Index>

    fun getFile(indexId: Int, archiveId: Int): ByteArray?

    fun valid(index: Int, archive: Int): Boolean
}