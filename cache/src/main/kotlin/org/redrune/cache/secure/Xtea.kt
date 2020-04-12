@file:Suppress("INTEGER_OVERFLOW")

package org.redrune.cache.secure

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
    fun decipher(buffer: ByteBuffer, key: IntArray, start: Int = buffer.position(), end: Int = buffer.remaining()) {
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
            buffer.putInt(start + i * 8, v0)
            buffer.putInt(start + i * 8 + 4, v1)
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
