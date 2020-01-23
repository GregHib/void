package org.redrune.cache.secure

import java.util.*
import kotlin.experimental.or

/**
 * The Whirlpool hashing function.
 *
 * <P>
 * **References**
 *
</P> * <P>
 * The Whirlpool algorithm was developed by
 * [Paulo S. L. M. Barreto](mailto:pbarreto@scopus.com.br) and
 * [Vincent Rijmen](mailto:vincent.rijmen@cryptomathic.com).
 *
 * See
 * P.S.L.M. Barreto, V. Rijmen,
 * ``The Whirlpool hashing function,''
 * First NESSIE workshop, 2000 (tweaked version, 2003),
 * <https:></https:>//www.cosic.esat.kuleuven.ac.be/nessie/workshop/submissions/whirlpool.zip>
 *
 * @author    Paulo S.L.M. Barreto
 * @author    Vincent Rijmen.
 *
 * @version 3.0 (2003.03.12)
 *
 * =============================================================================
 *
 * Differences from version 2.1:
 *
 * - Suboptimal diffusion matrix replaced by cir(1, 1, 4, 1, 8, 5, 2, 9).
 *
 * =============================================================================
 *
 * Differences from version 2.0:
 *
 * - Generation of ISO/IEC 10118-3 test vectors.
 * - Bug fix: nonzero carry was ignored when tallying the data length
 * (this bug apparently only manifested itself when feeding data
 * in pieces rather than in a single chunk at once).
 *
 * Differences from version 1.0:
 *
 * - Original S-box replaced by the tweaked, hardware-efficient version.
 *
 * =============================================================================
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS ''AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</P> */
class Whirlpool {

    /**
     * Global number of hashed bits (256-bit counter).
     */
    private var bitLength = ByteArray(32)

    /**
     * Buffer of data to hash.
     */
    private var buffer = ByteArray(64)

    /**
     * Current number of bits on the buffer.
     */
    private var bufferBits = 0

    /**
     * Current (possibly incomplete) byte slot on the buffer.
     */
    private var bufferPos = 0

    /**
     * The hashing state.
     */
    private var hash = LongArray(8)
    private var K = LongArray(8) // the round key
    private var L = LongArray(8)
    private var block = LongArray(8) // mu(buffer)
    private var state = LongArray(8) // the cipher state

    /**
     * The core Whirlpool transform.
     */
    private fun processBuffer() {
        /*
         * map the buffer to a block:
         */
        run {
            var i = 0
            var j = 0
            while (i < 8) {
                block[i] = buffer[j].toLong() shl 56 xor
                        (buffer[j + 1].toLong() and 0xffL shl 48) xor
                        (buffer[j + 2].toLong() and 0xffL shl 40) xor
                        (buffer[j + 3].toLong() and 0xffL shl 32) xor
                        (buffer[j + 4].toLong() and 0xffL shl 24) xor
                        (buffer[j + 5].toLong() and 0xffL shl 16) xor
                        (buffer[j + 6].toLong() and 0xffL shl 8) xor
                        (buffer[j + 7].toLong() and 0xffL)
                i++
                j += 8
            }
        }
        /*
         * compute and apply K^0 to the cipher state:
         */
        for (i in 0..7) {
            K[i] = hash[i]
            state[i] = block[i] xor K[i]
        }
        /*
         * iterate over all rounds:
         */
        for (r in 1..ROUNDS) {
            /*
             * compute K^r from K^{r-1}:
             */
            for (i in 0..7) {
                L[i] = 0L
                var t = 0
                var s = 56
                while (t < 8) {
                    L[i] = L[i] xor C[t][K[i - t and 7].ushr(s).toInt() and 0xff]
                    t++
                    s -= 8
                }
            }
            for (i in 0..7) {
                K[i] = L[i]
            }
            K[0] = K[0] xor rc[r]
            /*
             * apply the r-th round transformation:
             */
            for (i in 0..7) {
                L[i] = K[i]
                var t = 0
                var s = 56
                while (t < 8) {
                    L[i] = L[i] xor C[t][state[i - t and 7].ushr(s).toInt() and 0xff]
                    t++
                    s -= 8
                }
            }
            for (i in 0..7) {
                state[i] = L[i]
            }
        }
        /*
         * apply the Miyaguchi-Preneel compression function:
         */
        for (i in 0..7) {
            hash[i] = hash[i] xor (state[i] xor block[i])
        }
    }

    /**
     * Initialize the hashing state.
     */
    fun NESSIEinit() {
        Arrays.fill(bitLength, 0.toByte())
        bufferPos = 0
        bufferBits = bufferPos
        buffer[0] = 0 // it's only necessary to cleanup buffer[bufferPos].
        Arrays.fill(hash, 0L) // initial value
    }

    /**
     * Delivers input data to the hashing algorithm.
     *
     * @param    source        plaintext data to hash.
     * @param    sourceBits    how many bits of plaintext to process.
     *
     * This method maintains the invariant: bufferBits < 512
     */
    fun NESSIEadd(source: ByteArray, sourceBits: Long) {
        var sourceBits = sourceBits
        /*
                           sourcePos
                           |
                           +-------+-------+-------
                              ||||||||||||||||||||| source
                           +-------+-------+-------
        +-------+-------+-------+-------+-------+-------
        ||||||||||||||||||||||                           buffer
        +-------+-------+-------+-------+-------+-------
                        |
                        bufferPos
        */
        var sourcePos = 0 // index of leftmost source byte containing data (1 to 8 bits).
        val sourceGap = 8 - (sourceBits.toInt() and 7) and 7 // space on source[sourcePos].
        val bufferRem = bufferBits and 7 // occupied bits on buffer[bufferPos].
        var b: Int
        // tally the length of the added data:
        var value = sourceBits
        var i = 31
        var carry = 0
        while (i >= 0) {
            carry += (bitLength[i].toInt() and 0xff) + (value.toInt() and 0xff)
            bitLength[i] = carry.toByte()
            carry = carry ushr 8
            value = value ushr 8
            i--
        }
        // process data in chunks of 8 bits:
        while (sourceBits > 8) { // at least source[sourcePos] and source[sourcePos+1] contain data.
            // take a byte from the source:
            b =
                source[sourcePos].toInt() shl sourceGap and 0xff or (source[sourcePos + 1].toInt() and 0xff).ushr(8 - sourceGap)
            if (b < 0 || b >= 256) {
                throw RuntimeException("LOGIC ERROR")
            }
            // process this byte:
            buffer[bufferPos] = buffer[bufferPos++] or b.ushr(bufferRem).toByte()
            bufferBits += 8 - bufferRem // bufferBits = 8*bufferPos;
            if (bufferBits == 512) {
                // process data block:
                processBuffer()
                // reset buffer:
                bufferPos = 0
                bufferBits = bufferPos
            }
            buffer[bufferPos] = (b shl 8 - bufferRem and 0xff).toByte()
            bufferBits += bufferRem
            // proceed to remaining data:
            sourceBits -= 8
            sourcePos++
        }
        // now 0 <= sourceBits <= 8;
        // furthermore, all data (if any is left) is in source[sourcePos].
        if (sourceBits > 0) {
            b = source[sourcePos].toInt() shl sourceGap and 0xff // bits are left-justified on b.
            // process the remaining bits:
            buffer[bufferPos] = buffer[bufferPos] or b.ushr(bufferRem).toByte()
        } else {
            b = 0
        }
        if (bufferRem + sourceBits < 8) {
            // all remaining data fits on buffer[bufferPos], and there still remains some space.
            bufferBits += sourceBits.toInt()
        } else {
            // buffer[bufferPos] is full:
            bufferPos++
            bufferBits += 8 - bufferRem // bufferBits = 8*bufferPos;
            sourceBits -= (8 - bufferRem).toLong()
            // now 0 <= sourceBits < 8; furthermore, all data is in source[sourcePos].
            if (bufferBits == 512) {
                // process data block:
                processBuffer()
                // reset buffer:
                bufferPos = 0
                bufferBits = bufferPos
            }
            buffer[bufferPos] = (b shl 8 - bufferRem and 0xff).toByte()
            bufferBits += sourceBits.toInt()
        }
    }

    /**
     * Get the hash value from the hashing state.
     *
     * This method uses the invariant: bufferBits < 512
     */
    fun NESSIEfinalize(digest: ByteArray) {
        // append a '1'-bit:
        buffer[bufferPos] = buffer[bufferPos] or 0x80.ushr(bufferBits and 7).toByte()
        bufferPos++ // all remaining bits on the current byte are set to zero.
        // pad with zero bits to complete 512N + 256 bits:
        if (bufferPos > 32) {
            while (bufferPos < 64) {
                buffer[bufferPos++] = 0
            }
            // process data block:
            processBuffer()
            // reset buffer:
            bufferPos = 0
        }
        while (bufferPos < 32) {
            buffer[bufferPos++] = 0
        }
        // append bit length of hashed data:
        System.arraycopy(bitLength, 0, buffer, 32, 32)
        // process data block:
        processBuffer()
        // return the completed message digest:
        var i = 0
        var j = 0
        while (i < 8) {
            val h = hash[i]
            digest[j] = h.ushr(56).toByte()
            digest[j + 1] = h.ushr(48).toByte()
            digest[j + 2] = h.ushr(40).toByte()
            digest[j + 3] = h.ushr(32).toByte()
            digest[j + 4] = h.ushr(24).toByte()
            digest[j + 5] = h.ushr(16).toByte()
            digest[j + 6] = h.ushr(8).toByte()
            digest[j + 7] = h.toByte()
            i++
            j += 8
        }
    }

    /**
     * Delivers string input data to the hashing algorithm.
     *
     * @param    source        plaintext data to hash (ASCII text string).
     *
     * This method maintains the invariant: bufferBits < 512
     */
    fun NESSIEadd(source: String) {
        if (source.isNotEmpty()) {
            val data = ByteArray(source.length)
            for (i in 0 until source.length) {
                data[i] = source[i].toByte()
            }
            NESSIEadd(data, (8 * data.size).toLong())
        }
    }

    companion object {

        /**
         * The message digest size (in bits)
         */
        private const val DIGESTBITS = 512

        /**
         * The message digest size (in bytes)
         */
        val DIGESTBYTES = DIGESTBITS.ushr(3)

        /**
         * The number of rounds of the internal dedicated block cipher.
         */
        private const val ROUNDS = 10

        /**
         * The substitution box.
         */
        private const val sbox = "\u1823\uc6E8\u87B8\u014F\u36A6\ud2F5\u796F\u9152" +
                "\u60Bc\u9B8E\uA30c\u7B35\u1dE0\ud7c2\u2E4B\uFE57" +
                "\u1577\u37E5\u9FF0\u4AdA\u58c9\u290A\uB1A0\u6B85" +
                "\uBd5d\u10F4\ucB3E\u0567\uE427\u418B\uA77d\u95d8" +
                "\uFBEE\u7c66\udd17\u479E\ucA2d\uBF07\uAd5A\u8333" +
                "\u6302\uAA71\uc819\u49d9\uF2E3\u5B88\u9A26\u32B0" +
                "\uE90F\ud580\uBEcd\u3448\uFF7A\u905F\u2068\u1AAE" +
                "\uB454\u9322\u64F1\u7312\u4008\uc3Ec\udBA1\u8d3d" +
                "\u9700\ucF2B\u7682\ud61B\uB5AF\u6A50\u45F3\u30EF" +
                "\u3F55\uA2EA\u65BA\u2Fc0\udE1c\uFd4d\u9275\u068A" +
                "\uB2E6\u0E1F\u62d4\uA896\uF9c5\u2559\u8472\u394c" +
                "\u5E78\u388c\ud1A5\uE261\uB321\u9c1E\u43c7\uFc04" +
                "\u5199\u6d0d\uFAdF\u7E24\u3BAB\ucE11\u8F4E\uB7EB" +
                "\u3c81\u94F7\uB913\u2cd3\uE76E\uc403\u5644\u7FA9" +
                "\u2ABB\uc153\udc0B\u9d6c\u3174\uF646\uAc89\u14E1" +
                "\u163A\u6909\u70B6\ud0Ed\ucc42\u98A4\u285c\uF886"

        private val C = Array(8) { LongArray(256) }
        private val rc = LongArray(ROUNDS + 1)

        init {
            for (x in 0..255) {
                val c = sbox[x / 2]
                val v1 = (if (x and 1 == 0) c.toInt().ushr(8) else c.toInt() and 0xff).toLong()
                var v2 = v1 shl 1
                if (v2 >= 0x100L) {
                    v2 = v2 xor 0x11dL
                }
                var v4 = v2 shl 1
                if (v4 >= 0x100L) {
                    v4 = v4 xor 0x11dL
                }
                val v5 = v4 xor v1
                var v8 = v4 shl 1
                if (v8 >= 0x100L) {
                    v8 = v8 xor 0x11dL
                }
                val v9 = v8 xor v1
                /*
             * build the circulant table C[0][x] = S[x].[1, 1, 4, 1, 8, 5, 2, 9]:
             */
                C[0][x] = v1 shl 56 or (v1 shl 48) or (v4 shl 40) or (v1 shl 32) or
                        (v8 shl 24) or (v5 shl 16) or (v2 shl 8) or v9
                /*
             * build the remaining circulant tables C[t][x] = C[0][x] rotr t
             */
                for (t in 1..7) {
                    C[t][x] = C[t - 1][x].ushr(8) or (C[t - 1][x] shl 56)
                }
            }

            /*
         * build the round constants:
         */
            rc[0] = 0L /* not used (assignment kept only to properly initialize all variables) */
            for (r in 1..ROUNDS) {
                val i = 8 * (r - 1)
                rc[r] = C[0][i] and -0x100000000000000L xor
                        (C[1][i + 1] and 0x00ff000000000000L) xor
                        (C[2][i + 2] and 0x0000ff0000000000L) xor
                        (C[3][i + 3] and 0x000000ff00000000L) xor
                        (C[4][i + 4] and 0x00000000ff000000L) xor
                        (C[5][i + 5] and 0x0000000000ff0000L) xor
                        (C[6][i + 6] and 0x000000000000ff00L) xor
                        (C[7][i + 7] and 0x00000000000000ffL)
            }
        }

        fun whirlpool(data: ByteArray, off: Int, len: Int): ByteArray {
            val source: ByteArray
            if (off <= 0) {
                source = data
            } else {
                source = ByteArray(len)
                for (i in 0 until len)
                    source[i] = data[off + i]
            }
            val whirlpool = Whirlpool()
            whirlpool.NESSIEinit()
            whirlpool.NESSIEadd(source, (len * 8).toLong())
            val digest = ByteArray(64)
            whirlpool.NESSIEfinalize(digest)
            return digest
        }
    }

}
