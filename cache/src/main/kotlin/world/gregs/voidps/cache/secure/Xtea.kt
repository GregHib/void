@file:Suppress("INTEGER_OVERFLOW")

package world.gregs.voidps.cache.secure

import io.netty.buffer.ByteBuf
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
    fun decipher(buffer: ByteArray, key: IntArray, start: Int = 0) {
        if (key.size != 4) {
            throw IllegalArgumentException()
        }

        val numQuads = buffer.size / 8
        for (i in 0 until numQuads) {
            var sum = GOLDEN_RATIO * ROUNDS
            var v0 = (buffer[start + i * 8].toInt() and 0xff shl 24) or (buffer[start + i * 8 + 1].toInt() and 0xff shl 16) or (buffer[start + i * 8 + 2].toInt() and 0xff shl 8) or (buffer[start + i * 8 + 3].toInt() and 0xff)
            var v1 = (buffer[start + i * 8 + 4].toInt() and 0xff shl 24) or (buffer[start + i * 8 + 5].toInt() and 0xff shl 16) or (buffer[start + i * 8 + 6].toInt() and 0xff shl 8) or (buffer[start + i * 8 + 7].toInt() and 0xff)
            for (j in 0 until ROUNDS) {
                v1 -= (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
                sum -= GOLDEN_RATIO
                v0 -= (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
            }
            buffer[start + i * 8] = (v0 shr 24).toByte()
            buffer[start + i * 8 + 1] = (v0 shr 16).toByte()
            buffer[start + i * 8 + 2] = (v0 shr 8).toByte()
            buffer[start + i * 8 + 3] = v0.toByte()
            buffer[start + i * 8 + 4] = (v1 shr 24).toByte()
            buffer[start + i * 8 + 5] = (v1 shr 16).toByte()
            buffer[start + i * 8 + 6] = (v1 shr 8).toByte()
            buffer[start + i * 8 + 7] = v1.toByte()
        }
    }

    /**
     * Deciphers the specified [ByteBuf] with the given key.
     * @param buffer The buffer.
     * @param key The key.
     * @throws IllegalArgumentException if the key is not exactly 4 elements
     * long.
     */
    fun decipher(buffer: ByteBuf, key: IntArray, start: Int = buffer.readerIndex(), end: Int = buffer.readableBytes()) {
        if (key.size != 4) {
            throw IllegalArgumentException()
        }

        val numQuads = (end - start) / 8
        for (i in 0 until numQuads) {
            var sum = GOLDEN_RATIO * ROUNDS
            var v0 = buffer.getInt(start + i * 8)
            var v1 = buffer.getInt(start + i * 8 + 4)
            for (j in 0 until ROUNDS) {
                v1 -= (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
                sum -= GOLDEN_RATIO
                v0 -= (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
            }
            buffer.setInt(start + i * 8, v0)
            buffer.setInt(start + i * 8 + 4, v1)
        }
    }

    /**
     * Enciphers the specified [ByteBuf] with the given key.
     * @param buffer The buffer.
     * @param key The key.
     * @throws IllegalArgumentException if the key is not exactly 4 elements
     * long.
     */
    fun encipher(buffer: ByteBuf, start: Int, end: Int, key: IntArray) {
        if (key.size != 4)
            throw IllegalArgumentException()

        val numQuads = (end - start) / 8
        for (i in 0 until numQuads) {
            var sum = 0
            var v0 = buffer.getInt(start + i * 8)
            var v1 = buffer.getInt(start + i * 8 + 4)
            for (j in 0 until ROUNDS) {
                v0 += (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
                sum += GOLDEN_RATIO
                v1 += (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
            }
            buffer.setInt(start + i * 8, v0)
            buffer.setInt(start + i * 8 + 4, v1)
        }
    }
}
