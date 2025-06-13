package world.gregs.voidps.cache.secure

import world.gregs.voidps.cache.ReadOnlyCache
import java.math.BigInteger

class VersionTableBuilder(
    private val exponent: BigInteger,
    private val modulus: BigInteger,
    private val indexCount: Int,
) {

    private val crc = CRC()
    private val versionTable = ByteArray(positionFor(indexCount) + MAX_RSA_SIZE)
    private var built = false

    init {
        versionTable[5] = indexCount.toByte()
    }

    fun skip(index: Int) {
        val pos = positionFor(index)
        for (i in 0 until TABLE_INDEX_OFFSET) {
            versionTable[pos + i] = 0
        }
    }

    fun sector(index: Int, sectorData: ByteArray, whirlpool: Whirlpool) {
        val crc = crc.calculate(sectorData)
        crc(index, crc)
        val output = ByteArray(ReadOnlyCache.WHIRLPOOL_SIZE)
        whirlpool.reset()
        whirlpool.add(sectorData)
        whirlpool.finalize(output)
        whirlpool(index, output)
    }

    fun crc(index: Int, crc: Int) {
        val pos = positionFor(index)
        versionTable[pos] = (crc shr 24).toByte()
        versionTable[pos + 1] = (crc shr 16).toByte()
        versionTable[pos + 2] = (crc shr 8).toByte()
        versionTable[pos + 3] = (crc).toByte()
    }

    fun revision(index: Int, revision: Int) {
        val pos = positionFor(index) + 4
        versionTable[pos] = (revision shr 24).toByte()
        versionTable[pos + 1] = (revision shr 16).toByte()
        versionTable[pos + 2] = (revision shr 8).toByte()
        versionTable[pos + 3] = (revision).toByte()
    }

    fun whirlpool(index: Int, whirlpool: ByteArray) {
        val pos = positionFor(index) + 8
        for (i in whirlpool.indices) {
            versionTable[pos + i] = whirlpool[i]
        }
    }

    fun build(whirlpool: Whirlpool = Whirlpool()): ByteArray {
        if (built) {
            return versionTable
        }
        built = true
        val output = ByteArray(ReadOnlyCache.WHIRLPOOL_SIZE + 1)
        output[0] = 1
        whirlpool.reset()
        whirlpool.add(versionTable, 5, positionFor(indexCount) - 5)
        whirlpool.finalize(output, 1)
        val rsa = RSA.crypt(output, modulus, exponent)
        val pos = positionFor(indexCount)
        for (i in rsa.indices) {
            versionTable[pos + i] = rsa[i]
        }
        val end = pos + rsa.size
        versionTable[0] = 0
        val value = end - 5
        versionTable[1] = (value shr 24).toByte()
        versionTable[2] = (value shr 16).toByte()
        versionTable[3] = (value shr 8).toByte()
        versionTable[4] = (value).toByte()
        val data = ByteArray(end)
        System.arraycopy(versionTable, 0, data, 0, data.size)
        return data
    }

    companion object {
        private fun positionFor(index: Int) = 6 + index * TABLE_INDEX_OFFSET
        private const val TABLE_INDEX_OFFSET = ReadOnlyCache.WHIRLPOOL_SIZE + 8
        private const val MAX_RSA_SIZE = 256
    }
}
