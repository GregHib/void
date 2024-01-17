package world.gregs.voidps.cache.secure

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.ReadOnlyCache
import java.math.BigInteger

class VersionTableBuilder(
    private val exponent: BigInteger,
    private val modulus: BigInteger,
    private val indexCount: Int
) {

    private val versionTable: BufferWriter = BufferWriter(positionFor(indexCount) + MAX_RSA_SIZE)
    private var built = false

    init {
        versionTable.position(5)
        versionTable.writeByte(indexCount)
    }

    fun skip(index: Int) {
        versionTable.position(positionFor(index))
        versionTable.skip(TABLE_INDEX_OFFSET)
    }

    fun crc(index: Int, crc: Int) {
        versionTable.position(positionFor(index))
        versionTable.writeInt(crc)
    }

    fun revision(index: Int, revision: Int) {
        versionTable.position(positionFor(index) + 4)
        versionTable.writeInt(revision)
    }

    fun whirlpool(index: Int, whirlpool: ByteArray) {
        versionTable.position(positionFor(index) + 8)
        versionTable.writeBytes(whirlpool)
    }


    fun build(): ByteArray {
        if (built) {
            return versionTable.toArray()
        }
        built = true
        val whirlpool = ByteArray(ReadOnlyCache.WHIRLPOOL_SIZE + 1)
        whirlpool[0] = 1
        CRC.generateWhirlpool(versionTable.array(), 5, whirlpool, 1, positionFor(indexCount) - 5)
        val rsa = RSA.crypt(whirlpool, modulus, exponent)
        versionTable.position(positionFor(indexCount))
        versionTable.writeBytes(rsa)
        val end = versionTable.position()
        versionTable.position(0)
        versionTable.writeByte(0)
        versionTable.writeInt(end - 5)
        versionTable.position(end)
        return versionTable.toArray()
    }

    companion object {
        private fun positionFor(index: Int) = 6 + index * TABLE_INDEX_OFFSET
        private const val TABLE_INDEX_OFFSET = ReadOnlyCache.WHIRLPOOL_SIZE + 8
        private const val MAX_RSA_SIZE = 256
    }
}