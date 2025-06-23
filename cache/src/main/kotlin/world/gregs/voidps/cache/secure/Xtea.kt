@file:Suppress("INTEGER_OVERFLOW")

package world.gregs.voidps.cache.secure

import java.nio.ByteBuffer

object Xtea {

    /**
     * The golden ratio.
     */
    private const val GOLDEN_RATIO = -0x61c88647

    /**
     * The number of rounds.
     */
    private const val ROUNDS = 32

    /**
     * Deciphers the specified [ByteBuffer] with the given key.
     * @param buffer The buffer.
     * @param key The key.
     * @throws IllegalArgumentException if the key is not exactly 4 elements
     * long.
     */
    fun decipher(buffer: ByteArray, key: IntArray, start: Int = 0, end: Int = buffer.size) {
        if (key.size != 4) {
            throw IllegalArgumentException()
        }

        val numQuads = (end - start) / 8
        for (i in 0 until numQuads) {
            var sum = GOLDEN_RATIO * ROUNDS
            var v0 = getInt(buffer, start + i * 8)
            var v1 = getInt(buffer, start + i * 8 + 4)
            for (j in 0 until ROUNDS) {
                v1 -= (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
                sum -= GOLDEN_RATIO
                v0 -= (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
            }
            putInt(buffer, start + i * 8, v0)
            putInt(buffer, start + i * 8 + 4, v1)
        }
    }

    /**
     * Enciphers the specified [ByteArray] with the given key.
     * @param buffer The buffer.
     * @param key The key.
     * @throws IllegalArgumentException if the key is not exactly 4 elements
     * long.
     */
    fun encipher(buffer: ByteArray, start: Int, end: Int, key: IntArray) {
        if (key.size != 4) {
            throw IllegalArgumentException()
        }

        val numQuads = (end - start) / 8
        for (i in 0 until numQuads) {
            var sum = 0
            var v0 = getInt(buffer, start + i * 8)
            var v1 = getInt(buffer, start + i * 8 + 4)
            for (j in 0 until ROUNDS) {
                v0 += (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
                sum += GOLDEN_RATIO
                v1 += (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
            }
            putInt(buffer, start + i * 8, v0)
            putInt(buffer, start + i * 8 + 4, v1)
        }
    }

    private fun getInt(buffer: ByteArray, index: Int) = (buffer[index].toInt() and 0xff shl 24) or (buffer[index + 1].toInt() and 0xff shl 16) or (buffer[index + 2].toInt() and 0xff shl 8) or (buffer[index + 3].toInt() and 0xff)
    private fun putInt(buffer: ByteArray, index: Int, value: Int) {
        buffer[index] = (value shr 24).toByte()
        buffer[index + 1] = (value shr 16).toByte()
        buffer[index + 2] = (value shr 8).toByte()
        buffer[index + 3] = value.toByte()
    }
}
